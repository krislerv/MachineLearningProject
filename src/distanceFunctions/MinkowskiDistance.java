package distanceFunctions;

import algorithm.DataPoint;

/**
 * Real value distance function defined as follows:
 *      The distance between two values is the absolute difference
 *      between the values raised to the p power.
 * The final sum is then raised to the 1/p power.
 */
public class MinkowskiDistance implements RealValueDistanceFunction {

    private int p;

    public MinkowskiDistance(int p) {
        this.p = p;
    }

    @Override
    public double distance(DataPoint dp1, DataPoint dp2) {
        double distance = 0;
        for (int i = 0; i < dp1.realValues.length; i++) {
            distance += Math.pow(Math.abs(dp1.realValues[i] - dp2.realValues[i]), p);
        }
        return Math.pow(distance, 1.0/p);
    }

    @Override
    public String toString() {
        return "Minkowski distance (p = " + p + ")";
    }
}
