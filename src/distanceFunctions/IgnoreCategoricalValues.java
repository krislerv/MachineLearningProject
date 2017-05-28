package distanceFunctions;

import algorithm.Node;

/**
 * Categorical value distance function defined as follows:
 *      Ignores every categorical value. Always returns 0
 */
public class IgnoreCategoricalValues implements CategoricalValueDistanceFunction {

    @Override
    public double distance(Node n1, Node n2) {
        return 0;
    }

    @Override
    public String toString() {
        return "None";
    }
}
