package Behaviours;

import Agents.ClassifierAgent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.CSVLoader;

public class FinalClassifierBehaviour {
    private ClassifierAgent classifierAgent;
    private boolean send = true;
    
    public FinalClassifierBehaviour(ClassifierAgent classifierAgent) {
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
        }
    }
    private void trainModel() {

    }
}
