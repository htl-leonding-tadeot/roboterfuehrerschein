package at.htl.leonding.robocertificatebuilder.service;


import at.htl.leonding.robocertificatebuilder.RoboCertificateApplication;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.MimeConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.SAXException;
import org.apache.fop.apps.FOUserAgent;

public class CertificateBuilderService {
    private static final CertificateBuilderService instance = new CertificateBuilderService();


    private Document doc;
    private Element picture;
    private Element lastName;
    private Element firstName;
    private Element dob;
    private Element doi; // Date of issue

    private CertificateBuilderService() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = documentBuilder.parse(RoboCertificateApplication.class.getResourceAsStream("templates/roboschein.xfd"));
            doc.getDocumentElement().normalize();

            List<Element> basicData = getElementsById("fo:block", List.of("nachname", "vorname", "geburtsdatum", "ausstellungsdatum"));
            List<Element> pictures = getElementsById("fo:external-graphic", List.of("foto", "robotemplate"));

            lastName = basicData.get(0);
            firstName = basicData.get(1);
            dob = basicData.get(2);
            doi = basicData.get(3);
            picture = pictures.get(0);
            // Template for robo certificate
            Element roboImage = pictures.get(1);

            if(!Files.exists(Path.of("roboschein.png"))) {
                Files.copy(Objects.requireNonNull(RoboCertificateApplication.class.getResourceAsStream("templates/roboschein.png")), Path.of("roboschein.png"));
            }

            roboImage.setAttribute("src", String.format("url(%s)", Paths.get("roboschein.png").toAbsolutePath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CertificateBuilderService getInstance() {
        return instance;
    }

    private List<Element> getElementsById(String tagName, List<String> ids) {
        List<Element> elements = new ArrayList<>();
        NodeList dataBlocks = doc.getElementsByTagName(tagName);
        System.out.println(dataBlocks.getLength());

        for(int i = 0; i < dataBlocks.getLength(); i++) {
            Node n = dataBlocks.item(i);
            if(n.getNodeType() == Node.ELEMENT_NODE && ((Element)n).hasAttribute("id")) {
                Element e = (Element) dataBlocks.item(i);
                if(ids.contains(e.getAttribute("id"))) {
                    elements.add(e);
                }
            }
        }

        return elements;
    }
    
    public void buildDocument(String firstName, String lastName, LocalDate dob, String imagePath) throws TransformerException, IOException, SAXException {
        this.firstName.setTextContent(firstName);
        this.lastName.setTextContent(lastName);
        this.dob.setTextContent(dob.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        this.doi.setTextContent(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        this.picture.setAttribute("src", String.format("url(%s)",imagePath));

        String fileName = String.format("%s-%s.pdf", firstName, lastName);
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());

        OutputStream out = new FileOutputStream(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(baos);

        DOMSource dom = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();

        transformer.transform(dom, result);

        try {
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);

            Source src = new StreamSource(new ByteArrayInputStream(baos.toByteArray()));
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        } finally {
            out.close();
        }
    }
}
