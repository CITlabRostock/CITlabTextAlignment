package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;

import java.util.Objects;

public class CostCalculatorCharChar extends CostCalculatorAbstract {

    private final int offset;
    private boolean[] isValid;

    public CostCalculatorCharChar(int offset) {
        this.offset = offset;
    }

    @Override
    public void init(PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> dm, ConfMatVector[] list, NormalizedCharacter[] list1) {
        super.init(dm, list, list1);
        isValid = new boolean[sizeRef];
        for (int i = 0; i + offset < isValid.length; i++) {
            NormalizedCharacter c1 = refs[i];
            NormalizedCharacter c2 = refs[i + offset];
            if (c1 == null) {
                isValid[i] = true;
                continue;
            }
            if (c1.isNaC || c2.isNaC) {
                continue;
            }
            if (!Objects.equals(c1.orig, c2.orig)) {
                isValid[i] = true;
            }
        }
    }

    @Override
    public PathCalculatorGraph.DistanceSmall getNeighbourSmall(int[] point, PathCalculatorGraph.DistanceSmall distanceSmall) {
        final int refIdx = point[1];
        final int recoIdx = point[0] + 1;
        final int refIdx2 = refIdx + offset;
        if (!isValid[refIdx] || recoIdx >= sizeReco || refIdx2 >= sizeRef) {
            return null;
        }
        NormalizedCharacter normChar2 = refs[refIdx2];
        ConfMatVector recoPart = recos[recoIdx];
        Double cost = getCost(normChar2, recoPart);
        if (cost == null) {
            return null;
        }
        return new PathCalculatorGraph.DistanceSmall(point, new int[]{recoIdx, refIdx2}, distanceSmall.costsAcc + cost, this);
    }

    @Override
    public PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> getNeighbour(PathCalculatorGraph.DistanceSmall distanceSmall) {
        final int[] pointPrevious = distanceSmall.pointPrevious;
        final int refIdx = pointPrevious[1];
        final int recoIdx = pointPrevious[0] + 1;
        final int refIdx2 = refIdx + offset;
        if (!isValid[refIdx] || recoIdx >= sizeReco || refIdx2 >= sizeRef) {
            return null;
        }
        NormalizedCharacter normChar2 = refs[refIdx2];
        ConfMatVector recoPart = recos[recoIdx];
        Double cost = getCost(normChar2, recoPart);
        if (cost == null) {
            return null;
        }
        return new PathCalculatorGraph.Distance(distanceSmall, "CHAR_CHAR", cost, new ConfMatVector[]{recoPart}, new NormalizedCharacter[]{normChar2});
    }
}
