package de.uros.citlab.textalignment.types;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;

import java.util.Arrays;

public class FilterOffset implements PathCalculatorGraph.PathFilter<ConfMatVector, NormalizedCharacter> {

    private final double offset;
    private double[] minvalues;

    public FilterOffset(double offset) {
        this.offset = offset;
    }

    @Override
    public void init(ConfMatVector[] confMatVectors, NormalizedCharacter[] normalizedCharacters) {
        minvalues = new double[normalizedCharacters.length];
        Arrays.fill(minvalues, Double.MAX_VALUE);
    }

    @Override
    public boolean addNewEdge(PathCalculatorGraph.DistanceSmall distanceSmall) {
        return distanceSmall.costsAcc < minvalues[distanceSmall.point[1]] + offset;
    }

    @Override
    public boolean followPathsFromBestEdge(PathCalculatorGraph.DistanceSmall distanceSmall) {
        final double minValue = minvalues[distanceSmall.point[1]];
        final double actValue = distanceSmall.costsAcc;
        if (minValue + offset <= actValue) {
            return false;
        }
        if (actValue < minValue) {
            minvalues[distanceSmall.point[1]] = actValue;
        }
        return true;
    }

}
