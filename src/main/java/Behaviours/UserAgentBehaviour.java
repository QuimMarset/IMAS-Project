package Behaviours;

import Agents.UserAgent;
import Utils.AuditDataRowWithPrediction;
import Utils.Configuration;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
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
    ReceivePetitions,
    WaitForPetitionResults
}

public class UserAgentBehaviour extends CyclicBehaviour {

    private final Logger logger = Logger.getMyLogger(getClass().getName());

    private UserAgent userAgent;
    private UserAgentState userAgentState;

    private String configFilePath;

    public UserAgentBehaviour(UserAgent userAgent) {
        super(userAgent);
        this.userAgent = userAgent;
        this.userAgentState = UserAgentState.InitSystem;
    }

    @Override
    public void action() {
        if (this.userAgentState == UserAgentState.InitSystem) {
            initSystemAction();
            this.userAgentState = UserAgentState.WaitForTraining;
        }
        else if (this.userAgentState == UserAgentState.WaitForTraining) {
            waitForClassifiersToTrain();
        }
        else if (this.userAgentState == UserAgentState.ReceivePetitions) {
            receivePetitionsAction();
            this.userAgentState = UserAgentState.WaitForPetitionResults;
        }
        else {
            waitForPetitionResults();
        }
    }

    private void initSystemAction() {
        Configuration configuration = readConfigurationFile(null);
        sendConfigurationToDataManager(configuration);
    }

    private String promptQueryingAndReturnTestingIndices()
    {
        String testingIndices = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        logger.log(Level.INFO, "Select querying mode:");
        logger.log(Level.INFO, "1. Randomly auto-elect 15 data rows");
        logger.log(Level.INFO, "2. Manually enter the data rows indices");
        logger.log(Level.INFO, "Choice: ");
        try {
            int queryingMode = Integer.parseInt(br.readLine());
            switch (queryingMode)
            {
                case 1: List<Integer> generatedIndices = new ArrayList<>();
                    do {
                        generatedIndices.add((int) (Math.random()*(50+1)));
                    } while (generatedIndices.size() < 15);
                    testingIndices = generatedIndices.toString();
                    testingIndices = testingIndices.substring(1, testingIndices.length() - 1);
                    break;
                case 2: logger.log(Level.INFO, "Enter 15 numbers between 0-49 (comma separated): ");
                    testingIndices = br.readLine();
                    break;
                default: logger.log(Level.WARNING, "Invalid choice, choose from the above options!");
                    break;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return testingIndices;
    }

    private void receivePetitionsAction() {
        String testingIndices = null;
        do {
            testingIndices = promptQueryingAndReturnTestingIndices();
        } while (null == testingIndices);
        Configuration configuration = readConfigurationFile(this.configFilePath);
        configuration.setTestIndices(testingIndices);
        sendConfigurationToDataManager(configuration);
    }

    private Configuration readConfigurationFile(String configFilePath) {
        if (configFilePath == null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            logger.log(Level.INFO, "Enter the path to the desired configuration file to load: ");
            try {
                this.configFilePath = br.readLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            Configuration configuration = new Configuration(this.configFilePath);
            return configuration;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendConfigurationToDataManager(Configuration configuration) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));
        try {
            message.setContentObject(configuration);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.userAgent.send(message);
    }

    private void waitForClassifiersToTrain() {
        MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.userAgent.receive(messageTemplate);
        if (message != null && message.getSender().getLocalName().equals("dataManagerAgent")) {
            this.userAgentState = UserAgentState.ReceivePetitions;
        }
        else {
            this.block();
        }
    }

    private void waitForPetitionResults() {
        MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.userAgent.receive(messageTemplate);
        if (message != null && message.getSender().getLocalName().equals("dataManagerAgent")) {
            //TODO: get predictions for the 15 data rows
            try {
                AuditDataRowWithPrediction[] petitionResults = (AuditDataRowWithPrediction[]) message.getContentObject();
                logger.log(Level.INFO, "Received classifications for the chosen ");
                evaluateAndPrintPredictionResults(petitionResults);
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
