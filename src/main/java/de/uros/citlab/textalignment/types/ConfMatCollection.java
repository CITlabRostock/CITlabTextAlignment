////////////////////////////////////////////////
/// File:       ConfMatCollection.java
/// Created:    22.06.2015  22:27:26 
/// Encoding:   UTF-8 
//////////////////////////////////////////////// 
package de.uros.citlab.textalignment.types;


import de.uros.citlab.confmat.CharMap;
import de.uros.citlab.confmat.ConfMat;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Concatenate a list of ConfMats into one long ConfMat with a new channel for
 * newlines.
 * <p>
 * Since 22.06.2015
 *
 * @author tobias
 */
public class ConfMatCollection extends ConfMat {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(ConfMatCollection.class);
    private final CharMap charMapOrig;
    private double confNac;
    private double confRet;
    private final double retProb;
    private int[] indicesStart;
    private final int numOfCM;
    private final char returnSymb;
    private final double offsetNaC;
    private List<ConfMat> confMats;

//    public ConfMatCollection(double retProb, List<ConfMat> cmList, boolean appendFinalRet) {
//        this(retProb, cmList, appendFinalRet, 0.0);
//    }
//
//    public ConfMatCollection(double retProb, List<ConfMat> cmList, boolean appendFinalRet, double offsetNaC) {
//        this(CharMap.Return, retProb, cmList, appendFinalRet, offsetNaC);
//    }

    private ConfMatCollection(CharMap charMap, double[][] matrix, CharMap charMapOrig, int[] indicesStart, List<ConfMat> confMats, double retProb, int numOfCM, char returnSymb, double offsetNaC) {
        super(charMap, matrix);
        this.charMapOrig = charMapOrig;
        this.retProb = retProb;
        this.numOfCM = numOfCM;
        this.returnSymb = returnSymb;
        this.offsetNaC = offsetNaC;
        this.confMats = confMats;
        this.indicesStart = indicesStart;
    }

    public static ConfMatCollection newInstance(char retChar, double retProb, List<ConfMat> cmList, double offsetNaC) {
        if (cmList.isEmpty()) {
            throw new RuntimeException("empty ConfMat list");
        }
        int cmPos = 0;
        CharMap charMapOrig = cmList.get(0).getCharMap();
        if (charMapOrig.containsChar(retChar)) {
            throw new IllegalArgumentException("symbol '" + retChar + "' already in charMap - please choose other retChar.");
        }
        for (ConfMat cm : cmList) {
            if (cm.getLength() == 0) {
                LOG.error("empty ConfMat");
            }
            cmPos += cm.getLength();
            if (!charMapOrig.equals(cm.getCharMap())) {
                throw new IllegalArgumentException("ConfMats have to have the same charmaps!");
            }
        }

        int numOfCM = cmList.size();
        double[][] bigMat = new double[cmPos + cmList.size() + 1][charMapOrig.size() + 1];
        int posIdx = 0;
        int[] indicesStart = new int[cmList.size()];
        double confNac = Math.log(1 - retProb);
        double confRet = Math.log(retProb);
        int cmIdx = 0;
        {
            double[] bigVec = bigMat[posIdx++];
            Arrays.fill(bigVec, Double.NEGATIVE_INFINITY);
            bigVec[0] = confNac;
            bigVec[bigVec.length - 1] = confRet;
        }
        for (ConfMat cm : cmList) {
            double[][] mat = cm.getMatrix();
            indicesStart[cmIdx++] = posIdx;
            for (int j = 0; j < mat.length; j++) {
                double[] row = mat[j];
                double[] bigVec = bigMat[posIdx++];
                System.arraycopy(row, 0, bigVec, 0, row.length);
                bigVec[0] += offsetNaC;
                bigVec[row.length] = Double.NEGATIVE_INFINITY;
            }
            if (posIdx == bigMat.length) {
                break;
            }
            double[] bigVec = bigMat[posIdx++];
            Arrays.fill(bigVec, Double.NEGATIVE_INFINITY);
            bigVec[0] = confNac;
            bigVec[bigVec.length - 1] = confRet;
        }
        CharMap charMap = new CharMap(charMapOrig);
        charMap.add(CharMap.Return);
        return new ConfMatCollection(charMap, bigMat, charMapOrig, indicesStart, cmList, retProb, numOfCM, retChar, offsetNaC);
    }

    public int getOrigConfMatIndex(int pos) {
        if (pos <= 0 || pos >= getMatrix().length) {
            throw new ArrayIndexOutOfBoundsException("requested position is " + pos + " but length of MultiConfMat is " + getMatrix().length + ".");
        }
        for (int i = indicesStart.length - 1; i >= 0; i--) {
            if (indicesStart[i] <= pos) {
                return i;
            }
        }
        throw new RuntimeException("position " + pos + " should be in one of the starting-points " + Arrays.toString(indicesStart));
    }

    public ConfMat getOrigConfMat(int pos) {
        return confMats.get(getOrigConfMatIndex(pos));
    }

//    /**
//     * @param idx
//     * @return index of ConfMat (first) and index in ConfMat (second)
//     */
//    public Pair<Integer, Integer> getOrigIdx(int idx) {
//        if (idx < 0 || idx > getLength()) {
//            return null;
//        }
//        int start = 0;
//        while (start + 1 < numOfCM && idx >= indicesStart[start + 1]) {
//            start++;
//        }
//        return start < indicesStart.length ? new Pair<>(start, idx - indicesStart[start]) : null;
//    }

}
