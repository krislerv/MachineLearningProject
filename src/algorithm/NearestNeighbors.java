package algorithm;

import distanceFunctions.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of a k-NearestNeighbor algorithm with k-fold cross validation
 */
public class NearestNeighbors {

    /**
     * An ArrayList containing ArrayLists containing Nodes. Each ArrayList<Node> is a
     * sample (or fold) used in cross-validation. With 10-fold cross validation, the
     * size of samples would be 10
     */
    private ArrayList<ArrayList<Node>> samples;

    /**
     * A list of all the possible values for the target variable when we are classifying
     */
    private String[] classificationTargetAttributeValues;

    /**
     * k: How many neighbors to consider
     * crossValidationFolds: How many folds to use for cross-validation
     */
    private final int k, crossValidationFolds;

    /**
     * realValDist: Which distance function to use for real value attributes
     * catValDist: Which distance function to use for categorical value attributes
     */
    private RealValueDistanceFunction realValDist;
    private CategoricalValueDistanceFunction catValDist;

    /**
     * targetAttributeName: The name of the target attribute
     * filename: The filename of the dataset we're using
     * skippedAttributes: A list of the names of attributes we're omitting
     */
    private final String targetAttributeName, filename;
    private final ArrayList<String> skippedAttributes;

    /**
     * Initializes the algorithm with the given values. Loads the data, normalizes the
     * data, randomizes the order and divides the data into samples for cross-validation
     * @param k how many neighbors to consider
     * @param crossValidationFolds how many folds to use for cross-validation
     * @param targetAttributeName the name of the target attribute
     * @param skippedAttributes list of the names of attributes we're omitting
     * @param filename filename of the dataset we're using
     * @param realValDist distance function to use for real value attributes
     * @param catValDist distance function to use for categorical value attributes
     */
    private NearestNeighbors(int k, int crossValidationFolds, String targetAttributeName, ArrayList<String> skippedAttributes, String filename, RealValueDistanceFunction realValDist, CategoricalValueDistanceFunction catValDist) {
        // Initialize fields
        this.k = k;
        this.crossValidationFolds = crossValidationFolds;
        this.targetAttributeName = targetAttributeName;
        this.filename = filename;
        this.skippedAttributes = skippedAttributes;
        this.realValDist = realValDist;
        this.catValDist = catValDist;
        samples = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            samples.add(new ArrayList<>());
        }

        int targetAttributeIndex = -1;

