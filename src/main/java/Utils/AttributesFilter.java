package Utils;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.ArrayList;

public abstract class AttributesFilter {

    protected Instances filterAttributes(Instances instances, int[] attributesToKeep) {
        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(attributesToKeep);
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

    protected ArrayList<Attribute> createAttributeList(Instances dataset) {
        ArrayList<Attribute> attributeInfo = new ArrayList<>();
        for (int i = 0; i < dataset.numAttributes(); ++i) {
            attributeInfo.add(dataset.attribute(i));
        }
        return attributeInfo;
    }
}
