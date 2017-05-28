package distanceFunctions;

import algorithm.Node;

/**
 * Real value distance function defined as follows:
 *      The distance between two values is the absolute difference
 *      between the values raised to the p power.
 * The final sum is then raised to the 1/p power.
 */
public class PNorm implements RealValueDistanceFunction {

    private int p;

    public PNorm(int p) {
        this.p = p;
    }

    @Override
    public double distance(Node n1, Node n2) {
        double distance = 0;
        for (int i = 0; i < n1.realValues.length; i++) {
            distance += Math.pow(Math.abs(n1.realValues[i] - n2.realValues[i]), p);
        }
        return Math.pow(distance, 1.0/p);
    }

    @Override
    public String toString() {
        return p + "-Norm";
    }
}