        // Load data
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<String> attributeTypes = new ArrayList<>();
        try {
            Scanner in = new Scanner(new FileReader(filename));
            ArrayList<Integer> skippedAttributesIndexes = new ArrayList<>();
            int currentAttributeIndex = 0;
            // skip all the comments in the beginning of the file
            while (!in.nextLine().startsWith("@relation")) {
                // do nothing
            }
            // when we get to the attributes, record the sequence of
            // real-valued and categorical attributes.
            // if the attribute in question is to be omitted,
            // don't record it.
            // if the attribute in question is the target attribute,
            // keep track of its index and the possible attribute values.
            while (in.hasNext(Pattern.compile("@attribute"))) {
                String line = in.nextLine();
                String attributeName = line.split(" ")[1];
                if (skippedAttributes.contains(attributeName)) {
                    skippedAttributesIndexes.add(currentAttributeIndex);
                }
                if (line.split(" ")[2].equals("real")) {
                    if (attributeName.equals(targetAttributeName)) {
                        targetAttributeIndex = currentAttributeIndex;
                    }
                    attributeTypes.add("real");
                } else {
                    attributeTypes.add("categorical");
                    if (attributeName.equals(targetAttributeName)) {
                        targetAttributeIndex = currentAttributeIndex;
                        classificationTargetAttributeValues = line.split("\\{")[1].substring(0, line.split("\\{")[1].length() - 1).split(", ");
                    }
                }
                currentAttributeIndex++;

            }
            // skip everything between the attributes and the data
            while (!in.nextLine().startsWith("@data")) {
                // do nothing
            }
            // When we get to the data, create a Node-object for each row
            while (!in.hasNext(Pattern.compile("%"))) {
                String line = in.nextLine();
                String[] attributeList = line.split(",");
                ArrayList<Float> attributeListReal = new ArrayList<>();
                ArrayList<String> attributeListCategorical = new ArrayList<>();
                String targetAttribute = "";
                for (int i = 0; i < attributeList.length; i++) {
                    if (skippedAttributesIndexes.contains(i)) {
                        continue;
                    }
                    if (i == targetAttributeIndex) {
                        targetAttribute = attributeList[i];
                    } else if (attributeTypes.get(i).equals("real")) {
                        attributeListReal.add(Float.parseFloat(attributeList[i]));
                    } else {
                        attributeListCategorical.add(attributeList[i]);
                    }
                }
                nodes.add(new Node(attributeListReal, attributeListCategorical, targetAttribute));
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Normalize numerical values
        for (int i = 0; i < nodes.get(0).realValues.length; i++) {
            float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
            for (Node node : nodes) {
                if (node.realValues[i] < min) {
                    min = node.realValues[i];
                } else if (node.realValues[i] > max) {
                    max = node.realValues[i];
                }
            }
            for (Node node : nodes) {
                node.realValues[i] = (node.realValues[i] - min) / (max - min);
            }
        }
        // Randomize instance order and divide into samples (folds)
        Collections.shuffle(nodes, new Random(4));
        int nextSampleIndex = 0;
        while (!nodes.isEmpty()) {
            samples.get(nextSampleIndex).add(nodes.remove(0));
            nextSampleIndex = nextSampleIndex == crossValidationFolds - 1 ? 0 : nextSampleIndex+1;
        }
    }

    /**
     * Returns the k nearest neighbors to the given node
     * @param n1 the given node
     * @param validationSetIndex the index of the validation set (which will be skipped)
     * @return the k nearest neighbors to the given node
     */
    private ArrayList<Node> getNearestNeighbors(Node n1, int validationSetIndex) {
        ArrayList<Node> nearestNeighbors = new ArrayList<>();
        ArrayList<Node> trainingSet = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            if (i == validationSetIndex) {
                continue;
            }
            trainingSet.addAll(samples.get(i));
        }
        for (Node node : trainingSet) {
            if (nearestNeighbors.isEmpty()) {
                nearestNeighbors.add(node);
            } else {
                for (int j = 0; j < nearestNeighbors.size(); j++) {
                    if (realValDist.distance(node, n1) + catValDist.distance(node, n1) < realValDist.distance(nearestNeighbors.get(j), n1) + catValDist.distance(nearestNeighbors.get(j), n1)) {
                        nearestNeighbors.add(j, node);
                        break;
                    }
                }
                if (nearestNeighbors.size() < k && !nearestNeighbors.contains(node)) {
                    nearestNeighbors.add(node);
                }
                if (nearestNeighbors.size() > k) {
                    nearestNeighbors.remove(k);
                }
            }
        }
        return nearestNeighbors;
    }

    /**
     * Classifies a single node
     * @param n1 the node to be classified
     * @param validationSetIndex the index of the validation set (which will be skipped when generating neighbors)
     * @return the classification value of the node
     */
    private String classify(Node n1, int validationSetIndex) {
        ArrayList<Node> nearestNeighbors = getNearestNeighbors(n1, validationSetIndex);
        ArrayList<Double> attributeValueCounts = new ArrayList<>();

        for (String ignored : classificationTargetAttributeValues) {
            attributeValueCounts.add(0.0);
        }

        for (Node node : nearestNeighbors) {
            for (int i = 0; i < classificationTargetAttributeValues.length; i++) {
                if (Objects.equals(node.targetAttributeValue, classificationTargetAttributeValues[i])) {
                    if (true) {
                        attributeValueCounts.set(i, attributeValueCounts.get(i) + 1.0/Math.pow(realValDist.distance(node, n1) + catValDist.distance(node, n1), 2));
                    } else {
                        attributeValueCounts.set(i, attributeValueCounts.get(i) + 1);
                    }
                }
            }
        }
        return classificationTargetAttributeValues[attributeValueCounts.indexOf(Collections.max(attributeValueCounts))];
    }

