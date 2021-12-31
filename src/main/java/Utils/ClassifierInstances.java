package Utils;

import weka.core.Instances;

import java.io.Serializable;

public class ClassifierInstances implements Serializable {

    private Instances trainInstances;
    private Instances validaitonInstances;

    public ClassifierInstances(Instances trainInstances, Instances validationInstances) {
        this.trainInstances = trainInstances;
        this.validaitonInstances = validationInstances;
    }

    public Instances getTrainInstances() {
        return trainInstances;
    }

    public Instances getValidaitonInstances() {
        return validaitonInstances;
    }
}
