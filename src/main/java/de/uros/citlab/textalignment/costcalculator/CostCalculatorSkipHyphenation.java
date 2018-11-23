package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;

import java.util.ArrayList;
import java.util.List;

public class CostCalculatorSkipHyphenation implements PathCalculatorGraph.ICostCalculatorMulti<ConfMatVector, NormalizedCharacter> {

    private final CostCalculatorCharChar ccCC;
    private final CostCalculatorCharNac ccCN;
    private NormalizedCharacter[] refs;

    public CostCalculatorSkipHyphenation(int offsetNaC, int offsetChar) {
        ccCC = new CostCalculatorCharChar(offsetNaC + offsetChar);
        ccCN = new CostCalculatorCharNac(offsetNaC);
    }

    @Override
    public void init(PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> distanceMat, ConfMatVector[] confMatVectors, NormalizedCharacter[] normalizedCharacters) {
        ccCC.init(distanceMat, confMatVectors, normalizedCharacters);
        ccCN.init(distanceMat, confMatVectors, normalizedCharacters);
        this.refs = normalizedCharacters;
    }

    @Override
    public List<PathCalculatorGraph.DistanceSmall> getNeighboursSmall(int[] point, PathCalculatorGraph.DistanceSmall distanceSmall) {
        final int idxRef = point[1];
        NormalizedCharacter char1 = refs[idxRef];
        //first character ==> char1==null
        //character is hyphen-character ==> no long edge have to be done
        //reference-length is to short ==> no long edge can be added
        if (char1 == null || char1.isNaC || char1.isHyphen) {
            return null;
        }
        final int idxRef2 = idxRef + 1;
        if (idxRef2 >= refs.length) {
            return null;
        }
        NormalizedCharacter char2 = refs[idxRef2];
        if (!char2.isHyphen) {
            return null;
        }
        List<PathCalculatorGraph.DistanceSmall> res = new ArrayList<>(2);
        res.add(ccCN.getNeighbourSmall(point, distanceSmall));
        res.add(ccCC.getNeighbourSmall(point, distanceSmall));
        return res;
    }

    @Override
    public PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> getNeighbour(PathCalculatorGraph.DistanceSmall distanceSmall) {
        throw new RuntimeException("no direct neighbour possible");
    }

}
