package Agents;

import Behaviours.ClassifierBehaviour;
import jade.core.*;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import java.util.logging.Level;

public class ClassifierAgent extends Agent {

    private final Logger logger = Logger.getMyLogger(getClass().getName());
    private final J48 decisionTree;
    private Evaluation evaluation;

    public ClassifierAgent() {
        this.decisionTree = new J48();
    }

    protected void setup() {
        logger.log(Level.INFO, "Classifier " + getLocalName() + " created");

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

    public double trainModel(Instances trainInstances, Instances validationInstances) {
        try {
            this.decisionTree.buildClassifier(trainInstances);
            this.evaluation = new Evaluation(trainInstances);
            this.evaluation.evaluateModel(this.decisionTree, validationInstances);

            /* We are interested in determine if a firm is fraudulent (i.e. our class of interest is class 1).
               Even though we could consider worse a false positive than a false negative, we are going to use
               the F1 score as performance metric to combine predictions.
               Besides, given that we are selecting the data random, this will avoid problems of a classifier receiving
               a skewed distribution of classes
             */
            double f1Score = this.evaluation.fMeasure(1);

            this.logger.log(Level.INFO, "Classifier " + getLocalName() + " statistics:\n" +
                    this.evaluation.toClassDetailsString() + "\n" + this.evaluation.toMatrixString() + "\n" +
                    "Accuracy: " + Double.toString(1-this.evaluation.errorRate()));

            return f1Score;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

    public double[] classifyTestInstances(Instances testInstances) {
        try {
            return this.evaluation.evaluateModel(this.decisionTree, testInstances);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}