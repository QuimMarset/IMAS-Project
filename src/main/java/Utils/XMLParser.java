package Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

public class XMLParser {

    Element rootElement;

    public XMLParser(String filePath) {
        this.getRootElement(filePath);
    }

    private void getRootElement(String filePath) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new File(filePath));
            document.getDocumentElement().normalize();

            this.rootElement = document.getDocumentElement();

        }
        catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println("The configuration file " + filePath + " has not been parsed correctly");
            this.rootElement = null;
        }
    }

    private Node getAttributeNode(String attributeName) throws NullPointerException {
        if (this.rootElement == null) {
            throw new NullPointerException("The root node of the parsed XML file is null");
        }
        Node attributeNode = this.rootElement.getElementsByTagName(attributeName).item(0);
        if (attributeNode == null) {
            throw new NullPointerException("Attribute " + attributeName + " does not exist in the parsed XML file");
        }
        return attributeNode;
    }

    public int getNumericAttribute(String attributeName) throws NullPointerException, NumberFormatException {
        Node attributeNode = this.getAttributeNode(attributeName);
        try {
            int attributeNumericValue = Integer.parseInt(attributeNode.getTextContent());
            return attributeNumericValue;
        }
        catch (NumberFormatException e) {
            throw new NumberFormatException("Attribute " + attributeName + " is not a valid number: " +
                    attributeNode.getTextContent());
        }
    }

    public String getStringAttribute(String attributeName) {
        Node attributeNode = this.getAttributeNode(attributeName);
        return attributeNode.getTextContent();
    }

    public String getPathAttribute(String attributeName) throws InvalidPathException {
        Node attributeNode = this.getAttributeNode(attributeName);
        String filePath = attributeNode.getTextContent();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new InvalidPathException(filePath, "Attribute " + attributeName + " does not contain a valid path");
        }
        return filePath;
    }

    public int[] getIntListAttribute(String attributeName) throws NumberFormatException {
        String[] elements = getStringAttribute(attributeName).split("\n");
        int[] intElements = new int[elements.length - 2];
        for (int i = 1; i < elements.length - 1; ++i) {
            String trimmedElement = elements[i].trim();
            try {
                intElements[i-1] = Integer.parseInt(trimmedElement);
            }
            catch (NumberFormatException e) {
                throw new NumberFormatException("Attribute " + attributeName + " contains a invalid number: "
                        + trimmedElement);
            }
        }
        return intElements;
    }

    public List<String> getStringListAttribute(String attributeName) {
        String[] elements = getStringAttribute(attributeName).split("\n");
        List<String> parsedList = new ArrayList<>();
        for (String element : elements) {
            String trimmedElement = element.trim();
            if (trimmedElement.length() > 0) {
                parsedList.add(trimmedElement);
            }
        }
        return parsedList;
    }
}
