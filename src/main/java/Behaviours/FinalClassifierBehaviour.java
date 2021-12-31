package Behaviours;

import Agents.ClassifierAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class FinalClassifierBehaviour extends CyclicBehaviour {
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
