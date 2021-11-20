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

public class Configuration implements Serializable {

    private int numClassifiers;
    private String trainDatasetPath;
    private String testDatasetPath;
    private int numTrainAttributes;
    private int numTestAttributes;
    private String testIndices;

    public Configuration(String filePath) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(filePath));
            document.getDocumentElement().normalize();

            Element rootElement = document.getDocumentElement();

            this.numClassifiers = Integer.parseInt(rootElement.getElementsByTagName("classifiers").item(0).getTextContent());
            this.trainDatasetPath = rootElement.getElementsByTagName("train-dataset").item(0).getTextContent();
            this.testDatasetPath = rootElement.getElementsByTagName("test-dataset").item(0).getTextContent();
            this.numTrainAttributes = Integer.parseInt(rootElement.getElementsByTagName("train-attributes").item(0).getTextContent());
            this.numTestAttributes = Integer.parseInt(rootElement.getElementsByTagName("test-attributes").item(0).getTextContent());
            this.testIndices = rootElement.getElementsByTagName("test-indices").item(0).getTextContent();
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

    public int getNumTestAttributes() {
        return this.numTestAttributes;
    }

    public String getTrainDatasetPath() {
        return this.trainDatasetPath;
    }

    public String getTestDatasetPath() {
        return this.testDatasetPath;
    }

    public String getTestIndices() {
        return this.testIndices;
    }

    public void setTestIndices(String testIndices) {
        this.testIndices = testIndices;
    }
}