    /**
     * Performs a k-NearestNeighbor classification
     * Prints out the results
     */
    private void classification() {
        ArrayList<Float> successRates = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            int correct = 0;
            for (Node node : samples.get(i)) {
                if (Objects.equals(classify(node, i), node.targetAttributeValue)) {
                    correct++;
                }
            }
            successRates.add((float) correct / samples.get(i).size());
        }
        float averageSuccessRate = 0;
        for (Float f : successRates) {
            averageSuccessRate += f;
        }
        averageSuccessRate = averageSuccessRate / crossValidationFolds;

        System.out.println("Results for " + k + "-NearestNeighbor targetAttributeValue with " + crossValidationFolds + "-fold cross-validation for the data set " + filename + ":");
        System.out.println("Target attribute: " + targetAttributeName);
        System.out.println("Real value distance function: " + realValDist);
        System.out.println("Categorical value distance function: " + catValDist);
        System.out.println("The target attribute was correctly classified " + 100*averageSuccessRate + "% of the time");
        System.out.println("Attributes omitted: " + skippedAttributes);
    }

    /**
     * Predicts the value of a single node
     * @param n1 the node to predict the value for
     * @param validationSetIndex the index of the validation set (which will be skipped when generating neighbors)
     * @return the predicted value of the node
     */
    private double regress(Node n1, int validationSetIndex) {
        ArrayList<Node> nearestNeighbors = getNearestNeighbors(n1, validationSetIndex);
        float avgValue = 0;
        double sumDistance = 0;
        for (Node node : nearestNeighbors) {
            double distance = 1/Math.pow(realValDist.distance(node, n1) + catValDist.distance(node, n1), 2);
            if (Double.isNaN(distance) || Double.isInfinite(distance)) {
                distance = 1;
            }
            sumDistance += distance;
            if (true) {
                avgValue += Float.parseFloat(node.targetAttributeValue)*distance;
            } else {
                avgValue += Float.parseFloat(node.targetAttributeValue);
            }
        }
        if (true) {
            return avgValue / sumDistance;
        } else {
            return avgValue / nearestNeighbors.size();
        }
    }

    /**
     * Performs a k-NearestNeighbor regression
     * Prints out the results
     */
    private void regression() {
        ArrayList<Float> meanAbsoluteErrors = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            float absoluteError = 0;
            for (Node node : samples.get(i)) {
                absoluteError += Math.abs(regress(node, i) - Float.parseFloat(node.targetAttributeValue));
            }
            meanAbsoluteErrors.add(absoluteError / samples.get(i).size());
        }
        float averageAbsError = 0;
        for (Float f : meanAbsoluteErrors) {
            averageAbsError += f;
        }
        averageAbsError = averageAbsError / crossValidationFolds;

        System.out.println("Results for " + k + "-NearestNeighbor regression with " + crossValidationFolds + "-fold cross-validation for the data set " + filename + ":");
        System.out.println("Target attribute: " + targetAttributeName);
        System.out.println("Real value distance function: " + realValDist);
        System.out.println("Categorical value distance function: " + catValDist);
        System.out.println("The target attribute was predicted with a mean absolute error of " + averageAbsError);
        System.out.println("Attributes omitted: " + skippedAttributes);
    }

    public static void main(String[] args) {
        NearestNeighbors nn1 = new NearestNeighbors(3, 10, "class", new ArrayList<>(), "ionosphere.arff", new PNorm(2), new HammingDistance());
        nn1.classification();
        System.out.println();
        NearestNeighbors nn2 = new NearestNeighbors(3, 10, "price", new ArrayList<>(Arrays.asList("engine-size", "compression-ratio", "peak-rpm", "normalized-losses")), "autos.arff", new PNorm(2), new HammingDistance());
        nn2.regression();
        System.out.println();
        NearestNeighbors nn3 = new NearestNeighbors(3, 10, "price", new ArrayList<>(Arrays.asList("engine-size", "compression-ratio", "peak-rpm", "normalized-losses")), "autos.arff", new PNorm(2), new IgnoreCategoricalValues());
        nn3.regression();
    }

}
