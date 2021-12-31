package Behaviours;

import Agents.ClassifierAgent;
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

//import javax.sql.DataSource;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.CSVLoader;


public class ClassifierBehaviour extends CyclicBehaviour {

    private ClassifierAgent classifierAgent;
    private boolean send = true;

    CSVLoader loader = new CSVLoader();
    loader.setSource(new File("dataset.csv"));
    Instances data = loader.getDataSet();

    //DataSource source = new DataSource("dataset.csv");
    //Instances train = source.getDataSet();

    //Instances test= ;
    //int[][] train = {{9, 1, 8}, {8, 7, 5},{5,9,1},{0,5,8}};
    //int[][] test = {{8,3,6}, {1,7,0}};
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
        }
    }
    private void trainModel() {
        this.train = train;
        Classifier cls = new J48();
        cls.buildClassifier(this.train);
    }


}
