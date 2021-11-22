package Utils;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import java.util.*;

public class DatasetManager {

    private String trainDatasetPath;
    private String testDatasetPath;
    private int numClassifiers;
    private int numClassifierInstances;
    private int numClassifierAttributes;
    private int numAttributes;
    private Instances trainData;
    private List<int[]> classifierIndices = new ArrayList<>();


    public DatasetManager(Configuration configuration) {
        this.numClassifiers = configuration.getNumClassifiers();
        this.numClassifierAttributes = configuration.getNumTrainAttributes();
        this.numClassifierInstances = configuration.getNumTrainInstances();
        this.trainDatasetPath = configuration.getTrainDatasetPath();
        this.testDatasetPath = configuration.getTrainDatasetPath();
        readTrainDataset();
    }


    private void readTrainDataset() {
        ConverterUtils.DataSource source = null;
        try {
            source = new ConverterUtils.DataSource(this.trainDatasetPath);
            this.trainData = source.getDataSet();
            this.trainData.setClassIndex(numAttributes-1);
            this.numAttributes = this.trainData.numAttributes();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public int[] getAttributeIndices() {
        int[] selectedAttributeIndices = new int[this.numClassifierAttributes];

        Random random = new Random();
        Set<Integer> indicesSet = new HashSet<>();
        while (indicesSet.size() < this.numClassifierAttributes) {
            int index = random.nextInt(this.numAttributes);
            indicesSet.add(index);
        }

        int i = 0;
        for (int index : indicesSet) {
            selectedAttributeIndices[i] = index;
            ++i;
        }
        this.classifierIndices.add(selectedAttributeIndices);
        return selectedAttributeIndices;
    }


    public Instances getTrainInstances(int[] attributeIndices) {
        this.trainData.randomize(new Random());
        Instances trainInstances = new Instances(this.trainData, 0, this.numClassifierInstances);
        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(attributeIndices);
        removeFilter.setInvertSelection(true);
        try {
            removeFilter.setInputFormat(trainInstances);
            trainInstances = Filter.useFilter(trainInstances, removeFilter);
            return trainInstances;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
