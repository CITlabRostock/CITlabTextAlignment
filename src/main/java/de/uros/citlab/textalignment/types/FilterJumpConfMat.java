package de.uros.citlab.textalignment.types;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.costcalculator.CostCalculatorJumpConfMat;

public class FilterJumpConfMat implements PathCalculatorGraph.PathFilter<ConfMatVector, NormalizedCharacter> {

    private int idxMax;
    private double costMax;

    private static boolean isSkipPointSoft(NormalizedCharacter nc) {
        return nc.type != NormalizedCharacter.Type.Dft;
    }

    @Override
    public void init(ConfMatVector[] confMatVectors, NormalizedCharacter[] normalizedCharacters) {
        idxMax = -1;
        costMax = 0.0;
    }

    @Override
    public boolean addNewEdge(PathCalculatorGraph.DistanceSmall distanceSmall) {
        return !(distanceSmall.point[1] < idxMax && distanceSmall.costsAcc > costMax);
    }

    @Override
    public boolean followPathsFromBestEdge(PathCalculatorGraph.DistanceSmall distanceSmall) {
        if (distanceSmall.costCalculator instanceof CostCalculatorJumpConfMat) {
            if (idxMax < distanceSmall.point[1]) {
                idxMax = distanceSmall.point[1];
                costMax = distanceSmall.costsAcc;
                return true;
            } else if (idxMax == distanceSmall.point[1]) {
                costMax = Math.min(costMax, distanceSmall.costsAcc);
            } else {
                return false;
            }
        }
        return addNewEdge(distanceSmall);
    }

}
