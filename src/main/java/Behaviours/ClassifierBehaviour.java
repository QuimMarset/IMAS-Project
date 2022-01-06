package Behaviours;

import Agents.ClassifierAgent;
import Utils.ClassifierInstances;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import weka.classifiers.trees.J48;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


public class ClassifierBehaviour extends CyclicBehaviour {

    private ClassifierAgent classifierAgent;
    private boolean send = true;


    public ClassifierBehaviour(ClassifierAgent classifierAgent) {
        this.classifierAgent = classifierAgent;
    }

    @Override
    public void action() {
        if (this.send) {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));
            this.classifierAgent.send(message);
            this.send = false;
            //TODO someone
            receiveTrainValInstances();
        }
    }

    private void sendResults() throws IOException {
        System.out.println(getAgent().getLocalName() + ": ...Sending results to Final Classifier");
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("finalClassifierAgent", AID.ISLOCALNAME));

        HashMap<Double, J48> content = new HashMap<Double, J48>();
        content.put(this.classifierAgent.getErrorRate(), this.classifierAgent.getModel());

        message.setContentObject((Serializable) content);

        message.setContent(String.valueOf(this.classifierAgent.getErrorRate()));
        this.classifierAgent.send(message);
    }

    private void receiveTrainValInstances() {
        MessageTemplate performativeFilter = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
        MessageTemplate senderFilter = MessageTemplate.MatchSender(new AID("dataManagerAgent", AID.ISLOCALNAME));

        ACLMessage message = this.classifierAgent.receive(MessageTemplate.and(performativeFilter, senderFilter));

        if (message != null) {
            ACLMessage reply = message.createReply();
            try {
                ClassifierInstances instances = (ClassifierInstances) message.getContentObject();
                this.classifierAgent.trainModel(instances.getTrainInstances(),
                        instances.getValidaitonInstances());

                sendResults();
                reply.setContent("Classifier " + this.classifierAgent.getLocalName() + " has finished training");
                reply.setPerformative(ACLMessage.INFORM);
            }
            catch (UnreadableException e) {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("The train and validation instances cannot be retrieved");
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.classifierAgent.send(reply);
        }
        else {
            this.block();
        }
    }


}
