package algorithm;

import java.util.Arrays;

public class Node {

    public final Float[] realValues;
    public final String[] categoricalValues;

    final String classification;

    Node(Float[] realValues, String[] categoricalValues, String classification) {
        this.realValues = realValues;
        this.categoricalValues = categoricalValues;
        this.classification = classification;
    }

    @Override
    public String toString() {
        return "Class: " + classification + " " + Arrays.toString(realValues);
    }
}
