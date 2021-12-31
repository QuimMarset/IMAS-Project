package Utils;

import java.io.Serializable;

public class Configuration implements Serializable {

    private int numClassifiers;
    private String trainDatasetPath;
    private String testDatasetPath;
    // Number of attributes the classifiers will use
    private int numTrainAttributes;
    // Number of instances the classifiers will be trained with
    private int numTrainInstances;
    private int validationPercentage;

    public Configuration(int numClassifiers, String trainDatasetPath, String testDatasetPath, int numTrainAttributes,
                         int numTrainInstances, int validationPercentage) {
        this.numClassifiers = numClassifiers;
        this.trainDatasetPath = trainDatasetPath;
        this.testDatasetPath = testDatasetPath;
        this.numTrainAttributes = numTrainAttributes;
        this.numTrainInstances = numTrainInstances;
        this.validationPercentage = validationPercentage;
    }

    public int getNumClassifiers() {
        return this.numClassifiers;
    }

    public int getNumTrainAttributes() {
        return this.numTrainAttributes;
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

    public int getValidationPercentage() {
        return this.validationPercentage;
    }

}
