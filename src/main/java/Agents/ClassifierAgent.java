package Agents;

import Behaviours.ClassifierBehaviour;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.util.Random;
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

    public void trainModel(Instances trainInstances, Instances validaitonInstances) throws Exception {
        /*Classifier cls = new J48();
        Evaluation evaluation = new Evaluation(trainInstances);
        try {
            cls.buildClassifier(trainInstances);
            evaluation.evaluateModel(cls, validaitonInstances);
            System.out.println(evaluation.toClassDetailsString());
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        Evaluation eval = new Evaluation(trainInstances);
        J48 tree = new J48();
        StringBuffer forPredictionsPrinting = new StringBuffer();
        PlainText classifierOutput = new PlainText();
        classifierOutput.setBuffer(forPredictionsPrinting);
        weka.core.Range attsToOutput = null;
        Boolean outputDistribution = new Boolean(true);
        classifierOutput.setOutputDistribution(true);
        eval.crossValidateModel(tree, trainInstances, 10, new Random(1), classifierOutput, attsToOutput, outputDistribution);
        System.out.println("===== J48 classifier =====");
        System.out.println("Number of correct classified " + eval.correct());
        System.out.println("Percentage of correct classified " + eval.pctCorrect());
        System.out.println(eval.toClassDetailsString());
        System.out.println(eval.toMatrixString());
        System.out.println(eval.toSummaryString());
    }
}