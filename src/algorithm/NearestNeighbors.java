package algorithm;

import distanceFunctions.CategoricalValueDistanceFunction;
import distanceFunctions.HammingDistance;
import distanceFunctions.PNorm;
import distanceFunctions.RealValueDistanceFunction;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

public class NearestNeighbors {

    private ArrayList<ArrayList<Node>> samples;

    private String[] classificationTargetAttributeValues;

    private final int k, crossValidationFolds;

    private RealValueDistanceFunction realValDist;
    private CategoricalValueDistanceFunction catValDist;

    private NearestNeighbors(int k, int crossValidationFolds, String targetAttributeName, ArrayList<String> skippedAttributes, String filename, RealValueDistanceFunction realValDist, CategoricalValueDistanceFunction catValDist) {
        // Initialize fields
        this.k = k;
        this.crossValidationFolds = crossValidationFolds;
        this.realValDist = realValDist;
        this.catValDist = catValDist;
        samples = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            samples.add(new ArrayList<>());
        }

        boolean targetAttributeIsCategorical = false;
        int targetAttributeIndex = 0;

        // Load data
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<String> attributeTypes = new ArrayList<>();
        int realAttributeCount = 0;
        int categoricalAttributeCount = 0;
        try {
            Scanner in = new Scanner(new FileReader(filename));
            ArrayList<Integer> skippedAttributesIndexes = new ArrayList<>();
            int currentAttributeIndex = 0;
            //String line = "";
            while (!in.nextLine().startsWith("@relation")) {
                // do nothing
            }
            while (in.hasNext(Pattern.compile("@attribute"))) {
                String line = in.nextLine();
                String attributeName = line.split(" ")[1];
                /*if (skippedAttributes.contains(attributeName)) {
                    skippedAttributesIndexes.add(currentAttributeIndex);
                    currentAttributeIndex++;
                    continue;
                }*/
                if (line.split(" ")[2].equals("real")) {
                    if (attributeName.equals(targetAttributeName)) {
                        targetAttributeIndex = currentAttributeIndex;
                    }
                    attributeTypes.add("real");
                    realAttributeCount++;
                } else {
                    attributeTypes.add("categorical");
                    if (attributeName.equals(targetAttributeName)) {
                        targetAttributeIsCategorical = true;
                        classificationTargetAttributeValues = line.split("\\{")[1].substring(0, line.split("\\{")[1].length() - 1).split(", ");
                        System.out.println(Arrays.toString(classificationTargetAttributeValues));
                    }
                    categoricalAttributeCount++;
                }
                currentAttributeIndex++;

            }
            while (!in.nextLine().startsWith("@data")) {
                // do nothing
            }
            while (!in.hasNext(Pattern.compile("%"))) {
                String line = in.nextLine();
                String[] attributeList = line.split(",");
                Float[] attributeListReal = new Float[targetAttributeIsCategorical ? realAttributeCount : realAttributeCount - 1];
                String[] attributeListCategorical = new String[categoricalAttributeCount];
                String targetAttribute = "";
                int nextReal = 0;
                int nextCategorical = 0;
                for (int i = 0; i < attributeList.length; i++) {
                    /*if (skippedAttributesIndexes.contains(i)) {
                        continue;
                    }*/
                    if (i == 34) {
                        targetAttribute = attributeList[i];
                    } else if (attributeTypes.get(i).equals("real")) {
                        attributeListReal[nextReal] = Float.parseFloat(attributeList[i]);
                        nextReal++;
                    } else {
                        attributeListCategorical[nextCategorical] = attributeList[i];
                        nextCategorical++;
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
        Collections.shuffle(nodes, new Random(0));
        int nextSampleIndex = 0;
        while (!nodes.isEmpty()) {
            samples.get(nextSampleIndex).add(nodes.remove(0));
            nextSampleIndex = nextSampleIndex == crossValidationFolds - 1 ? 0 : nextSampleIndex+1;
        }
    }

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
                if (nearestNeighbors.size() > k) {
                    nearestNeighbors.remove(k);
                }
            }
        }
        return nearestNeighbors;
    }

    private String classify(Node n1, int validationSetIndex) {
        ArrayList<Node> nearestNeighbors = getNearestNeighbors(n1, validationSetIndex);
        ArrayList<Integer> attributeValueCounts = new ArrayList<>();

        for (String ignored : classificationTargetAttributeValues) {
            attributeValueCounts.add(0);
        }

        for (Node node : nearestNeighbors) {
            for (int i = 0; i < classificationTargetAttributeValues.length; i++) {
                if (Objects.equals(node.classification, classificationTargetAttributeValues[i])) {
                    attributeValueCounts.set(i, attributeValueCounts.get(i) + 1);
                }
            }
        }
        return classificationTargetAttributeValues[attributeValueCounts.indexOf(Collections.max(attributeValueCounts))];
    }

    private float classification() {
        ArrayList<Float> successRates = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            int correct = 0;
            for (Node node : samples.get(i)) {
                if (Objects.equals(classify(node, i), node.classification)) {
                    correct++;
                }
            }
            successRates.add((float) correct / samples.get(i).size());
        }
        float averageSuccessRate = 0;
        for (Float f : successRates) {
            averageSuccessRate += f;
        }
        System.out.println(successRates);
        return averageSuccessRate / crossValidationFolds;
    }

    private float regress(Node n1, int validationSetIndex) {
        ArrayList<Node> nearestNeighbors = getNearestNeighbors(n1, validationSetIndex);
        float avgValue = 0;
        for (Node node : nearestNeighbors) {
            avgValue += Float.parseFloat(node.classification);
        }
        return avgValue / nearestNeighbors.size();
    }

    private float regression() {
        ArrayList<Float> meanAbsoluteErrors = new ArrayList<>();
        for (int i = 0; i < crossValidationFolds; i++) {
            float absoluteError = 0;
            for (Node node : samples.get(i)) {
                absoluteError += Math.abs(regress(node, i) - Float.parseFloat(node.classification));
            }
            meanAbsoluteErrors.add(absoluteError / samples.get(i).size());
        }
        float averageAbsError = 0;
        for (Float f : meanAbsoluteErrors) {
            averageAbsError += f;
        }
        System.out.println(meanAbsoluteErrors);
        return averageAbsError / crossValidationFolds;
    }

    public static void main(String[] args) {
        NearestNeighbors nn1 = new NearestNeighbors(3, 10, "class", new ArrayList<>(), "ionosphere.arff", new PNorm(2), new HammingDistance());
        System.out.println(nn1.classification());
        NearestNeighbors nn2 = new NearestNeighbors(3, 10, "price", new ArrayList<>(Arrays.asList()), "autos.arff", new PNorm(2), new HammingDistance());
        System.out.println(nn2.regression());
    }

}
