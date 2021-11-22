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
    private int numTrainInstances;
    private String testIndices;

    public Configuration(String filePath) {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(filePath));
            document.getDocumentElement().normalize();

            Element rootElement = document.getDocumentElement();

            this.numClassifiers = Integer.parseInt(getAttribute(rootElement, "classifiers"));
            this.trainDatasetPath = getAttribute(rootElement, "train_dataset");
            this.testDatasetPath = getAttribute(rootElement, "test_dataset");
            this.numTrainAttributes = Integer.parseInt(getAttribute(rootElement, "train_attributes"));
            this.numTestAttributes = Integer.parseInt(getAttribute(rootElement, "test_attributes"));
            this.numTrainInstances = Integer.parseInt(getAttribute(rootElement, "train_instances"));
            this.testIndices = getAttribute(rootElement, "test_indices");
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

    private String getAttribute(Element rootElement, String attributeName) {
        return rootElement.getElementsByTagName(attributeName).item(0).getTextContent();
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

    public int getNumTrainInstances() {
        return this.numTrainInstances;
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
