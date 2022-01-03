package Behaviours;

import Agents.UserAgent;
import Utils.AuditDataRowWithPrediction;
import Utils.Configuration;
import Utils.TestQuery;
import Utils.UserInteractionUtils;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static Utils.PredictionEvaluatorUtils.evaluateAndPrintPredictionResults;

enum UserAgentState {
    InitSystem,
    WaitForTraining,
    PerformTestQueries,
    WaitForQueriesAcceptance,
    WaitForQueriesResults
}

public class UserAgentBehaviour extends CyclicBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());

    private final UserAgent userAgent;
    private UserAgentState userAgentState;

    public UserAgentBehaviour(UserAgent userAgent) {
        super(userAgent);
        this.userAgent = userAgent;
        this.userAgentState = UserAgentState.InitSystem;
    }

    @Override
    public void action() {
        if (this.userAgentState == UserAgentState.InitSystem) {
            initializeSystem();
        }
        else if (this.userAgentState == UserAgentState.WaitForTraining) {
            waitForClassifiersToTrain();
        }
        else if (this.userAgentState == UserAgentState.PerformTestQueries) {
            performTestQueries();
        }
        else if (this.userAgentState == UserAgentState.WaitForQueriesAcceptance) {
            waitForQueriesRequestResponse();
        }
        else {
            // Wait for test queries results
            waitForQueriesResults();
        }
    }

    private void initializeSystem() {
        Configuration configuration = UserInteractionUtils.getSystemConfiguration();

        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));
        try {
            message.setContentObject(configuration);
            this.userAgent.send(message);
            this.userAgentState = UserAgentState.WaitForTraining;
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
            if (messagePerformative == ACLMessage.AGREE) {
                this.logger.log(Level.INFO, "The classifiers have been created correctly");
            }
            else if (messagePerformative == ACLMessage.REFUSE) {
                // A refuse has been sent back because the training cannot be done with those system settings
                this.userAgentState = UserAgentState.InitSystem;
                System.out.println("\nThere were some problems with the system configuration. Enter them again");
            }
            else if (messagePerformative == ACLMessage.INFORM) {
                this.userAgentState = UserAgentState.PerformTestQueries;
                System.out.println("\nThe classifiers have finished training. Test queries can be done now");
            }
        }
        else {
            this.block();
        }
    }

    private void performTestQueries() {
        TestQuery testQuery = UserInteractionUtils.getTestQuery();

        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));

        try {
            message.setContentObject(testQuery);
            this.userAgent.send(message);
            this.userAgentState = UserAgentState.WaitForQueriesAcceptance;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void waitForQueriesRequestResponse() {
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.userAgent.receive(senderFilter);

        if (message != null) {
            if (message.getPerformative() == ACLMessage.AGREE) {
                System.out.println(message.getContent());
                System.out.println("Now wait for the results to come");
                this.userAgentState = UserAgentState.WaitForQueriesResults;
            }
            else if (message.getPerformative() == ACLMessage.REFUSE) {
                this.userAgentState = UserAgentState.PerformTestQueries;
                System.out.println(message.getContent());
            }

        }
        else {
            this.block();
        }
    }

    private void waitForQueriesResults() {
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("finalClassifierAgent", AID.ISLOCALNAME));
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.AGREE);
        ACLMessage message = this.userAgent.receive(MessageTemplate.and(senderFilter, performativeFilter));

        if (message != null) {
            try {
                message.getContentObject();
                //evaluateAndPrintPredictionResults(petitionResults);
                this.userAgentState = UserAgentState.PerformTestQueries;
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
