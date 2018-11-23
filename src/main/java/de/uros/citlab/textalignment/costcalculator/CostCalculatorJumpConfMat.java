package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;

import java.util.*;

public class CostCalculatorJumpConfMat implements PathCalculatorGraph.ICostCalculatorMulti<ConfMatVector, NormalizedCharacter> {

    protected PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> dm;
    protected ConfMatVector[] recos;
    protected NormalizedCharacter[] refs;
    protected int sizeReco;
    protected int sizeRef;
    //        protected final CharMap<Integer> cm;
    protected int idxNac;
    protected int idxReturn;
    //    Set<Integer> skipPoints;
    boolean[] isSkipPoint;
    Set<Integer> returnPoints;
    boolean[] isReturnPoints;
    final double jumpCosts;

    public CostCalculatorJumpConfMat(double jumpCosts) {
        this.jumpCosts = jumpCosts;
    }

    private static boolean isSkipPointSoft(NormalizedCharacter nc) {
        return nc.type != NormalizedCharacter.Type.Dft;
    }

    @Override
    public void init(PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> distanceMat, ConfMatVector[] confMatVectors, NormalizedCharacter[] normalizedCharacters) {
        this.dm = distanceMat;
        this.recos = confMatVectors;
        this.refs = normalizedCharacters;
        this.sizeReco = recos.length;
        this.sizeRef = refs.length;
        isReturnPoints = new boolean[sizeReco];
        isSkipPoint = new boolean[sizeRef];
        returnPoints=new LinkedHashSet<>();
        for (int j = 1; j < refs.length; j++) {
            isSkipPoint[j] = isSkipPointSoft(refs[j]);
        }
        for (int i = 1; i < recos.length; i++) {
            if (recos[i].isReturn) {
                isReturnPoints[i] = true;
                returnPoints.add(i);
            }
        }
    }

    @Override
    public List<PathCalculatorGraph.DistanceSmall> getNeighboursSmall(int[] point, PathCalculatorGraph.DistanceSmall distanceSmall) {
        final int recoIdx = point[0];
        if (!isReturnPoints[recoIdx]) {
            //nothing to do - actual position is no skip-character
            return null;
        }
        final int refIdx = point[1];
        if (!isSkipPoint[refIdx]) {
            //nothing to do - actual character is no skip-character
            return null;
        }
        List<PathCalculatorGraph.DistanceSmall> res = new ArrayList<>(returnPoints.size());
        //actual character is skip-character. Do something!
        for (Integer returnPoint : returnPoints) {
            if (recoIdx != returnPoint) {
                res.add(new PathCalculatorGraph.DistanceSmall(point, new int[]{returnPoint, refIdx}, distanceSmall.costsAcc + jumpCosts, this));
            }
        }
        return res;
    }

    @Override
    public PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> getNeighbour(PathCalculatorGraph.DistanceSmall distanceSmall) {
        return new PathCalculatorGraph.Distance(distanceSmall, "JUMP_CONFMAT", jumpCosts, null, null);
    }
}
