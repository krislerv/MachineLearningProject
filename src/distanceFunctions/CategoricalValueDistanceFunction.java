package distanceFunctions;

import algorithm.DataPoint;

public interface CategoricalValueDistanceFunction {

    double distance(DataPoint dp1, DataPoint dp2);
}
