package algorithm;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of a single instance.
 */
public class DataPoint {

    /**
     * An array of all the real values of the instance
     */
    public final Float[] realValues;

    /**
     * An array of all the categorical values of the instance
     */
    public final String[] categoricalValues;

    /**
     * The value of the target attribute for this instance
     */
    final String targetAttributeValue;

    DataPoint(ArrayList<Float> realValues, ArrayList<String> categoricalValues, String targetAttributeValue) {
        this.realValues = new Float[realValues.size()];
        this.categoricalValues = new String[categoricalValues.size()];
        for (int i = 0; i < realValues.size(); i++) {
            this.realValues[i] = realValues.get(i);
        }
        for (int i = 0; i < categoricalValues.size(); i++) {
            this.categoricalValues[i] = categoricalValues.get(i);
        }
        this.targetAttributeValue = targetAttributeValue;
    }

    @Override
    public String toString() {
        return "Class: " + targetAttributeValue + " " + Arrays.toString(realValues) + " " + Arrays.toString(categoricalValues);
    }
}
