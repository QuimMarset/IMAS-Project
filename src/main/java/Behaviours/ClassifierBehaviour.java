package Behaviours;

import Agents.ClassifierAgent;
import Utils.ClassifierInstances;
import Utils.Configuration;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;





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
