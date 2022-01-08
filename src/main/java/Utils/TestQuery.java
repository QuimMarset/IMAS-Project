package Utils;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Data
public class TestQuery implements Serializable {

    private final boolean randomMode;
    private boolean groundTruthAvailable;
    private int[] instanceIndices;
    private List<String> attributeNames;
    private int[] groundTruthValues;

    public TestQuery() {
        this.randomMode = true;
        this.groundTruthAvailable = false;
        this.instanceIndices = null;
        this.attributeNames = null;
    }

    public TestQuery(int[] instanceIndices, List<String> attributeNames, boolean groundTruthAvailable, int[] groundTruthValues) {
        this.randomMode = false;
        this.instanceIndices = instanceIndices;
        this.attributeNames = attributeNames;
        if (groundTruthAvailable) {
            this.groundTruthAvailable = groundTruthAvailable;
            this.groundTruthValues = groundTruthValues;
        }
    }

    @Override
    public String toString() {
        return "Instance indices (starting from 0): " + Arrays.toString(this.instanceIndices) + "\nAttribute names :"
                + attributeNames;
    }
}
