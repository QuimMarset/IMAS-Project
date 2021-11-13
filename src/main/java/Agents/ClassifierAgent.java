package Agents;

import Behaviours.ClassifierBehaviour;
import Behaviours.DataManagerBehaviour;
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import weka.classifiers.trees.J48;

import java.util.logging.Level;

public class ClassifierAgent extends Agent {
    private final Logger logger = Logger.getMyLogger(getClass().getName());

    J48 decisionTree;

    public ClassifierAgent() {
        this.decisionTree = new J48();
    }

    protected void setup() {
        logger.log(Level.INFO, "Classifier " + getAID() + " created!");
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ClassifierAgent");
        sd.setName(getName());
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            this.addBehaviour(new ClassifierBehaviour(this));
        }
        catch (FIPAException e) {
            logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
            doDelete();
        }
    }
}