package urv.imas;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

public class MyAgent extends Agent {

    public class MyBehaviour extends Behaviour{

        @Override
        public void action() {

        }

        @Override
        public boolean done() {
            return false;
        }
    }

    protected void setup() {

    }

}
