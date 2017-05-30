package distanceFunctions;

import algorithm.DataPoint;

/**
 * Categorical value distance function defined as follows:
 *      1 if two categorical values are different
 *      0 if two categorical values are the same
 * The final distance is divided by the number of categorical values being classified to normalize
 */
public class HammingDistance implements CategoricalValueDistanceFunction {

    @Override
    public double distance(DataPoint dp1, DataPoint dp2) {
        if (dp1.categoricalValues.length == 0) {
            return 0;
        }
        double distance = 0;
        for (int i = 0; i < dp1.categoricalValues.length; i++) {
            if (!dp1.categoricalValues[i].equals(dp2.categoricalValues[i])) {
                distance += 1; // 0.2;
            }
        }
        return distance / dp1.categoricalValues.length;
    }

    @Override
    public String toString() {
        return "Hamming Distance";
    }
}
