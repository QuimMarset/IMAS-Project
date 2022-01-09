package Behaviours;

import Agents.DataManagerAgent;
import Behaviours.Enums.DataManagerAgentState;
import Utils.ClassifierInstances;
import Utils.Configuration;
import Utils.TestQuery;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import weka.core.Instances;

import javax.management.AttributeNotFoundException;
import java.io.IOException;

public class DataManagerBehaviour extends CyclicBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private final DataManagerAgent dataManagerAgent;
    private DataManagerAgentState state;
    private boolean classifiersCreated;
    private int numTrainedClassifiers;
    private int numClassifiers;
    private int numAskedClassifiers;
    private int numFinishedClassifiers;
    private ACLMessage messagePendingToReply;
    private Instances[] preprocessedTestInstances;

    public DataManagerBehaviour(DataManagerAgent dataManagerAgent) {
        super(dataManagerAgent);
        this.dataManagerAgent = dataManagerAgent;
        this.state = DataManagerAgentState.WaitingForSystemConfiguration;
        this.classifiersCreated = false;
        this.numTrainedClassifiers = 0;
    }

    @Override
    public void action() {
        if (this.state == DataManagerAgentState.WaitingForSystemConfiguration) {
            Configuration configuration = this.receiveSystemConfiguration();
            if (configuration != null) {
                // Configuration has been correctly received
                boolean correctlyInitialized = this.initializeDatasetManager(configuration);
                if (correctlyInitialized) {
                    this.numClassifiers = configuration.getNumClassifiers();
                    this.sendNumOfClassifiersToFinalClassifier();
                    this.state = DataManagerAgentState.WaitingForFinalClassifierAck;
                }
            }
        }

        else if (this.state == DataManagerAgentState.WaitingForFinalClassifierAck) {
            boolean informReceived = this.receiveFinalClassifierInform();
            if (informReceived) {
                // This happens when this agent has sent the number of classifiers that will predict the test query
                if (this.classifiersCreated) {
                    this.sendTestInstancesToClassifiers();
                    this.state = DataManagerAgentState.WaitingForClassifiersToPredict;
                }
                else {
                    // This happens when this agent has sent the number of created classifiers
                    this.state = DataManagerAgentState.CreateClassifiers;
                }
            }
        }

        else if (this.state == DataManagerAgentState.CreateClassifiers) {
            this.createClassifierAgents();
            this.dataManagerAgent.logClassifierAttributes();
            this.classifiersCreated = true;
            this.state = DataManagerAgentState.WaitingForClassifiersToTrain;
        }

        else if (this.state == DataManagerAgentState.WaitingForClassifiersToTrain) {
            this.waitForClassifiersToTrain();
            if (this.numTrainedClassifiers == this.numClassifiers) {
                this.logger.log(Logger.INFO, "All classifiers have end training");
                this.sendInformToUserAgent();
                this.state = DataManagerAgentState.WaitingForQueries;
            }
        }

        else if (this.state == DataManagerAgentState.WaitingForQueries) {
            TestQuery testQuery = this.receiveTestQuery();
            if (testQuery != null) {
                Instances testInstances = this.getTestInstances(testQuery);
                if (testInstances != null) {
                    // Test Instances have been retrieved without problems
                    this.preprocessTestInstances(testInstances);
                    if (this.numAskedClassifiers > 0) {
                        // At least one classifier can predict the received test query
                        this.sendNumberOfAskedClassifiersToFinalClassifier();
                        this.state = DataManagerAgentState.WaitingForFinalClassifierAck;
                    }
                    this.sendTestQueryResponseToUserAgent(testQuery);
                }
            }
        }

        else {
            /*
            Waiting for classifiers to end predicting (not returning the results, but as a way for this agent to
            know everything went good, and wait for new test queries)
            */
            this.waitForClassifiersToEndPredicting();
            if (this.numAskedClassifiers == this.numFinishedClassifiers) {
                this.logger.log(Logger.INFO, "The ordered classifiers have end predicting instances");
                this.state = DataManagerAgentState.WaitingForQueries;
            }
        }
    }

    private Configuration receiveSystemConfiguration() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("userAgent", AID.ISLOCALNAME));
        ACLMessage message = this.dataManagerAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            try {
                Configuration configuration = (Configuration) message.getContentObject();
                this.messagePendingToReply = message;
                this.logger.log(Logger.INFO, "System configuration received");
                return configuration;
            }
            catch (UnreadableException e) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The configuration file cannot be retrieved from the message");
                this.dataManagerAgent.send(reply);
            }
        }
        else {
            this.block();
        }
        return null;
    }

    private boolean initializeDatasetManager(Configuration configuration) {
        try {
            this.dataManagerAgent.initializeDatasetManager(configuration);
            return true;
        }
        catch (IndexOutOfBoundsException e) {
            ACLMessage reply = this.messagePendingToReply.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(e.getMessage());
            this.dataManagerAgent.send(reply);
            this.messagePendingToReply = null;
        }
        return false;
    }

    private void sendNumOfClassifiersToFinalClassifier() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("finalClassifierAgent", AID.ISLOCALNAME));
        message.setContent(String.valueOf(this.numClassifiers));
        this.dataManagerAgent.send(message);
    }

    private boolean receiveFinalClassifierInform() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("finalClassifierAgent", AID.ISLOCALNAME));
        ACLMessage message = this.dataManagerAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));
        if (message != null) {
            this.logger.log(Logger.INFO, "Inform from Final Classifier received");
            return true;
        }
        else {
            this.block();
        }
        return false;
    }

    private void createClassifierAgents() {
        for (int i = 1; i <= this.numClassifiers; ++i) {
            this.dataManagerAgent.createClassifierAgent(i);
            ClassifierInstances classifierInstances = this.dataManagerAgent.getClassifierInstances();

            ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
            message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            message.addReceiver(new AID("classifierAgent_" + i, AID.ISLOCALNAME));
            try {
                message.setContentObject(classifierInstances);
                this.dataManagerAgent.send(message);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void waitForClassifiersToTrain() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.dataManagerAgent.receive(performativeFilter);

        if (message != null && message.getSender().getLocalName().startsWith("classifierAgent_")) {
            ++this.numTrainedClassifiers;
        }
        else {
            this.block();
        }
    }

    private void sendInformToUserAgent() {
        ACLMessage reply = this.messagePendingToReply.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent("All classifiers have been trained, test queries can be performed");
        this.dataManagerAgent.send(reply);
        this.messagePendingToReply = null;
    }

    private TestQuery receiveTestQuery() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("userAgent", AID.ISLOCALNAME));
        ACLMessage message = this.dataManagerAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            try {
                this.messagePendingToReply = message;
                this.logger.log(Logger.INFO, "Test Query received");
                return (TestQuery) message.getContentObject();
            }
            catch (UnreadableException e) {
                ACLMessage reply = message.createReply();
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The test query cannot be retrieved");
            }
        }
        else {
            this.block();
        }
        return null;
    }

    private Instances getTestInstances(TestQuery testQuery) {
        try {
            this.logger.log(Logger.INFO, "Test instances from the query have been processed");
            return this.dataManagerAgent.getTestInstances(testQuery);
        }
        catch (IndexOutOfBoundsException | AttributeNotFoundException e) {
            ACLMessage reply = this.messagePendingToReply.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent(e.getMessage());
            this.dataManagerAgent.send(reply);
            this.messagePendingToReply = null;
        }
        return null;
    }

    private void preprocessTestInstances(Instances testInstances) {
        this.numAskedClassifiers = 0;
        this.numFinishedClassifiers = 0;
        if (this.preprocessedTestInstances == null) {
            this.preprocessedTestInstances = new Instances[this.numClassifiers];
        }

        for (int i = 0; i < this.numClassifiers; ++i) {
            if (this.dataManagerAgent.areTestInstancesPredictable(testInstances, i)) {
                this.numAskedClassifiers++;
                this.preprocessedTestInstances[i] = this.dataManagerAgent.getClassifierTestInstances(testInstances, i);
            }
            else {
                this.preprocessedTestInstances[i] = null;
            }
        }
    }

    private void sendNumberOfAskedClassifiersToFinalClassifier() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("finalClassifierAgent", AID.ISLOCALNAME));
        message.setContent(String.valueOf(this.numAskedClassifiers));
        this.dataManagerAgent.send(message);
    }

    private void sendTestInstancesToClassifiers() {
        for (int i = 0; i < this.preprocessedTestInstances.length; ++i) {
            if (this.preprocessedTestInstances[i] != null) {
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                message.addReceiver(new AID("classifierAgent_" + (i + 1), AID.ISLOCALNAME));
                try {
                    message.setContentObject(this.preprocessedTestInstances[i]);
                    this.dataManagerAgent.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendTestQueryResponseToUserAgent(TestQuery testQuery) {
        ACLMessage reply = this.messagePendingToReply.createReply();
        if (this.numAskedClassifiers > 0) {
            reply.setPerformative(ACLMessage.AGREE);
            try {
                reply.setContentObject(testQuery);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("No classifier was able to classify the passed instances");
        }
        this.dataManagerAgent.send(reply);
    }

    private void waitForClassifiersToEndPredicting() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.dataManagerAgent.receive(performativeFilter);
        if (message != null && message.getSender().getLocalName().startsWith("classifierAgent_")) {
            ++this.numFinishedClassifiers;
        }
        else {
            this.block();
        }
    }

}
