package algorithm;

import distanceFunctions.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Implementation of a k-NearestNeighbor algorithm
 */
public class NearestNeighbors {

    /**
     * An ArrayList containing ArrayLists containing DataPoints. Each ArrayList<DataPoint> is a
     * fold used in cross-validation. With 10-fold cross validation, the
     * size of folds would be 10
     */
    private final ArrayList<ArrayList<DataPoint>> folds;

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
     * If the neighbors should be distance weighted
     */
    private final boolean useDistanceWeighting;

    /**
     * realValDist: Which distance function to use for real value attributes
     * catValDist: Which distance function to use for categorical value attributes
     */
    private final RealValueDistanceFunction realValDist;
    private final CategoricalValueDistanceFunction catValDist;

    /**
     * targetAttributeName: The name of the target attribute
     * filename: The filename of the data set we're using
     * ignoredAttributes: A list of the names of attributes we're ignoring
     */
    private final String targetAttributeName, filename;
    private final ArrayList<String> ignoredAttributes;

    /**
     * Initializes the algorithm with the given values. Loads the data, normalizes the
     * data, randomizes the order and divides the data into folds for cross-validation
     * @param k how many neighbors to consider
     * @param crossValidationFolds how many folds to use for cross-validation
     * @param targetAttributeName the name of the target attribute
     * @param ignoredAttributes list of the names of attributes we're ignoring
     * @param filename filename of the data set we're using
     * @param realValDist distance function to use for real value attributes
     * @param catValDist distance function to use for categorical value attributes
     */
    private NearestNeighbors(int k, int crossValidationFolds, boolean useDistanceWeighting, String targetAttributeName, ArrayList<String> ignoredAttributes, String filename, RealValueDistanceFunction realValDist, CategoricalValueDistanceFunction catValDist) {
        // Initialize fields
        this.k = k;
        this.crossValidationFolds = crossValidationFolds;
        this.useDistanceWeighting = useDistanceWeighting;
        this.targetAttributeName = targetAttributeName;
        this.filename = filename;
        this.ignoredAttributes = ignoredAttributes;
        this.realValDist = realValDist;
        this.catValDist = catValDist;
        folds = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            folds.add(new ArrayList<>());
        }
        // Load data
        int targetAttributeIndex = -1;
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
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
                if (ignoredAttributes.contains(attributeName)) {
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
            // When we get to the data, create a DataPoint-object for each row
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
                dataPoints.add(new DataPoint(attributeListReal, attributeListCategorical, targetAttribute));
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Normalize numerical values
        for (int i = 0; i < dataPoints.get(0).realValues.length; i++) {
            float min = Float.MAX_VALUE, max = Float.MIN_VALUE;
            for (DataPoint dataPoint : dataPoints) {
                if (dataPoint.realValues[i] < min) {
                    min = dataPoint.realValues[i];
                } else if (dataPoint.realValues[i] > max) {
                    max = dataPoint.realValues[i];
                }
            }
            for (DataPoint dataPoint : dataPoints) {
                dataPoint.realValues[i] = (dataPoint.realValues[i] - min) / (max - min);
            }
        }
        // Randomize instance order and divide into folds
        Collections.shuffle(dataPoints, new Random(4));
        int nextFoldIndex = 0;
        while (!dataPoints.isEmpty()) {
            folds.get(nextFoldIndex).add(dataPoints.remove(0));
            nextFoldIndex = nextFoldIndex == crossValidationFolds - 1 ? 0 : nextFoldIndex+1;
        }
    }

