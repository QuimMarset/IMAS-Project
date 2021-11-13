package Behaviours;

import Agents.DataManagerAgent;
import Utils.Configuration;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.logging.Level;

enum DataManagerAgentState {
    Idle,
    WaitingForTrain,
    WaitingForQueries,
}

public class DataManagerBehaviour extends CyclicBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());

    private DataManagerAgent dataManagerAgent;
    private DataManagerAgentState dataManagerAgentState;
    private int numClassifiers;
    private int numTrainedClassifiers = 0;

    public DataManagerBehaviour(DataManagerAgent dataManagerAgent) {
        this.dataManagerAgent = dataManagerAgent;
        this.dataManagerAgentState = DataManagerAgentState.Idle;
    }

    @Override
    public void action() {
        if (this.dataManagerAgentState == DataManagerAgentState.Idle) {
            waitForMsgFromUserAgent();
        }
        else if (this.dataManagerAgentState == DataManagerAgentState.WaitingForTrain) {
            waitForClassifiersToTrain();
        }
        else
        {
            waitForTestQueryToClassify();
        }
    }

    private void waitForMsgFromUserAgent() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage message = this.dataManagerAgent.receive(performativeFilter);
        if (message != null) {
            try {
                Configuration configuration = (Configuration) message.getContentObject();
                createClassifierAgents(configuration);
                this.dataManagerAgentState = DataManagerAgentState.WaitingForTrain;
            }
            catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
        else {
            this.block();
        }
    }

    private void createClassifierAgents(Configuration configuration) {
        int numClassifiers = configuration.getNumClassifiers();
        this.numClassifiers = numClassifiers;

        String trainDatasetPath = configuration.getTrainDatasetPath();
        int numAttributes = configuration.getNumTrainAttributes();

        ContainerController containerController = this.dataManagerAgent.getContainerController();

        String packageName = this.dataManagerAgent.getClass().getPackage().getName();

        // DatasetManager datasetManager = new DatasetManager(trainDatasetPath);

        for(int i = 0; i < numClassifiers; ++i) {
            try {
                Object[] params = {trainDatasetPath, numAttributes};
                AgentController agentController = containerController.createNewAgent("classifierAgent_" + (i+1),
                        packageName + ".ClassifierAgent", params);
                agentController.start();
            }
            catch (StaleProxyException e) {
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
                this.dataManagerAgentState = DataManagerAgentState.WaitingForQueries;
                logger.log(Level.INFO, "All classifiers trained!");
                // Send message to UserAgent that training is complete
                informTrainingCompletion();
            }
        }
        else {
            this.block();
        }
    }

    private void informTrainingCompletion() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("userAgent", AID.ISLOCALNAME));
        this.dataManagerAgent.send(message);
    }

    private void waitForTestQueryToClassify() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        ACLMessage message = this.dataManagerAgent.receive(performativeFilter);
        if (message != null) {
            try {
                Configuration configuration = (Configuration) message.getContentObject();
                logger.log(Level.INFO, "Received petition to classify the following indices: " + configuration.getTestIndices());
                //TODO
            }
            catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
        else {
            this.block();
        }
    }
}
