package Utils;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import java.util.ArrayList;

public class ClassifierAttributes extends InstancesUtils {

    ArrayList<Attribute> attributes;

    public ClassifierAttributes(Instances instances) {
        this.attributes = this.createAttributeArrayList(instances);
    }

    private boolean containsAttribute(Attribute attribute) {
        for (Attribute attributeClassifier : this.attributes) {
            if (attribute.name().equals(attributeClassifier.name())) {
                return true;
            }
        }
        return false;
    }

    private boolean isClassifiable(Instance instance) {
        for (int i = 0; i < instance.numAttributes(); ++i) {
            if (!containsAttribute(instance.attribute(i))) {
                return false;
            }
        }
        return true;
    }

    private int[] getClassifierAttributeIndices(Instances instances) {
        int[] attributeIndices = new int[this.attributes.size()];
        int i = 0;
        for (int j = 0; j < instances.numAttributes(); ++j) {
            if (this.attributes.contains(instances.attribute(j))) {
                attributeIndices[i] = j;
                ++i;
            }
        }
        return attributeIndices;
    }

    public Instances filterClassifiableInstances(Instances instances) {
        for (int i = 0; i < instances.numInstances(); ++i) {
            if (!this.isClassifiable(instances.get(i))) {
                instances.remove(i);
            }
        }

        int[] attributeIndices = this.getClassifierAttributeIndices(instances);
        return this.filterAttributes(instances, attributeIndices);
    }

}
