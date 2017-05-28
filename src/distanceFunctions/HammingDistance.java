package distanceFunctions;

import algorithm.Node;

import java.util.Objects;

/**
 * Categorical value distance function defined as follows:
 *      1 if two categorical values are different
 *      0 if two categorical values are the same
 * The final distance is divided by the number of categorical values being classified to normalize
 */
public class HammingDistance implements CategoricalValueDistanceFunction {

    @Override
    public double distance(Node n1, Node n2) {
        if (n1.categoricalValues.length == 0) {
            return 0;
        }
        double distance = 0;
        for (int i = 0; i < n1.categoricalValues.length; i++) {
            if (!Objects.equals(n1.categoricalValues[i], n2.categoricalValues[i])) {
                distance += 1; // 0.2;
            }
        }
        return distance / n1.categoricalValues.length;
    }

    @Override
    public String toString() {
        return "Hamming Distance";
    }
}
