package distanceFunctions;

import algorithm.Node;

import java.util.Objects;

public class HammingDistance implements CategoricalValueDistanceFunction {

    @Override
    public double distance(Node n1, Node n2) {
        double distance = 0;
        for (int i = 0; i < n1.categoricalValues.length; i++) {
            if (!Objects.equals(n1.categoricalValues[i], n2.categoricalValues[i])) {
                distance++;
            }
        }
        return distance / n1.categoricalValues.length;
    }
}
