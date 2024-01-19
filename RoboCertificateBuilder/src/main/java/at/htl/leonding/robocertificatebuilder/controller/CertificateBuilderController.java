package at.htl.leonding.robocertificatebuilder.controller;

import at.htl.leonding.robocertificatebuilder.service.CertificateBuilderService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;

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

    private CertificateBuilderService certService;

    @FXML
    public void initialize() {
        certService = CertificateBuilderService.getInstance();
    }

    @FXML
    public void onPicClicked() {

    }

    @FXML
    public void onPdfClicked() throws TransformerException, IOException, SAXException {
        certService.buildDocument(txtFirstName.getText(), txtLastName.getText(), dpDob.getValue(), "");
    }
}