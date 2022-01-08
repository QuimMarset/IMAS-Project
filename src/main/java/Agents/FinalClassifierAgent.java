package Agents;

import Behaviours.ClassifierBehaviour;
import Behaviours.FinalClassifierBehaviour;
import jade.core.*;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import weka.core.Instances;

public class FinalClassifierAgent extends Agent {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private Instances trainInstances;

    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("FinalClassifierAgent");
        sd.setName(getName());
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            this.addBehaviour(new FinalClassifierBehaviour(this));
        }
        catch (FIPAException e) {
            logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
            doDelete();
        }
    }

}