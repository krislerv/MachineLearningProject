package distanceFunctions;

import algorithm.Node;

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
}
