package Behaviours;

import Agents.ClassifierAgent;
import Behaviours.Enums.ClassifierAgentState;
import Utils.ClassifierInstances;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import weka.core.Instances;
import java.io.IOException;


public class ClassifierBehaviour extends CyclicBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private final ClassifierAgent classifierAgent;
    private ClassifierAgentState state;
    private ACLMessage messageToReply;

    public ClassifierBehaviour(ClassifierAgent classifierAgent) {
        this.classifierAgent = classifierAgent;
        this.state = ClassifierAgentState.PendingToTrain;
    }

    @Override
    public void action() {
        if (this.state == ClassifierAgentState.PendingToTrain) {
            ClassifierInstances classifierInstances = this.receiveTrainValInstances();
            if (classifierInstances != null) {
                this.logger.log(Logger.INFO, this.classifierAgent.getLocalName() +
                        " has received the train/validation instances");
                double f1Score = this.trainModel(classifierInstances);
                this.sendValidationMetric(f1Score);
                this.state = ClassifierAgentState.WaitingForMetricsAck;
            }
        }

        else if (this.state == ClassifierAgentState.WaitingForMetricsAck) {
            boolean informReceived = this.waitInformFromFinalClassifier();
            if (informReceived) {
                this.state = ClassifierAgentState.Trained;
            }
        }

        else if (this.state == ClassifierAgentState.Trained) {
            this.sendInformToDataManager("Classifier " + this.classifierAgent.getLocalName() + " has finished training");
            this.state = ClassifierAgentState.WaitingForQueries;
        }

        else if (this.state == ClassifierAgentState.WaitingForQueries) {
            Instances testInstances = this.receiveTestQueries();
            if (testInstances != null) {
                this.logger.log(Logger.INFO, "Test instances received");
                double[] predictions = this.classifyTestInstances(testInstances);
                this.sendTestPredictionsToFinalClassifier(predictions);
                this.state = ClassifierAgentState.WaitingForPredictionsAck;
            }
        }

        else {
            boolean informReceived = this.waitInformFromFinalClassifier();
            if (informReceived) {
                this.sendInformToDataManager("Classifier " + this.classifierAgent.getLocalName() + " has classified" +
                        "the test instances");
                this.state = ClassifierAgentState.WaitingForQueries;
            }
        }
    }

    private ClassifierInstances receiveTrainValInstances() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.classifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            try {
                this.messageToReply = message;
                return (ClassifierInstances) message.getContentObject();
            }
            catch (UnreadableException e) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The train and validation instances cannot be retrieved");
                this.classifierAgent.send(reply);
            }
        }
        else {
            this.block();
        }
        return null;
    }

    private double trainModel(ClassifierInstances classifierInstances) {
        return this.classifierAgent.trainModel(classifierInstances.getTrainInstances(),
                classifierInstances.getValidaitonInstances());
    }

    private void sendValidationMetric(double f1Score) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("finalClassifierAgent", AID.ISLOCALNAME));
        message.setContent(String.valueOf(f1Score));
        this.classifierAgent.send(message);
    }

    private boolean waitInformFromFinalClassifier() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("finalClassifierAgent",
                AID.ISLOCALNAME));
        ACLMessage message = this.classifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            this.logger.log(Logger.INFO, "Inform from Final Classifier received");
            return true;
        }
        else {
            this.block();
        }
        return false;
    }

    private void sendInformToDataManager(String messageContent) {
        ACLMessage reply = this.messageToReply.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(messageContent);
        this.classifierAgent.send(reply);
        this.messageToReply = null;
    }

    private Instances receiveTestQueries() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.classifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            try {
                Instances testInstances = (Instances) message.getContentObject();
                this.messageToReply = message;
                return testInstances;
            }
            catch (UnreadableException e) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("Test instances cannot be retrieved");
                this.classifierAgent.send(reply);
            }
        }
        else {
            this.block();
        }

        return null;
    }

    private double[] classifyTestInstances(Instances testInstances) {
        return this.classifierAgent.classifyTestInstances(testInstances);
    }

    private void sendTestPredictionsToFinalClassifier(double[] testPredictions) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("finalClassifierAgent", AID.ISLOCALNAME));
        try {
            message.setContentObject(testPredictions);
            this.classifierAgent.send(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}
