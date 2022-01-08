package Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Predictions implements Serializable {

    private final double[] predictions;

    public Predictions(double[] predictions) {
        this.predictions = predictions;
    }

    private static String translateLabel(double label) {
        if (label == 0) {
            return "Normal";
        }
        else {
            // label == 1
            return "Altered";
        }
    }

    public List<String> getPredictions() {
        List<String> translatedPredictions = new ArrayList<>();
        for (double prediction : this.predictions) {
            translatedPredictions.add(translateLabel(prediction));
        }
        return translatedPredictions;
    }
}