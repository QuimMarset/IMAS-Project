package Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;

public class Configuration implements Serializable {

    private int numClassifiers;
    private String datasetPath;
    private int numTrainAttributes;

    public Configuration(String filePath) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(filePath));
            document.getDocumentElement().normalize();

            Element rootElement = document.getDocumentElement();

            this.numClassifiers = Integer.parseInt(rootElement.getElementsByTagName("classifiers").item(0).getTextContent());
            this.datasetPath = rootElement.getElementsByTagName("dataset").item(0).getTextContent();
            this.numTrainAttributes = Integer.parseInt(rootElement.getElementsByTagName("attributes").item(0).getTextContent());

        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public int getNumClassifiers() {
        return this.numClassifiers;
    }

    public int getNumTrainAttributes() {
        return this.numTrainAttributes;
    }

    public String getDatasetPath() {
        return this.datasetPath;
    }

}
