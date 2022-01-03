package Utils;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.util.ArrayList;
import java.util.List;

public class ClassifierAttributes {

    List<Attribute> attributes;

    public ClassifierAttributes(Instances instances) {
        this.generateAttributeList(instances);
    }

    private void generateAttributeList(Instances instances) {
        this.attributes = new ArrayList<>();
        for (int i = 0; i < instances.numAttributes(); ++i) {
            this.attributes.add(instances.attribute(i));
        }
    }

    private boolean containsAttribute(Attribute attribute, Instances instances) {
        for (int i = 0; i < instances.numAttributes(); ++i) {
            if (attribute.name().equals(instances.attribute(i).name())) {
                return true;
            }
        }
        return false;
    }

    public boolean areInstancesClassifiable(Instances instances) {
        for (Attribute attribute : this.attributes) {
            if (!this.containsAttribute(attribute, instances)) {
                return false;
            }
        }
        return true;
    }

    private int[] getAttributesToKeep(Instances instances) {
        int[] attributeIndices = new int[attributes.size()];
        for (int i = 0; i < attributes.size(); ++i) {
            for (int j = 0; j < instances.numAttributes(); ++j) {
                if (attributes.get(i).name().equals(instances.attribute(j).name())) {
                    attributeIndices[i] = j;
                    break;
                }
            }
        }
        return attributeIndices;
    }

    public Instances filterClassifiableInstances(Instances instances) {
        int[] attributeIndices = this.getAttributesToKeep(instances);

        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(attributeIndices);
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
}
