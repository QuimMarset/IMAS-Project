package Agents;

import Behaviours.ClassifierBehaviour;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.util.logging.Level;

public class ClassifierAgent extends Agent {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private J48 decisionTree;
    private Instances trainInstances;

    public ClassifierAgent() {
        this.decisionTree = new J48();
    }

    protected void setup() {
        logger.log(Level.INFO, "Classifier " + getAID() + " created!");

        Object[] arguments = this.getArguments();
        if (arguments != null) {
            this.trainInstances = (Instances) arguments[0];
        }

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