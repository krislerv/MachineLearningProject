package distanceFunctions;

import algorithm.DataPoint;

/**
 * Categorical value distance function defined as follows:
 *      Ignores every categorical value. Always returns 0
 */
public class IgnoreCategoricalValues implements CategoricalValueDistanceFunction {

    @Override
    public double distance(DataPoint dp1, DataPoint dp2) {
        return 0;
    }

    @Override
    public String toString() {
        return "None";
    }
}
