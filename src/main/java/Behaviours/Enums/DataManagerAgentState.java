package Behaviours.Enums;

public enum DataManagerAgentState {
    WaitingForSystemConfiguration,
    WaitingForFinalClassifierAck,
    CreateClassifiers,
    WaitingForClassifiersToTrain,
    WaitingForQueries,
    WaitingForClassifiersToPredict
}