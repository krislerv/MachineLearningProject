algorithm/NearestNeighbors.java is the main class for this project.

To compile:
In this directory, type: javac algorithm/NearestNeighbors.java

To run:
In this directory, type: java algorithm/NearestNeighbors

This will print the results of the experiments mentioned in the report. To do your own experiments, modify the main method in algorithm/NearestNeighbors.java










Output of the experiments mentioned in the report:

Results for 2-NearestNeighbor classification with 10-fold cross-validation for the data set ionosphere.arff:
Target attribute: class (Possible values: [b, g])
Real value distance function: Minkowski distance (p = 2)
Categorical value distance function: None
The target attribute was correctly classified 92.02381% of the time
Attributes omitted: [a01, a03, a06, a10, a12, a16, a17, a18, a20, a22, a23, a24, a26, a27, a29, a30, a31, a32, a33]

Results for 3-NearestNeighbor classification with 10-fold cross-validation for the data set ionosphere.arff:
Target attribute: class (Possible values: [b, g])
Real value distance function: Minkowski distance (p = 2)
Categorical value distance function: None
The target attribute was correctly classified 91.15872% of the time
Attributes omitted: [a03, a10, a12, a16, a17, a18, a20, a22, a23, a24, a26, a27, a29, a30, a31, a32, a33]

Results for 2-NearestNeighbor regression with 10-fold cross-validation for the data set autos.arff:
Target attribute: price
Real value distance function: Minkowski distance (p = 2)
Categorical value distance function: Hamming Distance
The target attribute was predicted with a mean absolute error of 1327.366
Attributes omitted: [normalized-losses, fuel-type, aspiration, num-of-doors, body-style, length, height, engine-type, num-of-cylinders, fuel-system, bore, stroke, compression-ratio, peak-rpm, symboling]

Results for 5-NearestNeighbor regression with 10-fold cross-validation for the data set autos.arff:
Target attribute: price
Real value distance function: Minkowski distance (p = 2)
Categorical value distance function: Hamming Distance
The target attribute was predicted with a mean absolute error of 1237.6842
Attributes omitted: [normalized-losses, fuel-type, aspiration, num-of-doors, body-style, length, width, height, engine-type, num-of-cylinders, fuel-system, stroke, compression-ratio, symboling]