package Behaviours.Enums;

public enum ClassifierAgentState {
    PendingToTrain,
    WaitingForMetricsAck,
    Trained,
    WaitingForQueries,
    WaitingForPredictionsAck
}
