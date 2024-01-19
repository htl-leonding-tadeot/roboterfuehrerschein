package at.htl.leonding.robocertificatebuilder.controller;

import at.htl.leonding.robocertificatebuilder.service.CertificateBuilderService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class CertificateBuilderController {
    @FXML
    public TextField txtFirstName;
    @FXML
    public TextField txtLastName;
    @FXML
    public DatePicker dpDob;
    @FXML
    public Button btnPic;
    @FXML
    public Button btnPdf;
    @FXML
    public Label status;

    private CertificateBuilderService certService;

    private String imagePath;

    @FXML
    public void initialize() {
        certService = CertificateBuilderService.getInstance();
    }

    @FXML
    public void onPicClicked() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(status.getScene().getWindow());
        if(selectedFile != null) {
            this.imagePath = selectedFile.getAbsolutePath();
        } else {
            this.status.setText("Please select a valid file");
        }
    }

    @FXML
    public void onPdfClicked() throws TransformerException, IOException, SAXException {
        if(imagePath == null || imagePath.isBlank()) {
            status.setText("Please open an image before creating pdf!");
            return;
        }

        try {
            dpDob.getConverter().fromString(dpDob.getEditor().getText());
        } catch (Exception e) {
            status.setText("Please enter a valid Date!");
            return;
        }

        status.setText("");
        try {
            certService.buildDocument(txtFirstName.getText(), txtLastName.getText(), dpDob.getValue(), imagePath);
        } catch (Exception e) {
            status.setText("failed to build pdf");
            return;
        }

        status.setText("pdf build complete");
    }
}