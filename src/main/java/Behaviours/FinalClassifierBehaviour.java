package Behaviours;

import Agents.FinalClassifierAgent;
import Utils.AuditDataRowWithPrediction;
import Utils.Configuration;
import Utils.DatasetManager;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.logging.Level;

import static Utils.PredictionEvaluatorUtils.evaluateAndPrintPredictionResults;


public class FinalClassifierBehaviour {
    private FinalClassifierAgent finalclassifierAgent;
    private boolean send = true;
    private int numClassifiers;
    private int numResultsReceived = 0;

    public FinalClassifierBehaviour(FinalClassifierAgent classifierAgent) {
        this.finalclassifierAgent = classifierAgent;
    }

    //@Override
    public void action() {
        if (this.send) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));
            this.finalclassifierAgent.send(message);
            this.send = false;
            //TODO someone
        }
    }
    private void trainModel() {

    }

    private void testModel(){

    }
    /*
    private void waitForResultsFromClassifierAgents() {
        while(numResultsReceived < numClassifiers) {
            MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage message = this.finalclassifierAgent.receive(performativeFilter);
            if (message != null && message.getSender().getLocalName().equals("classifierAgent")) {
                try {
                    Configuration configuration = (Configuration) message.getContentObject();
                    this.numClassifiers = configuration.getNumClassifiers();
                    this.datasetManager = new DatasetManager(configuration);

                    this.dataManagerAgentState = DataManagerAgentState.WaitingForTrain;
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                numResultsReceived++;
            } else {
                this.block();
            }
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
    }*/


}