package Behaviours;

import Agents.FinalClassifierAgent;
import Behaviours.Enums.FinalClassifierAgentState;
import Utils.Predictions;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import javafx.util.Pair;
import weka.core.Debug;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class FinalClassifierBehaviour extends CyclicBehaviour {

    private static final DecimalFormat df = new DecimalFormat("0.000");

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private final FinalClassifierAgent finalclassifierAgent;
    private FinalClassifierAgentState finalClassifierAgentState;
    private int numClassifiers;
    private int numClassifying;
    private ACLMessage pendingToReply;
    private int numReceived;
    List<Double> f1Scores;
    List<Pair<Integer, double[]>> receivedTestPredictions;
    double[] combinedPredictions;

    public FinalClassifierBehaviour(FinalClassifierAgent finalclassifierAgent) {
        super(finalclassifierAgent);
        this.finalclassifierAgent = finalclassifierAgent;
        this.finalClassifierAgentState = FinalClassifierAgentState.ReceiveNumOfClassifiers;
        this.f1Scores = new ArrayList<>();
    }

    //@Override
    public void action() {
        if (this.finalClassifierAgentState == FinalClassifierAgentState.ReceiveNumOfClassifiers) {
            Integer numClassifiers = this.receiveNumberFromDataManagerAgent();
            if (numClassifiers != null) {
                // The value has been received
                this.numClassifiers = numClassifiers;
                this.logger.log(Logger.INFO, "Number of classifiers received: " + this.numClassifiers);
                this.sendInformToLastReceivedMessage("Received number of created classifiers");
                this.finalClassifierAgentState = FinalClassifierAgentState.ReceiveMetrics;
            }
        }

        else if (this.finalClassifierAgentState == FinalClassifierAgentState.ReceiveMetrics) {
            boolean received = this.receiveMetric();
            if (received) {
                this.sendInformToLastReceivedMessage("Received F1-Score");
            }
            if (this.numReceived == this.numClassifiers) {
                this.finalClassifierAgentState = FinalClassifierAgentState.ReceiveNumOfClassifying;
            }
        }

        else if (this.finalClassifierAgentState == FinalClassifierAgentState.ReceiveNumOfClassifying) {
            Integer numClassifying = this.receiveNumberFromDataManagerAgent();
            if (numClassifying != null) {
                // The value has been received
                this.numClassifying = numClassifying;
                this.logger.log(Logger.INFO, "Number of classifiers that will predict the current test query received: "
                    + this.numClassifying);
                this.sendInformToLastReceivedMessage("Received number of classifiers to predict test query");
                this.finalClassifierAgentState = FinalClassifierAgentState.ReceivePredictions;
                // Set to 0 to know when to stop waiting for predictions
                this.numReceived = 0;
                this.receivedTestPredictions = new ArrayList<>();
            }
        }

        else if (this.finalClassifierAgentState == FinalClassifierAgentState.ReceivePredictions) {
            boolean received = this.receivePredictions();
            if (received) {
                this.sendInformToLastReceivedMessage("Received predictions");
            }
            if (this.numReceived == this.numClassifying) {
                this.combinePredictions();
                this.finalClassifierAgentState = FinalClassifierAgentState.ReturnCombinedPredictions;
            }
        }

        else {
            // Return the combined predictions to the User Agent
            this.sendPredictionsToUserAgent();
            this.finalClassifierAgentState = FinalClassifierAgentState.ReceiveNumOfClassifying;
        }
    }

    private Integer receiveNumberFromDataManagerAgent() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.finalclassifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            this.pendingToReply = message;
            return Integer.parseInt(message.getContent());
        }
        else {
            this.block();
        }
        return null;
    }

    private void sendInformToLastReceivedMessage(String content) {
        ACLMessage reply = this.pendingToReply.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(content);
        this.finalclassifierAgent.send(reply);
    }

    private boolean receiveMetric() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.finalclassifierAgent.receive(performativeFilter);

        if (message != null && message.getSender().getLocalName().startsWith("classifierAgent_")) {
            double f1Score = Double.parseDouble(message.getContent());
            this.f1Scores.add(f1Score);
            this.numReceived++;
            this.pendingToReply = message;
            this.logger.log(Logger.INFO, "F1-score from " + message.getSender().getLocalName() + " received: " +
                    df.format(f1Score));
            return true;
        }
        else {
            this.block();
        }
        return false;
    }

    private boolean receivePredictions() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage message = this.finalclassifierAgent.receive(performativeFilter);

        if (message != null && message.getSender().getLocalName().startsWith("classifierAgent_")) {
            try {
                String classifierName = message.getSender().getLocalName();
                Integer classifierIndex = Integer.parseInt(classifierName.split("_")[1]);
                double[] predictions = (double[]) message.getContentObject();
                this.receivedTestPredictions.add(new Pair<>(classifierIndex, predictions));
                this.numReceived++;
                this.pendingToReply = message;
                this.logger.log(Logger.INFO, "Predictions from " + classifierName + " received");
                return true;
            }
            catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void combinePredictions() {
        int numTestInstances = this.receivedTestPredictions.get(0).getValue().length;
        this.combinedPredictions = new double[numTestInstances];
        double normalizationFactor = 0;

        for (Pair<Integer, double[]> predictions : this.receivedTestPredictions) {
            Integer classifierIndex = predictions.getKey();
            double[] predictedLabels = predictions.getValue();
            double f1Score = this.f1Scores.get(classifierIndex-1);

            normalizationFactor += f1Score;

            for (int i = 0; i < numTestInstances; ++i) {
                this.combinedPredictions[i] += predictedLabels[i]*f1Score;
            }
        }

        for (int i = 0; i < this.combinedPredictions.length; ++i) {
            double value = this.combinedPredictions[i];
            // Round to either 0 or 1 depending on to which is near
            this.combinedPredictions[i] = Math.round(value/(normalizationFactor + 1e-8));
        }
    }

    private void sendPredictionsToUserAgent() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("userAgent", AID.ISLOCALNAME));

        try {
            message.setContentObject(new Predictions(this.combinedPredictions));
            this.finalclassifierAgent.send(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}