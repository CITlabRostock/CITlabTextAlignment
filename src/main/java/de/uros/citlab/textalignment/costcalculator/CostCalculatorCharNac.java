package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;

public class CostCalculatorCharNac extends CostCalculatorAbstract {

    private final int offset;

    public CostCalculatorCharNac(int offset) {
        this.offset = offset;
    }

    @Override
    public PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> getNeighbour(PathCalculatorGraph.DistanceSmall dist) {
        final int[] pointPrevious = dist.pointPrevious;
        final int refIdx = pointPrevious[1];
        final int refIdx2 = refIdx + offset;
        NormalizedCharacter normCharBefore = refs[refIdx];
        if (refIdx != 0 && normCharBefore.isNaC) {
            return null;
        }
        int recoIdx = pointPrevious[0] + 1;
        if (recoIdx >= sizeReco || refIdx2 >= sizeRef) {
            return null;
        }
        NormalizedCharacter normChar = refs[refIdx2];
        ConfMatVector recoPart = recos[recoIdx];
        Double cost = getCost(normChar, recoPart);
        if (cost == null) {
            return null;
        }
//        final String op = refIdx == 0 ? "START_NAC" : "CHAR_NAC";
        return new PathCalculatorGraph.Distance(dist, "CHAR_NAC", cost, new ConfMatVector[]{recoPart}, new NormalizedCharacter[]{normChar});
    }

    @Override
    public PathCalculatorGraph.DistanceSmall getNeighbourSmall(int[] ints, PathCalculatorGraph.DistanceSmall distanceSmall) {
        int refIdx = ints[1];
        NormalizedCharacter normCharBefore = refs[refIdx];
        if (refIdx != 0 && normCharBefore.isNaC) {
            return null;
        }
        int recoIdx = ints[0] + 1;
        int refIdx2 = refIdx + offset;
        if (recoIdx >= sizeReco || refIdx2 >= sizeRef) {
            return null;
        }
        NormalizedCharacter normChar = refs[refIdx2];
//            char[] ref = normChar.normalized;
        ConfMatVector recoPart = recos[recoIdx];
        Double cost = getCost(normChar, recoPart);
        if (cost == null) {
            return null;
        }
        return new PathCalculatorGraph.DistanceSmall(ints, new int[]{recoIdx, refIdx2}, distanceSmall.costsAcc + cost, this);
    }
}
