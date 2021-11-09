package Behaviours;

import Agents.UserAgent;
import Utils.Configuration;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

enum UserAgentState {
    InitSystem,
    ReceivePetitions
}

public class HumanInteractionBehaviour extends CyclicBehaviour {

    private UserAgent userAgent;
    private UserAgentState userAgentState;

    public HumanInteractionBehaviour(UserAgent userAgent) {
        super(userAgent);
        this.userAgent = userAgent;
        this.userAgentState = UserAgentState.InitSystem;
    }

    @Override
    public void action() {
        if (this.userAgentState == UserAgentState.InitSystem) {
            initSystemAction();
            this.userAgentState = UserAgentState.ReceivePetitions;
        }
        else {
            receivePetitionsAction();
        }
    }

    private void initSystemAction() {
        Configuration configuration = readConfigurationFile();
        sendConfigurationToDataManager(configuration);
        // read xml/properties to get nº classifiers, dataset path, nº train attributes
        // store as a property
        // send to DataManager
    }

    private void receivePetitionsAction() {

    }

    private Configuration readConfigurationFile() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter the path to the desired configuration file to load:");
        try {
            String configFilePath = br.readLine();
            Configuration configuration = new Configuration(configFilePath);
            return configuration;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendConfigurationToDataManager(Configuration configuration) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID("dataManagerAgent", AID.ISLOCALNAME));
        try {
            message.setContentObject(configuration);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.userAgent.send(message);
    }
}
