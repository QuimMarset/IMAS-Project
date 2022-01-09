package Behaviours;

import Agents.UserAgent;
import Behaviours.Enums.UserAgentState;
import Utils.Configuration;
import Utils.Predictions;
import Utils.TestQuery;
import Utils.UserInteractionUtils;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;


public class UserAgentBehaviour extends CyclicBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private final UserAgent userAgent;
    private UserAgentState state;
    private TestQuery currentTestQuery;

    public UserAgentBehaviour(UserAgent userAgent) {
        super(userAgent);
        this.userAgent = userAgent;
        this.state = UserAgentState.InitSystem;
    }

    @Override
    public void action() {
        if (this.state == UserAgentState.InitSystem) {
            Configuration configuration = UserInteractionUtils.getSystemConfiguration();
            this.logger.log(Logger.INFO, "System configuration has been parsed");
            this.sendSerializableToDataManager(configuration);
            this.state = UserAgentState.WaitForTraining;
        }

        else if (this.state == UserAgentState.WaitForTraining) {
            this.waitForClassifiersToTrain();
        }

        else if (this.state == UserAgentState.PerformTestQueries) {
            TestQuery testQuery = UserInteractionUtils.getTestQuery();
            this.logger.log(Logger.INFO, "Test Query has been parsed");
            this.sendSerializableToDataManager(testQuery);
            this.state = UserAgentState.WaitForQueriesAcceptance;
        }

        else if (this.state == UserAgentState.WaitForQueriesAcceptance) {
            waitForQueriesRequestResponse();
        }

        else {
            // Wait for test queries results
            Predictions predictions = receiveTestQueriesResults();
            if (predictions != null) {
                this.printTestQueriesResults(predictions);
                System.out.println("\nTest Instances have been classified");
                System.out.println("Introduce again another test query\n");
                this.state = UserAgentState.PerformTestQueries;
            }
        }
    }

    private void sendSerializableToDataManager(Serializable contentObject) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));
        try {
            message.setContentObject(contentObject);
            this.userAgent.send(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForClassifiersToTrain() {
        MessageTemplate messageTemplate = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.userAgent.receive(messageTemplate);

        if (message != null) {
            int messagePerformative = message.getPerformative();
            if (messagePerformative == ACLMessage.REFUSE) {
                // A refuse has been sent back because the training cannot be done with those system settings
                this.state = UserAgentState.InitSystem;
                System.out.println(message.getContent());
                System.out.println("Select the configuration again");
            }
            else if (messagePerformative == ACLMessage.INFORM) {
                this.state = UserAgentState.PerformTestQueries;
                System.out.println("\nThe classifiers have finished training. Test queries can be done now");
            }
        }
        else {
            this.block();
        }
    }

    private void waitForQueriesRequestResponse() {
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.userAgent.receive(senderFilter);

        if (message != null) {
            if (message.getPerformative() == ACLMessage.AGREE) {
                try {
                    this.currentTestQuery = (TestQuery) message.getContentObject();
                    if (this.currentTestQuery.isRandomMode()) {
                        System.out.println("Random generated query:\n" + this.currentTestQuery);
                    }
                    System.out.println("Now wait for the results to come");
                    this.state = UserAgentState.WaitForQueriesResults;
                }
                catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            else if (message.getPerformative() == ACLMessage.REFUSE) {
                this.state = UserAgentState.PerformTestQueries;
                System.out.println(message.getContent());
                System.out.println("Select the test query again");
            }

        }
        else {
            this.block();
        }
    }

    private Predictions receiveTestQueriesResults() {
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("finalClassifierAgent", AID.ISLOCALNAME));
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.userAgent.receive(MessageTemplate.and(senderFilter, performativeFilter));

        if (message != null) {
            try {
                return (Predictions) message.getContentObject();
            }
            catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
        else {
            this.block();
        }
        return null;
    }

    private void printTestQueriesResults(Predictions predictions) {
        System.out.println("\nTest Query Results:");
        List<String> predicted = predictions.getPredictions();
        int[] instanceIndices = this.currentTestQuery.getInstanceIndices();

        boolean groundTruthAvailable = this.currentTestQuery.isGroundTruthAvailable();
        int[] groundTruthValues = null;
        if (groundTruthAvailable) {
           groundTruthValues = this.currentTestQuery.getGroundTruthValues();
        }

        for (int i = 0; i < instanceIndices.length; ++i) {
            if (groundTruthAvailable && groundTruthValues[i] != -1) {
                System.out.println("Test instance " + instanceIndices[i] + ": Predicted = " + predicted.get(i) +
                        " | Actual = " + Predictions.translateLabel(groundTruthValues[i]));
            }
            else {
                System.out.println("Test instance " + instanceIndices[i] + ": Predicted = " + predicted.get(i));
            }
        }
    }
}
