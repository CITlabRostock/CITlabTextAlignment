package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;

import java.util.LinkedList;
import java.util.List;

public class CostCalculatorSkipChar implements PathCalculatorGraph.ICostCalculatorMulti<ConfMatVector, NormalizedCharacter> {

    private final CostCalculatorCharChar ccCC;
    private final CostCalculatorCharNac ccCN;
    private NormalizedCharacter[] refs;

    public CostCalculatorSkipChar(int offsetNaC, int offsetChar) {
        ccCC = new CostCalculatorCharChar(offsetNaC + offsetChar);
        ccCN = new CostCalculatorCharNac(offsetNaC);
    }

    @Override
    public void init(PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> dm, ConfMatVector[] confMatVectors, NormalizedCharacter[] normalizedCharacters) {
        ccCC.init(dm, confMatVectors, normalizedCharacters);
        ccCN.init(dm, confMatVectors, normalizedCharacters);
        refs = normalizedCharacters;
    }

    @Override
    public List<PathCalculatorGraph.DistanceSmall> getNeighboursSmall(int[] point, PathCalculatorGraph.DistanceSmall distanceSmall) {
        final int idxRef = point[1];
        NormalizedCharacter char1 = refs[idxRef];
        //first character ==> char1==null
        //first character is Nac ==> null
        //next character is out of range ==> null
        if (char1 == null || char1.isNaC) {
            return null;
        }
        final int idxRef2 = idxRef + 2;
        //next character not skipable ==> null
        if (idxRef2 >= refs.length || !refs[idxRef2].isSkipable) {
            return null;
        }
        List<PathCalculatorGraph.DistanceSmall> res = new LinkedList<>();
        res.add(ccCN.getNeighbourSmall(point, distanceSmall));
        res.add(ccCC.getNeighbourSmall(point, distanceSmall));
        return res;
    }

    @Override
    public PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> getNeighbour(PathCalculatorGraph.DistanceSmall distanceSmall) {
        throw new RuntimeException("should not be called for this instance");
    }
}
