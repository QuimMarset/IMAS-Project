package Utils;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.ArrayList;

public abstract class InstancesUtils {

    protected Instances filterAttributes(Instances instances, int[] attributesToKeep) {
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

    protected ArrayList<Attribute> createAttributeArrayList(Instances dataset) {
        ArrayList<Attribute> attributeInfo = new ArrayList<>();
        for (int i = 0; i < dataset.numAttributes(); ++i) {
            attributeInfo.add(dataset.attribute(i));
        }
        return attributeInfo;
    }
}
