package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;

public class CostCalculatorNacChar extends CostCalculatorAbstract {

    private final int offset;

    public CostCalculatorNacChar(int offset) {
        this.offset = offset;
    }

    @Override
    public PathCalculatorGraph.DistanceSmall getNeighbourSmall(int[] ints, PathCalculatorGraph.DistanceSmall distanceSmall) {
        final int refIdx = ints[1];
        NormalizedCharacter normCharBefore = refs[refIdx];
        if (refIdx == 0 || !normCharBefore.isNaC) {
            return null;
        }
        final int refIdx2 = refIdx + offset;
        final int recoIdx = ints[0] + 1;
        if (recoIdx >= sizeReco || refIdx2 >= sizeRef) {
            return null;
        }
        final Double cost = getCost(refs[refIdx2], recos[recoIdx]);
        if (cost == null) {
            return null;
        }
        return new PathCalculatorGraph.DistanceSmall(ints, new int[]{recoIdx, refIdx2}, distanceSmall.costsAcc + cost, this);
    }

    @Override
    public PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> getNeighbour(PathCalculatorGraph.DistanceSmall distanceSmall) {
        int[] ints = distanceSmall.point;
        final NormalizedCharacter normChar = refs[ints[1]];
//            char[] ref = normChar.normalized;
        final ConfMatVector recoPart = recos[ints[0]];
        final Double cost = getCost(normChar, recoPart);
        return new PathCalculatorGraph.Distance(distanceSmall, "NAC_CHAR", cost, new ConfMatVector[]{recoPart}, new NormalizedCharacter[]{normChar});

    }
}
