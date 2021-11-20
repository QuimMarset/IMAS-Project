package Utils;

import lombok.Data;

@Data
public class AuditDataRowWithPrediction extends AuditDataRow {
    private String predictedRisk;
}
