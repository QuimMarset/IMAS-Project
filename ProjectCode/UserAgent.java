
import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

/**
 * This agent implements a simple Ping Agent that registers itself with the DF
 * and then waits for ACLMessages. If a REQUEST message is received containing
 * the string "ping" within the content then it replies with an INFORM message
 * whose content will be the string "pong".
 *
 * @author Tiziana Trucco - CSELT S.p.A.
 * @version $Date: 2010-04-08 13:08:55 +0200 (gio, 08 apr 2010) $ $Revision:
 *          6297 $
 */
public class UserAgent extends Agent {

    public class behaviour extends Behaviour{ // this is to be change, is just for the skeleton
        public void action() {

        }
        public boolean done(){
            
        }
    }


    protected void setup() {
        // Registration with the DF
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("UserAgent");
        sd.setName(getName());
        //sd.setOwnership("TILAB");
        dfd.setName(getAID());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            WaitPingAndReplyBehaviour PingBehaviour = new WaitPingAndReplyBehaviour(this);
            addBehaviour(PingBehaviour);
        } catch (FIPAException e) {
            myLogger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
            doDelete();
        }
    }
}
