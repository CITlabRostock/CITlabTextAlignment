package de.uros.citlab.textalignment.costcalculator;

import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.textalignment.types.ConfMatVector;
import de.uros.citlab.textalignment.types.NormalizedCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class CostCalculatorAbstract implements PathCalculatorGraph.ICostCalculator<ConfMatVector, NormalizedCharacter> {

    private static Logger LOG = LoggerFactory.getLogger(CostCalculatorAbstract.class);
    protected PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> dm;
    protected ConfMatVector[] recos;
    protected NormalizedCharacter[] refs;
    protected int sizeReco;
    protected int sizeRef;

    public CostCalculatorAbstract() {
    }

    protected static boolean isSkipPointSoft(NormalizedCharacter nc) {
        return nc.type != NormalizedCharacter.Type.Dft;
    }

    protected static boolean isSkipPointHard(NormalizedCharacter nc) {
        return isSkipPointSoft(nc) && nc.type != NormalizedCharacter.Type.HypenLineBreak;
    }

    protected Double getCost(NormalizedCharacter normedChar, ConfMatVector confMatVec) {
        if (normedChar.isAnyChar) {
            double cost = confMatVec.costAnyChar;
            if (Double.isFinite(cost)) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("for character in " + normedChar + " no propability found - take any character as cost");
                }
                return cost;
            }
            return null;
        }
        double cost = Double.POSITIVE_INFINITY;
        for (int key : normedChar.index) {
            cost = Math.min(cost, confMatVec.costVec[key]);
        }
        switch (normedChar.type) {
            case Return:
                cost = confMatVec.costReturn;
                break;
            case SpaceLineBreak:
                cost = Math.min(cost, confMatVec.costReturn);
                break;
            case HypenLineBreak:
                cost = Math.min(cost, confMatVec.costReturn) + normedChar.costsHyphenLineBreak;
                break;
            default:
                LOG.warn("unexpected type '{}' of normedChar - take default costs", normedChar.type);
            case Dft:
        }
        return Double.isInfinite(cost) ? null : cost;
    }

    @Override
    public void init(PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> dm, ConfMatVector[] list, NormalizedCharacter[] list1) {
        this.dm = dm;
        this.recos = list;
        this.refs = list1;
        this.sizeReco = recos.length;
        this.sizeRef = refs.length;
    }

}
