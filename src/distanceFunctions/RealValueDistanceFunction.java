package distanceFunctions;

import algorithm.DataPoint;

public interface RealValueDistanceFunction {

    double distance(DataPoint dp1, DataPoint dp2);
}
