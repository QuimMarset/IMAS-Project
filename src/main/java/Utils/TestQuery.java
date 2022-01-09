package Utils;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Data
public class TestQuery implements Serializable {

    private final boolean randomMode;
    private int[] instanceIndices;
    private List<String> attributeNames;
    private int[] groundTruthValues;

    public TestQuery() {
        this.randomMode = true;
        this.instanceIndices = null;
        this.attributeNames = null;
    }

    public TestQuery(int[] instanceIndices, List<String> attributeNames, int[] groundTruthValues) {
        this.randomMode = false;
        this.instanceIndices = instanceIndices;
        this.attributeNames = attributeNames;
        this.groundTruthValues = groundTruthValues;
    }

    @Override
    public String toString() {
        return "Instance indices (starting from 0): " + Arrays.toString(this.instanceIndices) + "\nAttribute names :"
                + attributeNames;
    }

    public void setGroundTruthValues(int[] groundTruthValues) {
        if (this.groundTruthValues == null) {
            this.groundTruthValues = groundTruthValues;
        }
    }

    public boolean isGroundTruthAvailable() {
        return this.groundTruthValues != null;
    }
}
