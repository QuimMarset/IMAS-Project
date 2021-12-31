package Utils;

import java.io.Serializable;
import java.util.List;

public class TestQuery implements Serializable {

    private boolean randomMode;
    private int[] instancesIndices;
    private List<String> attributesName;

    public TestQuery() {
        this.randomMode = true;
        this.instancesIndices = null;
        this.attributesName = null;
    }

    public TestQuery(int[] instancesIndices, List<String> attributesNames) {
        this.randomMode = false;
        this.instancesIndices = instancesIndices;
        this.attributesName = attributesNames;
    }

    public boolean isRandom() {
        return this.randomMode;
    }

    public int[] getInstancesIndices() {
        return instancesIndices;
    }

    public List<String> getAttributesName() {
        return attributesName;
    }
}
