package Agents;

import Behaviours.DataManagerBehaviour;
import Utils.*;
import jade.core.*;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import weka.core.Instances;

import javax.management.AttributeNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class DataManagerAgent extends Agent {

    private final Logger logger = Logger.getMyLogger(getClass().getName());

    private Configuration configuration;
    private DatasetManager datasetManager;
    private final List<ClassifierAttributes> classifierAttributes = new ArrayList<>();

    public void initializeDatasetManager(Configuration configuration) throws IndexOutOfBoundsException {
        this.configuration = configuration;
        this.datasetManager = new DatasetManager(configuration.getTrainDatasetPath(),
                configuration.getTestDatasetPath());
        this.checkConfigurationWithDataset();
    }

    public void createClassifierAgent(int index) {
        ContainerController containerController = this.getContainerController();
        String packageName = this.getClass().getPackage().getName();

        try {
            AgentController agentController = containerController.createNewAgent("classifierAgent_" + (index),
                    packageName + ".ClassifierAgent", null);
            agentController.start();
        }
        catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public ClassifierInstances getClassifierInstances() {
        int numTrainInstances = configuration.getNumTrainInstances();
        int validationPercentage = configuration.getValidationPercentage();
        int numTrainAttributes = configuration.getNumTrainAttributes();

        ClassifierInstances classifierInstances = this.datasetManager.getClassifierInstances(numTrainInstances,
                validationPercentage, numTrainAttributes);

        // Store the attributes used by that classifier to filter in the test phase
        this.classifierAttributes.add(new ClassifierAttributes(classifierInstances.getTrainInstances()));

        return classifierInstances;
    }

    public Instances getTestInstances(TestQuery testQuery) throws IndexOutOfBoundsException, AttributeNotFoundException {
        int[] instanceIndices;
        List<String> attributeNames;

        if (testQuery.isRandom()) {
            /* Generate a random test query containing 15 instances, and each instance will contain 20 attributes
               This process seem redundant, but it is for the sake of filling the test query that will be later returned
               to the User Agent */
            instanceIndices = this.datasetManager.getRandomTestInstances(15);
            attributeNames = this.datasetManager.getRandomAttributes(20);
            testQuery.setInstanceIndices(instanceIndices);
            testQuery.setAttributeNames(attributeNames);
        }
        else {
            instanceIndices = testQuery.getInstanceIndices();
            attributeNames = testQuery.getAttributeNames();
        }
        return this.datasetManager.getTestInstances(instanceIndices, attributeNames);
    }

    public boolean areTestInstancesPredictable(Instances testInstances, int classifier) {
        return this.classifierAttributes.get(classifier).areInstancesClassifiable(testInstances);
    }

    public Instances getClassifierTestInstances(Instances testInstances, int classifier) {
        return this.classifierAttributes.get(classifier).filterClassifiableInstances(testInstances);
    }

    protected void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("DataManagerAgent");
        sd.setName(getName());
        dfd.setName(getAID());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            addBehaviour(new DataManagerBehaviour(this));

        }
        catch (FIPAException e) {
            logger.log(Logger.SEVERE, "Agent " + getLocalName() + " - Cannot register with DF", e);
            doDelete();
        }
    }

    private void checkConfigurationWithDataset() throws IndexOutOfBoundsException {
        int numTrainInstances = this.configuration.getNumTrainInstances();
        int numTrainAttributes = this.configuration.getNumTrainAttributes();
        int validationPercentage = this.configuration.getValidationPercentage();

        int numNoClassAttributes = this.datasetManager.getNumNoClassAttributes();
        int numTrainDatasetInstances = this.datasetManager.getNumTrainInstances();

        if (numTrainInstances > numTrainDatasetInstances || numTrainAttributes > numNoClassAttributes ||
                validationPercentage > 100) {
            throw new IndexOutOfBoundsException("The configuration tries to train with either more instances or " +
                    "attributes the dataset has, or testing with more attributes, or validating with a percentage " +
                    "bigger than 100");
        }
    }
}