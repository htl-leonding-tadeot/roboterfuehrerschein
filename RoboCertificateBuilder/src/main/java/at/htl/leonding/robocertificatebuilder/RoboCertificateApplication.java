package at.htl.leonding.robocertificatebuilder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class RoboCertificateApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RoboCertificateApplication.class.getResource("view/certificate-builder-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 350, 240);
        stage.setTitle("Certificate Builder (Pro)");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}