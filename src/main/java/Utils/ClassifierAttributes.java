package Utils;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
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

}
