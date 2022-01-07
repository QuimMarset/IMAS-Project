package Behaviours;

import Agents.ClassifierAgent;
import Agents.FinalClassifierAgent;
import Agents.UserAgent;
import Utils.*;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.classifiers.trees.J48;

import javax.management.AttributeNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static Utils.PredictionEvaluatorUtils.evaluateAndPrintPredictionResults;

enum FinalClassifierAgentState {
    GetNumClassifier,
    GetPercentageError,
    InformClassifier,
    GetPredictions
}

public class FinalClassifierBehaviour extends CyclicBehaviour {
    private FinalClassifierAgent finalclassifierAgent;
    private FinalClassifierAgentState finalClassifierAgentState;
    private boolean send = true;
    double [] bestError;
    Object [] predictions;
    double bError;
    private int numClassifiers = 0;
    private int numResultsReceived = 0;

    public FinalClassifierBehaviour(FinalClassifierAgent finalclassifierAgent) {
        super(finalclassifierAgent);
        this.finalclassifierAgent = finalclassifierAgent;
        this.finalClassifierAgentState = FinalClassifierAgentState.GetNumClassifier;
    }

    //@Override
    public void action() {
        if (this.finalClassifierAgentState == FinalClassifierAgentState.GetNumClassifier) {
            receiveNumberOfClassifiers();
        }
        else if (this.finalClassifierAgentState == FinalClassifierAgentState.GetPercentageError) {
            getPercentageError();
        }
        else if (this.finalClassifierAgentState == FinalClassifierAgentState.InformClassifier) {
            informClassifier();
        }
        else if (this.finalClassifierAgentState == FinalClassifierAgentState.GetPredictions) {
            getPredictions();
        }

    }


    private void receiveNumberOfClassifiers() {

        System.out.println("Final classifier : receiveNumberOfClassifiers()");
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.finalclassifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));
        System.out.println(message);
        if (message != null) {
            try {
                numClassifiers = Integer.parseInt(message.getContent());
                System.out.println("Final Classifier : received number of classifiers from Data Manager : " + numClassifiers);
                this.finalClassifierAgentState = FinalClassifierAgentState.GetPercentageError;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            this.block();
        }
    }


    private boolean getPercentageError() {
        bestError = new double[numClassifiers];
        boolean received = false;
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.finalclassifierAgent.receive(performativeFilter);
        for(int i = 0; i<= numClassifiers;i++){
            if (message != null && message.getSender().getLocalName().startsWith("classifierAgent_")) {
                double error = Double.parseDouble(message.getContent());
                bestError[i]=error;
            }
        }

        if(bestError[0]!=0.0){
            received = true;
        }
        this.finalClassifierAgentState = FinalClassifierAgentState.InformClassifier;
        return received;
    }

    private void informClassifier() {
        for(int i=0; i<=numClassifiers; i++) {
            MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("classifierAgent_" + (i + 1), AID.ISLOCALNAME));
            ACLMessage message = this.finalclassifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

            if (message != null) {
                ACLMessage reply = message.createReply();
                try {

                    boolean haveBeenSent = this.getPercentageError();

                    if (haveBeenSent) {
                        reply.setContent("Percentage error Received");
                        this.finalClassifierAgentState = FinalClassifierAgentState.GetPredictions;
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("No Percentage Error Found");

                    }
                } catch (IndexOutOfBoundsException e) {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("The test queries contain invalid instance indices");
                }

                this.finalclassifierAgent.send(reply);
            } else {
                this.block();
            }
        }

    }

    private void getPredictions(){
        predictions = new Object[numClassifiers];
        bError = 1000.0;
        int pos = 0;
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        ACLMessage message = this.finalclassifierAgent.receive(performativeFilter);

        for(int i = 0; i<= numClassifiers;i++){
            if (message != null && message.getSender().getLocalName().startsWith("classifierAgent_")) {
                Object prediction = message.getContent();
                predictions[i] = prediction;
            }
            if(bestError[i]<bError){
                bError = bestError[i];
                pos = i;
            }
        }
        sendPrediction(pos);
        this.finalClassifierAgentState = FinalClassifierAgentState.GetPredictions;

    }

    private void sendPrediction(int pos){
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("userAgent", AID.ISLOCALNAME));

        message.setContent(String.valueOf(predictions[pos]));
        this.finalclassifierAgent.send(message);
    }




}