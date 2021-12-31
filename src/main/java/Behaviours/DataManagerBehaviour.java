package Behaviours;

import Agents.DataManagerAgent;
import Utils.ClassifierInstances;
import Utils.Configuration;
import Utils.TestQuery;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import weka.core.Instances;

import javax.management.AttributeNotFoundException;
import java.io.IOException;


enum DataManagerAgentState {
    Idle,
    WaitingForTrain,
    WaitingForQueries,
}

public class DataManagerBehaviour extends CyclicBehaviour {

    private final DataManagerAgent dataManagerAgent;
    private DataManagerAgentState dataManagerAgentState;
    private int numTrainedClassifiers = 0;
    private int numClassifiers;

    private int numAskedClassifiers;
    private int numClassifiersFinished;

    private ACLMessage messagePendingToReply;

    public DataManagerBehaviour(DataManagerAgent dataManagerAgent) {
        super(dataManagerAgent);
        this.dataManagerAgent = dataManagerAgent;
        this.dataManagerAgentState = DataManagerAgentState.Idle;
    }

    @Override
    public void action() {
        if (this.dataManagerAgentState == DataManagerAgentState.Idle) {
            this.waitForSystemConfiguration();
        }
        else if (this.dataManagerAgentState == DataManagerAgentState.WaitingForTrain) {
            this.waitForClassifiersToTrain();
        }
        else if (this.dataManagerAgentState == DataManagerAgentState.WaitingForQueries) {
            this.waitForTestQueriesToClassify();
        }
        else {
            // Waiting for query answering
            this.waitForQueryAnswers();
        }
    }

    private void waitForSystemConfiguration() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("userAgent", AID.ISLOCALNAME));

        ACLMessage message = this.dataManagerAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            ACLMessage reply = message.createReply();

            try {
                Configuration configuration = (Configuration) message.getContentObject();
                this.numClassifiers = configuration.getNumClassifiers();
                this.dataManagerAgent.initializeDatasetManager(configuration);
                this.createClassifierAgents();

                this.dataManagerAgentState = DataManagerAgentState.WaitingForTrain;

                reply.setPerformative(ACLMessage.AGREE);
                reply.setContent("The classifiers have been creating. Wait for them to train");

                // Store the message to later reply with the INFORM performative once the classifiers are trained
                this.messagePendingToReply = message;
            }
            catch (UnreadableException e) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The configuration file cannot be retrieved from the message");
            }
            catch (IndexOutOfBoundsException e) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("There is a problem with the configuration settings to train the classifiers");
            }

            this.dataManagerAgent.send(reply);
        }
        else {
            this.block();
        }
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

            if (this.numClassifiers == this.numTrainedClassifiers) {

                ACLMessage reply = this.messagePendingToReply.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("All classifiers have been trained, test queries can be performed");
                this.dataManagerAgent.send(reply);

                this.dataManagerAgentState = DataManagerAgentState.WaitingForQueries;
            }
        }
        else {
            this.block();
        }
    }

    private boolean sendTestInstancesToClassifiers(TestQuery testQuery)
            throws IndexOutOfBoundsException, AttributeNotFoundException {

        this.numAskedClassifiers = 0;
        Instances testInstances = this.dataManagerAgent.getTestInstances(testQuery);

        for (int i = 0; i < this.numClassifiers; ++i) {

            if (this.dataManagerAgent.areTestInstancesPredictable(testInstances, i)) {
                this.numAskedClassifiers++;
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                message.addReceiver(new AID("classifierAgent_" + (i+1), AID.ISLOCALNAME));

                try {
                    message.setContentObject(testInstances);
                    this.dataManagerAgent.send(message);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.numAskedClassifiers == 0) {
            return false;
        }
        return true;
    }

    private void waitForTestQueriesToClassify() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("userAgent", AID.ISLOCALNAME));
        ACLMessage message = this.dataManagerAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            ACLMessage reply = message.createReply();
            try {
                TestQuery testQuery = (TestQuery) message.getContentObject();

                boolean haveBeenSent = this.sendTestInstancesToClassifiers(testQuery);

                if (haveBeenSent) {
                    reply.setPerformative(ACLMessage.AGREE);
                    if (testQuery.isRandom()) {
                        reply.setContent("");
                        /*reply.setContent("The random test queries will be processed\n Instances: " +
                                testQuery.getInstancesIndices().toString() + "Attributes: " +
                                testQuery.getAttributesName().toString());*/
                    }
                    else {
                        reply.setContent("The test queries will be processed");
                    }
                }
                else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("No classifier was able to classify the passed instances");
                }
            }
            catch (UnreadableException e) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The test queries cannot be read");
            }
            catch (IndexOutOfBoundsException e) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The test queries contain invalid instance indices");
            }
            catch (AttributeNotFoundException e) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The test queries contain invalid attributes");
            }
            this.dataManagerAgent.send(reply);
        }
        else {
            this.block();
        }
    }

    private void waitForQueryAnswers() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.dataManagerAgent.receive(performativeFilter);

        if (message != null && message.getSender().getLocalName().startsWith("classifierAgent_")) {
            ++this.numClassifiersFinished;

            if (this.numAskedClassifiers == this.numClassifiersFinished) {

                ACLMessage reply = this.messagePendingToReply.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent("All possible classifiers have classified the test instances. Wait for the results");
                this.dataManagerAgent.send(reply);

                this.dataManagerAgentState = DataManagerAgentState.WaitingForQueries;
            }
        }
        else {
            this.block();
        }
    }
}
