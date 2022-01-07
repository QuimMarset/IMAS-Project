package Agents;

import Behaviours.ClassifierBehaviour;
import jade.core.*;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
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
    private double testError;
    private double perCorrect;

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

    public double getErrorRate() {

        return testError;
    }

    public J48 getModel() {
        return decisionTree;
    }



    public void trainModel(Instances trainInstances, Instances validaitonInstances) throws Exception {

        testError = 0.00;
        perCorrect = 0.00;

        decisionTree = new J48();
        decisionTree.buildClassifier(trainInstances);
        Evaluation eval = new Evaluation(trainInstances);
        eval.evaluateModel(decisionTree, validaitonInstances);


        StringBuffer forPredictionsPrinting = new StringBuffer();
        PlainText classifierOutput = new PlainText();
        classifierOutput.setBuffer(forPredictionsPrinting);
        weka.core.Range attsToOutput = null;
        Boolean outputDistribution = new Boolean(true);
        classifierOutput.setOutputDistribution(true);
        eval.crossValidateModel(decisionTree, trainInstances, 10, new Random(1), classifierOutput, attsToOutput, outputDistribution);

        //Possible accuracy variables
        testError = eval.errorRate();


        System.out.println("===== J48 classifier ===== " + getLocalName());
        System.out.println("Number of correct classified " + eval.correct());
        System.out.println("Percentage of correct classified " + eval.pctCorrect());

        System.out.println("TestError:" + eval.errorRate());
        System.out.println(eval.toClassDetailsString());
        System.out.println(eval.toMatrixString());
        System.out.println(eval.toSummaryString());


    }
}