    /**
     * Returns the k nearest neighbors to the given data point
     * @param dp1 the given data point
     * @param validationSetIndex the index of the validation set (which will be skipped)
     * @return the k nearest neighbors to the given data point
     */
    private ArrayList<DataPoint> getNearestNeighbors(DataPoint dp1, int validationSetIndex) {
        ArrayList<DataPoint> nearestNeighbors = new ArrayList<>();
        ArrayList<DataPoint> trainingSet = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            if (i == validationSetIndex) {
                continue;
            }
            trainingSet.addAll(folds.get(i));
        }
        for (DataPoint dataPoint : trainingSet) {
            if (nearestNeighbors.isEmpty()) {
                nearestNeighbors.add(dataPoint);
            } else {
                for (int j = 0; j < nearestNeighbors.size(); j++) {
                    if (realValDist.distance(dataPoint, dp1) + catValDist.distance(dataPoint, dp1) < realValDist.distance(nearestNeighbors.get(j), dp1) + catValDist.distance(nearestNeighbors.get(j), dp1)) {
                        nearestNeighbors.add(j, dataPoint);
                        break;
                    }
                }
                if (nearestNeighbors.size() < k && !nearestNeighbors.contains(dataPoint)) {
                    nearestNeighbors.add(dataPoint);
                }
                if (nearestNeighbors.size() > k) {
                    nearestNeighbors.remove(k);
                }
            }
        }
        return nearestNeighbors;
    }

    /**
     * Classifies a single data point
     * @param dp1 the data point to be classified
     * @param validationSetIndex the index of the validation set (which will be skipped when generating neighbors)
     * @return the classification value of the data point
     */
    private String classify(DataPoint dp1, int validationSetIndex) {
        ArrayList<DataPoint> nearestNeighbors = getNearestNeighbors(dp1, validationSetIndex);
        ArrayList<Double> attributeValueCounts = new ArrayList<>();

        for (String ignored : classificationTargetAttributeValues) {
            attributeValueCounts.add(0.0);
        }

        for (DataPoint dataPoint : nearestNeighbors) {
            for (int i = 0; i < classificationTargetAttributeValues.length; i++) {
                if (dataPoint.targetAttributeValue.equals(classificationTargetAttributeValues[i])) {
                    if (useDistanceWeighting) {
                        double distance = Math.pow(realValDist.distance(dataPoint, dp1) + catValDist.distance(dataPoint, dp1), 2);
                        if (distance == 0) {
                            distance = 1;
                        }
                        attributeValueCounts.set(i, attributeValueCounts.get(i) + 1.0/distance);
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
            for (DataPoint dataPoint : folds.get(i)) {
                if (classify(dataPoint, i).equals(dataPoint.targetAttributeValue)) {
                    correct++;
                }
            }
            successRates.add((float) correct / folds.get(i).size());
        }
        float averageSuccessRate = 0;
        for (Float f : successRates) {
            averageSuccessRate += f;
        }
        averageSuccessRate = averageSuccessRate / crossValidationFolds;

        System.out.println("Results for " + k + "-NearestNeighbor classification with " + crossValidationFolds + "-fold cross-validation for the data set " + filename + ":");
        System.out.println("Target attribute: " + targetAttributeName + " (Possible values: " + Arrays.toString(classificationTargetAttributeValues) + ")");
        System.out.println("Real value distance function: " + realValDist);
        System.out.println("Categorical value distance function: " + catValDist);
        System.out.println("The target attribute was correctly classified " + 100*averageSuccessRate + "% of the time");
        System.out.println("Attributes omitted: " + ignoredAttributes);
    }

    /**
     * Predicts the value of a single data point
     * @param dp1 the data point to predict the value for
     * @param validationSetIndex the index of the validation set (which will be skipped when generating neighbors)
     * @return the predicted value of the data point
     */
    private double regress(DataPoint dp1, int validationSetIndex) {
        ArrayList<DataPoint> nearestNeighbors = getNearestNeighbors(dp1, validationSetIndex);
        float avgValue = 0;
        double sumDistance = 0;
        for (DataPoint dataPoint : nearestNeighbors) {
            double distance = 1/Math.pow(realValDist.distance(dataPoint, dp1) + catValDist.distance(dataPoint, dp1), 2);
            if (Double.isNaN(distance) || Double.isInfinite(distance)) {
                distance = 1;
            }
            sumDistance += distance;
            if (useDistanceWeighting) {
                avgValue += Float.parseFloat(dataPoint.targetAttributeValue)*distance;
            } else {
                avgValue += Float.parseFloat(dataPoint.targetAttributeValue);
            }
        }
        if (useDistanceWeighting) {
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
            for (DataPoint dataPoint : folds.get(i)) {
                absoluteError += Math.abs(regress(dataPoint, i) - Float.parseFloat(dataPoint.targetAttributeValue));
            }
            meanAbsoluteErrors.add(absoluteError / folds.get(i).size());
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
        System.out.println("Attributes omitted: " + ignoredAttributes);
    }

    public static void main(String[] args) {
        NearestNeighbors nn1 = new NearestNeighbors(2, 10, false, "class", new ArrayList<>(Arrays.asList("a01", "a03", "a06", "a10", "a12", "a16", "a17", "a18", "a20", "a22", "a23", "a24", "a26", "a27", "a29", "a30", "a31", "a32", "a33")), "ionosphere.arff", new MinkowskiDistance(2), new IgnoreCategoricalValues());
        nn1.classification();
        System.out.println();
        NearestNeighbors nn1a = new NearestNeighbors(3, 10, true, "class", new ArrayList<>(Arrays.asList("a03", "a10", "a12", "a16", "a17", "a18", "a20", "a22", "a23", "a24", "a26", "a27", "a29", "a30", "a31", "a32", "a33")), "ionosphere.arff", new MinkowskiDistance(2), new IgnoreCategoricalValues());
        nn1a.classification();
        System.out.println();
        NearestNeighbors nn2 = new NearestNeighbors(2, 10, false, "price", new ArrayList<>(Arrays.asList("normalized-losses","fuel-type","aspiration","num-of-doors","body-style","length","height","engine-type","num-of-cylinders","fuel-system","bore","stroke","compression-ratio","peak-rpm","symboling")), "autos.arff", new MinkowskiDistance(2), new HammingDistance());
        nn2.regression();
        System.out.println();
        NearestNeighbors nn3 = new NearestNeighbors(5, 10, true, "price", new ArrayList<>(Arrays.asList("normalized-losses","fuel-type","aspiration","num-of-doors","body-style","length","width","height","engine-type","num-of-cylinders","fuel-system","stroke","compression-ratio","symboling")), "autos.arff", new MinkowskiDistance(2), new HammingDistance());
        nn3.regression();
    }

}
