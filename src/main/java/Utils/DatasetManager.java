package Utils;

import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import javax.management.AttributeNotFoundException;
import java.util.*;

public class DatasetManager {

    private final Instances trainData;
    private final Instances testData;

    private final int numNoClassAttributes;
    private final int numTrainInstances;
    private final int numTestInstances;

    private final ArrayList<Attribute> attributeInfo;

    private final Randomizer randomizer;

    public DatasetManager(String trainDatasetPath, String testDatasetPath) {
        this.randomizer = new Randomizer();
        this.trainData = readDataset(trainDatasetPath);
        this.testData = readDataset(testDatasetPath);
        this.numNoClassAttributes = this.trainData.numAttributes()-1;
        this.numTrainInstances = this.trainData.numInstances();
        this.numTestInstances = this.testData.numInstances();
        this.attributeInfo = this.createAttributeArrayList(this.trainData);
    }

    private ArrayList<Attribute> createAttributeArrayList(Instances dataset) {
        ArrayList<Attribute> attributeInfo = new ArrayList<>();
        for (int i = 0; i < dataset.numAttributes(); ++i) {
            attributeInfo.add(dataset.attribute(i));
        }
        return attributeInfo;
    }

    private Instances readDataset(String datasetPath) {
        Instances data = null;
        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(datasetPath);
            data = source.getDataSet();
            data.setClassIndex(data.numAttributes()-1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    private Instances filterAttributes(Instances instances, int[] attributesToKeep) {
        // Add the class attribute when filtering
        int[] attributesToKeepPlusClass = new int[attributesToKeep.length + 1];
        System.arraycopy(attributesToKeep, 0, attributesToKeepPlusClass, 0, attributesToKeep.length);
        attributesToKeepPlusClass[attributesToKeepPlusClass.length-1] = instances.classIndex();

        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(attributesToKeepPlusClass);
        removeFilter.setInvertSelection(true);
        try {
            removeFilter.setInputFormat(instances);
            instances = Filter.useFilter(instances, removeFilter);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return instances;
    }

    public ClassifierInstances getClassifierInstances(int numClassifierInstances, int validationPercentage,
                                                      int numClassifierAttributes) {
        int[] attributeIndices = this.randomizer.getRandomIndices(this.numNoClassAttributes, numClassifierAttributes);
        int[] instancesIndices = this.randomizer.getRandomIndices(this.numTrainInstances, numClassifierInstances);

        int numTrainInstances = (int) ((1 - validationPercentage/100.0)*numClassifierInstances);
        int numValInstances = numClassifierInstances - numTrainInstances;

        Instances trainInstances = new Instances("trainInstances", this.attributeInfo, numTrainInstances);
        Instances valInstances = new Instances("trainInstances", this.attributeInfo, numValInstances);

        for (int i = 0; i < numClassifierInstances; ++i) {
            int trainDatasetIndex = instancesIndices[i];
            if (i < numTrainInstances) {
                trainInstances.add(this.trainData.get(trainDatasetIndex));
            }
            else {
                valInstances.add(this.trainData.get(trainDatasetIndex));
            }
        }

        trainInstances.setClassIndex(this.numNoClassAttributes);
        valInstances.setClassIndex(this.numNoClassAttributes);

        trainInstances = this.filterAttributes(trainInstances, attributeIndices);
        valInstances = this.filterAttributes(valInstances, attributeIndices);

        return new ClassifierInstances(trainInstances, valInstances);
    }

    public Instances getTestInstancesRandom(int numTestInstances, int numTestAttributes) {
        int[] attributeIndices = this.randomizer.getRandomIndices(this.numNoClassAttributes, numTestAttributes);
        int[] instancesIndices = this.randomizer.getRandomIndices(this.numTestInstances, numTestInstances);
        Instances testInstances = new Instances("queryTestInstances", this.attributeInfo, instancesIndices.length);
        for (int instancesIndex : instancesIndices) {
            testInstances.add(this.testData.get(instancesIndex));
        }
        testInstances.setClassIndex(this.numNoClassAttributes);
        return this.filterAttributes(testInstances, attributeIndices);
    }

    public Instances getTestInstances(int[] instanceIndices, List<String> attributesName)
            throws IndexOutOfBoundsException, AttributeNotFoundException {

        if (instanceIndices.length > this.numTestInstances) {
            throw new IndexOutOfBoundsException("Trying to fetch more instances than the test dataset contains");
        }

        Instances testInstances = new Instances("queryTestInstances", this.attributeInfo, instanceIndices.length);

        for (int index: instanceIndices) {
            if (index >= this.numTestInstances) {
                throw new IndexOutOfBoundsException("Index " + index + " is out of bounds. Test dataset has "
                        + this.numTestInstances + " instances");
            }
            testInstances.add(this.testData.get(index));
        }

        if (attributesName.size() > this.numNoClassAttributes) {
            throw new IndexOutOfBoundsException("Trying to use more attributes than the test dataset contains");
        }
        int[] attributeIndices = new int[attributesName.size()];
        for (int i = 0; i < attributesName.size(); ++i) {
            attributeIndices[i] = this.getAttributeIndex(attributesName.get(i));
            if (attributeIndices[i] == this.testData.classIndex()) {
                throw new IndexOutOfBoundsException("Trying to use the class attribute as independent attribute");
            }
        }

        testInstances.setClassIndex(this.numNoClassAttributes);
        return this.filterAttributes(testInstances, attributeIndices);
    }

    private int getAttributeIndex(String attributeName) throws AttributeNotFoundException {
        Attribute attribute = this.trainData.attribute(attributeName);
        if (attribute != null) {
            return attribute.index();
        }
        else {
            throw new AttributeNotFoundException("Attribute " + attributeName + " does not belong to the dataset");
        }
    }

    public int getNumTrainInstances() {
        return this.numTrainInstances;
    }

    public int getNumNoClassAttributes() {
        return this.numNoClassAttributes;
    }
}
