package Utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class TestQuery implements Serializable {

    private boolean randomMode;
    private int[] instanceIndices;
    private List<String> attributeNames;

    public TestQuery() {
        this.randomMode = true;
        this.instanceIndices = null;
        this.attributeNames = null;
    }

    public TestQuery(int[] instanceIndices, List<String> attributeNames) {
        this.randomMode = false;
        this.instanceIndices = instanceIndices;
        this.attributeNames = attributeNames;
    }

    public boolean isRandom() {
        return this.randomMode;
    }

    public int[] getInstanceIndices() {
        return instanceIndices;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public void setInstanceIndices(int[] instancesIndices) {
        this.instanceIndices = instancesIndices;
    }

    public void setAttributeNames(List<String> attributeNames) {
        this.attributeNames = attributeNames;
    }

    @Override
    public String toString() {
        return "Instance indices (starting from 0): " + Arrays.toString(this.instanceIndices) + "\nAttribute names :"
                + attributeNames;
    }
}
