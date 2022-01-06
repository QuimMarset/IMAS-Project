package Behaviours;

import Agents.ClassifierAgent;
import Agents.FinalClassifierAgent;
import Utils.AuditDataRowWithPrediction;
import Utils.ClassifierInstances;
import Utils.Configuration;
import Utils.DatasetManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static Utils.PredictionEvaluatorUtils.evaluateAndPrintPredictionResults;


public class FinalClassifierBehaviour extends CyclicBehaviour {
    private FinalClassifierAgent finalclassifierAgent;
    private boolean send = true;
    private int numClassifiers = 0;
    private int numResultsReceived = 0;

    public FinalClassifierBehaviour(FinalClassifierAgent classifierAgent) {
        this.finalclassifierAgent = classifierAgent;
    }

    //@Override
    public void action() {
        if (this.send) {
            receiveNumberOfClassifiers();
        }
    }

    private void trainModel() {

    }

    private void testModel(){

    }

    private void receiveNumberOfClassifiers() {
        /*
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));
        this.finalclassifierAgent.send(message);
        this.send = false;
        */
        System.out.println("Final classifier : receiveNumberOfClassifiers()");
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));
        ACLMessage message = this.finalclassifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));
        System.out.println(message);
        if (message != null) {
            try {
                numClassifiers = Integer.parseInt(message.getContent());
                System.out.println("Final Classifier : received number of classifiers from Data Manager : " + numClassifiers);
                waitForResultsFromClassifierAgents();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            this.block();
        }
    }


    private void waitForResultsFromClassifierAgents() {
        double bestError = 1000;
        J48 bestClassifier = null;
        String bestClassifierName = "";
        while(numResultsReceived < numClassifiers) {
            MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("classifierAgent_4", AID.ISLOCALNAME));
            ACLMessage message = this.finalclassifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));
            System.out.println("Final classifier - Classifier sender : " + message.getSender());
            System.out.println(message);
            if (message != null && message.getSender().getLocalName().equals("classifierAgent")) {
                System.out.println("Final Classifier : ...Received message from Classifier Agent");
                try {
                    Map<Double, J48> content = (HashMap<Double, J48>) message.getContentObject();

                    Object[][] array = new String[content.size()][2];
                    int count = 0;
                    for(Map.Entry<Double, J48> entry : content.entrySet()){
                        array[count][0] = entry.getKey();
                        array[count][1] = entry.getValue();
                        double error = entry.getKey();
                        J48 model = entry.getValue();
                        if(error < bestError) {
                            bestError = error;
                            bestClassifier = model;
                            bestClassifierName = message.getSender().getName();
                        }
                        count++;
                    }



                    /*
                    double error = Double.parseDouble(message.getContent());
                    System.out.println("Classifier Error rate : " + error);

                    if(error < bestError) {
                        bestError = error;
                        bestClassifier = message.getSender().getName();
                        DFAgentDescription dfd = new DFAgentDescription();

                    }
*/
                    //J48 model = (J48) message.getContentObject();
                    //System.out.println("Classifier Model : ");
                    //System.out.println(model);
                    /*
                    Configuration configuration = (Configuration) message.getContentObject();
                    this.numClassifiers = configuration.getNumClassifiers();
                    this.datasetManager = new DatasetManager(configuration);

                    this.dataManagerAgentState = DataManagerAgentState.WaitingForTrain;
                    */
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
                numResultsReceived++;
            } else {
                this.block();
            }
        }

        System.out.println("Best Classifier : "+ bestClassifierName + "\n Error: " + bestError + "\n Model: " + bestClassifier + "\n");
    }
    /*
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