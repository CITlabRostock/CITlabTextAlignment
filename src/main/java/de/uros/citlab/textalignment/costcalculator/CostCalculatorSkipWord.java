package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class CostCalculatorSkipWord extends CostCalculatorAbstract {
    private static Logger LOG = LoggerFactory.getLogger(CostCalculatorSkipWord.class);
    private boolean[] isSkipPoint;
    private boolean[] isReturnPoint;
    //    List<Integer> skipPoints;
//    List<Integer> returnPoints;
    private final double charCosts;

    public CostCalculatorSkipWord(double charCosts) {
        this.charCosts = charCosts;
    }

    @Override
    public void init(PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> dm, ConfMatVector[] recos, NormalizedCharacter[] refs) {
        super.init(dm, recos, refs);
        isSkipPoint = new boolean[refs.length];
        for (int j = 1; j < refs.length; j++) {
            isSkipPoint[j] = isSkipPointHard(refs[j]);
        }
        isReturnPoint = new boolean[recos.length];
        for (int i = 1; i < recos.length; i++) {
            isReturnPoint[i] = recos[i].isReturn;
        }
    }

    @Override
    public PathCalculatorGraph.DistanceSmall getNeighbourSmall(int[] point, PathCalculatorGraph.DistanceSmall distanceSmall) {
        int refIdx = point[1];
        if (!isSkipPoint[refIdx]) {
            //nothing to do - actual character is no skip-character
            return null;
        }
        int recoIdx = point[0];
        if (!isReturnPoint[recoIdx]) {
            //nothing to do - actual position is no skip-character
            return null;
        }
        //actual character is skip-character. Do something!
//            List<IDistance<double[], Character>> res = new LinkedList<>();
        //TODO: make more efficient: calculate cost and final position only once
        int refIdx2 = refIdx + 1;
        while (refIdx2 < isSkipPoint.length) {
            if (isSkipPoint[refIdx2]) {
                break;
            }
            refIdx2++;
        }
        if (refIdx2 == isSkipPoint.length) {
            return null;
        }
        int count = 0;
        for (int i = refIdx + 1; i <= refIdx2; i++) {
            if (!refs[i].isHyphen) {
                count++;
            }
        }
        double cost = charCosts * count;
        return new PathCalculatorGraph.DistanceSmall(point, new int[]{recoIdx, refIdx2}, distanceSmall.costsAcc + cost, this);
    }

    @Override
    public PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> getNeighbour(PathCalculatorGraph.DistanceSmall distanceSmall) {
        int refIdx = distanceSmall.pointPrevious[1];
        int refIdx2 = distanceSmall.point[1];
        int count = 0;
        NormalizedCharacter[] skips = new NormalizedCharacter[refIdx2 - refIdx];
        for (int i = 0; i < skips.length; i++) {
            NormalizedCharacter normedChar = this.refs[refIdx + i + 1];
            skips[i] = normedChar;
            if (!normedChar.isHyphen) {
                count++;
            }
        }
        double cost = charCosts * count;
        return new PathCalculatorGraph.Distance(distanceSmall, "SKIP_WORD", cost, null, skips);
    }

}
