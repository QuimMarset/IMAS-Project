package Utils;

import jade.util.Logger;

import java.util.logging.Level;

public class PredictionEvaluatorUtils {

    private static final Logger logger = Logger.getMyLogger("PredictionEvaluatorUtils");

    public static void evaluateAndPrintPredictionResults(AuditDataRowWithPrediction[] petitionResults)
    {
        int truePositives = 0;
        int falsePositives = 0;
        int trueNegatives = 0;
        int falseNegatives = 0;
        for (AuditDataRowWithPrediction result : petitionResults)
        {
            if (result.getRisk() == result.getPredictedRisk() && result.getRisk() == RecordType.NORMAL.getType())
            {
                ++truePositives;
            }
            else if (result.getRisk() == result.getPredictedRisk() && result.getRisk() == RecordType.ALTERED.getType())
            {
                ++trueNegatives;
            }
            else if (result.getRisk() != result.getPredictedRisk() && result.getPredictedRisk() == RecordType.NORMAL.getType())
            {
                ++falsePositives;
            }
            else
            {
                ++falseNegatives;
            }
        }
        float accuracy = (truePositives + trueNegatives) / (truePositives + trueNegatives + falsePositives + falseNegatives);
        float precision = truePositives / (truePositives + falsePositives);
        float recall = truePositives / (truePositives + falseNegatives);
        float f1Score = 2 * (recall * precision) / (recall + precision);
        logger.log(Level.INFO, "Confusion matrix evaluating performance:");
        logger.log(Level.INFO, "(TruePositives) " + truePositives + "      (FalseNegatives) " + falseNegatives);
        logger.log(Level.INFO, "(FalsePositives) " + falsePositives + "      (TrueNegatives) " + trueNegatives);

        logger.log(Level.INFO, "\nAccuracy: " + accuracy);
        logger.log(Level.INFO, "Precision: " + precision);
        logger.log(Level.INFO, "Recall: " + recall);
        logger.log(Level.INFO, "F1 Score: " + f1Score);
        return;
    }
}
