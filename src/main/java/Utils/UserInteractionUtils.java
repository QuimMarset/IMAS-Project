package Utils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.util.List;

public final class UserInteractionUtils {

    private UserInteractionUtils() {}

    public static Configuration getSystemConfiguration() {
        int numClassifiers, numTrainInstances, numTrainAttributes, validationPercentage;
        String trainDatasetPath, testDatasetPath;

        while (true) {
            String filePath = getXMLFile("./config/config.xml", "Select the Configuration file to start the system");
            XMLParser configurationParser = new XMLParser(filePath);
            try {
                numClassifiers = configurationParser.getNumericAttribute("classifiers");
                trainDatasetPath = configurationParser.getPathAttribute("train_dataset");
                testDatasetPath = configurationParser.getPathAttribute("test_dataset");
                numTrainInstances = configurationParser.getNumericAttribute("train_instances");
                numTrainAttributes = configurationParser.getNumericAttribute("train_attributes");
                validationPercentage = configurationParser.getNumericAttribute("validation_percentage");
                return new Configuration(numClassifiers, trainDatasetPath, testDatasetPath, numTrainAttributes,
                        numTrainInstances, validationPercentage);
            }
            catch (NullPointerException | NumberFormatException | InvalidPathException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static TestQuery getTestQuery() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Select querying mode:");
        System.out.println("1. Load an xml containing the query");
        System.out.println("2. Let the system randomly select 15 instances with 20 attributes each");
        System.out.println("Choice (type 1 or 2): ");

        int queryMode = 0;
        TestQuery testQuery = null;

        try {
            while (queryMode != 1 && queryMode != 2) {
                queryMode = Integer.parseInt(br.readLine());

                if (queryMode == 1) {
                    testQuery = getTestQueryFromFile();
                }
                else if (queryMode == 2) {
                    testQuery = new TestQuery();
                }
                else {
                    System.out.println("Wrong option. Try again!");
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return testQuery;
    }

    private static TestQuery getTestQueryFromFile() {
        int[] instanceIndices;
        List<String> attributesName;
        boolean groundTruthAvailable;
        int[] groundTruthValues = null;

        while (true) {
            String filePath = getXMLFile("./config/testQuery.xml", "Select the Test Query file to test");
            XMLParser testQueryParser = new XMLParser(filePath);
            try {
                instanceIndices = testQueryParser.getIntListAttribute("instances");
                attributesName = testQueryParser.getStringListAttribute("attributes");
                if (testQueryParser.existsAttribute("groundTruthValues")) {
                    groundTruthValues = testQueryParser.getIntListAttribute("groundTruthValues");
                }
                return new TestQuery(instanceIndices, attributesName, groundTruthValues);
            }
            catch (NumberFormatException | NullPointerException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String getXMLFile(String defaultPath, String dialogTitle) {
        // Set to the default configuration file in the config folder
        String filePath = defaultPath;

        JFileChooser fileChooser = new JFileChooser(new File("./"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setDialogTitle(dialogTitle);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                final String name = f.getName();
                return name.endsWith(".xml");
            }

            @Override
            public String getDescription() {
                return "*.xml";
            }
        });

        int status = fileChooser.showOpenDialog(null);
        if (status == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (file != null) {
                filePath = file.getAbsolutePath();
            }
        }

        return filePath;
    }
}
