package at.htl.leonding.pimpedhotroad.server.rework;

import at.htl.leonding.pimpedhotroad.logger.FileLogger;
import at.htl.leonding.pimpedhotroad.model.Impulse;
import at.htl.leonding.pimpedhotroad.server.*;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputProvider;
import com.pi4j.io.gpio.digital.DigitalState;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 1/26/17.
 */
public class ImpulseProcessor extends Thread {
    public interface ImpulseProcessorListener{
        void onImpulseReceived(Socket socket, Impulse receivedImpulse);
        void onImpulseProcessed(Socket socket, Impulse processedImpulse);
        void onStreamDisconnected(Socket socket);
    }

    private Socket socket;
    private List<ImpulseProcessorListener> listeners;
    private boolean running = true;

    private final DirectoryPlayer player;

    private static final int PIN_PWMA = 0;
    private static final int PIN_AIN1 = 1;
    private static final int PIN_AIN2 = 2;
    private static final int PIN_PWMB = 3;
    private static final int PIN_BIN1 = 4;
    private static final int PIN_BIN2 = 5;
    private static final int PIN_STBY = 6;

    private volatile DigitalOutput PWMA;
    private volatile DigitalOutput AIN1;
    private volatile DigitalOutput AIN2;
    private volatile DigitalOutput PWMB;
    private volatile DigitalOutput BIN1;
    private volatile DigitalOutput BIN2;
    private volatile DigitalOutput STBY;

    private final Context PI4J_CONTEXT = Pi4J.newAutoContext();

    public ImpulseProcessor(Socket socket, DirectoryPlayer player){
        this.socket = socket;
        this.listeners = new ArrayList<>();
        this.player = player;

        DigitalOutputProvider digitalOutputProvider = PI4J_CONTEXT.provider("pigpio-digital-output");

        PWMA = digitalOutputProvider.create(getConfigForPin(PIN_PWMA));
        AIN1 = digitalOutputProvider.create(getConfigForPin(PIN_AIN1));
        AIN2 = digitalOutputProvider.create(getConfigForPin(PIN_AIN2));
        PWMB = digitalOutputProvider.create(getConfigForPin(PIN_PWMB));
        BIN1 = digitalOutputProvider.create(getConfigForPin(PIN_BIN1));
        BIN2 = digitalOutputProvider.create(getConfigForPin(PIN_BIN2));
        STBY = digitalOutputProvider.create(getConfigForPin(PIN_STBY));
    }

    public DigitalOutputConfig getConfigForPin(int pin) {
        return DigitalOutput.newConfigBuilder(PI4J_CONTEXT)
                .address(pin)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .build();
    }

    public void addListener(ImpulseProcessorListener listener){
        if(listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(ImpulseProcessorListener listener){
        if(listener != null){
            listeners.remove(listener);
        }
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            while(running) {
                Object inputObject = objectInputStream.readObject();

                if (inputObject instanceof Impulse impulse) {
                    for (ImpulseProcessorListener listener : listeners) {
                        listener.onImpulseReceived(socket, impulse);
                    }

                    processImpulse(impulse);

                    for (ImpulseProcessorListener listener : listeners) {
                        listener.onImpulseProcessed(socket, impulse);
                    }
                }else{
                    FileLogger.getInstance().log(this.getClass(), "Got object that is not an impulse");
                }
            }

            objectInputStream.close();
            inputStream.close();

            objectInputStream = null;
            inputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
            FileLogger.getInstance().log(this.getClass(), e.getMessage());
            running = false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            FileLogger.getInstance().log(this.getClass(), e.getMessage());
        }

        for (ImpulseProcessorListener listener :
                listeners) {
            listener.onStreamDisconnected(socket);
        }
    }

    /**
     * Function to process all received signals from the client.
     *
     * Needs to be altered everytime the vehicle is modified. Currently: 2
     * motors with 2 wheels and a GHETTOBLASTAH!
     *
     * @param impulse Single impulse
     */
    private synchronized void processImpulse(Impulse impulse) {
        System.out.println("Impulse received: " + impulse.toString());

        switch (impulse) {
            case FORWARD:
                AIN1.high();
                AIN2.low();
                BIN1.high();
                BIN2.low();
                STBY.high();
                PWMA.high();
                PWMB.high();
                break;
            case RIGHT:
                AIN1.low();
                AIN2.low();
                BIN1.high();
                BIN2.low();
                STBY.high();
                PWMA.low();
                PWMB.high();
                break;
            case BACKWARD:
                AIN1.low();
                AIN2.high();
                BIN1.low();
                BIN2.high();
                STBY.high();
                PWMA.high();
                PWMB.high();
                break;
            case LEFT:
                AIN1.high();
                AIN2.low();
                BIN1.low();
                BIN2.low();
                STBY.high();
                PWMA.high();
                PWMB.low();
                break;
            case STOP:
                AIN1.low();
                AIN2.low();
                BIN1.low();
                BIN2.low();
                STBY.low();
                PWMA.low();
                PWMB.low();
                break;
            case QUIT:
                AIN1.low();
                AIN2.low();
                BIN1.low();
                BIN2.low();
                STBY.low();
                PWMA.low();
                PWMB.low();
                processMusicImpulse(Impulse.PAUSE_SONG);
                running = false;
                break;
            case PLAY_SONG:
            case PAUSE_SONG:
            case NEXT_SONG:
            case PREV_SONG:
                processMusicImpulse(impulse);
                break;
        }
    }

    /**
     * Processes a music impulse for the vehicle.
     *
     * @param impulse Music impulse
     */
    private synchronized void processMusicImpulse(Impulse impulse) {
        try {
            switch (impulse) {
                case PLAY_SONG:
                    player.play();
                    break;

                case PAUSE_SONG:
                    player.pause();
                    break;
                case NEXT_SONG:
                    player.next();
                    break;
                case PREV_SONG:
                    player.prev();
                    break;
            }
        } catch (Exception ex) {
            System.out.println("Music playback error! Doing nothing...");
            Logger.getLogger(at.htl.leonding.pimpedhotroad.server.rework.VehicleServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
