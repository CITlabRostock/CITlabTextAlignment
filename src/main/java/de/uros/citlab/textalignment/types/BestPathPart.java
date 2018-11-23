/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.textalignment.types;


import de.uros.citlab.confmat.CharMap;
import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author gundram
 */
public class BestPathPart {

    public double[][] orig;
    public int startReco;
    public int endReco;
    public int startRef;
    public int endRef;
    public double costs;
    public double costStart;
    public double costEnd;
    public String reference;
    public static final Logger LOG = LoggerFactory.getLogger(BestPathPart.class);

    public double getConf() {
        return Math.exp(-getCost());
    }

    public double getCost() {
        return Math.max(Math.max(costs, costStart), costEnd);
    }

    private static String collapse(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        char[] chars = str.toCharArray();
        if (chars[0] != CharMap.NaC) {
            sb.append(chars[0]);
        }
        char last = chars[0];
        for (int i = 1; i < chars.length; i++) {
            if (last != chars[i]) {
                last = chars[i];
                if (last != CharMap.NaC) {
                    sb.append(last);
                }
            }
        }
        return sb.toString();
    }

    public static BestPathPart newInstance(List<PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter>> elements) {
        final int borderSize = 3;
        double[][] confMatPart = new double[elements.size()][];
        StringBuilder sb = new StringBuilder();
        StringBuilder prefix = new StringBuilder();
        StringBuilder suffix = new StringBuilder();
        int lenPrefix = 0, lenSuffix = 0;
        for (; lenPrefix < elements.size(); lenPrefix++) {
            PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> aElement = elements.get(lenPrefix);
            NormalizedCharacter[] aRef = aElement.getReferences();
            if (aRef.length > 1) {
                throw new RuntimeException("does not expect reference-length > 1");
            }
            for (NormalizedCharacter normalizedCharacter : aRef) {
                prefix.append(normalizedCharacter.orig);
            }
            if (collapse(prefix.toString()).length() >= borderSize) {
                break;
            }
        }
        for (; lenSuffix < elements.size(); lenSuffix++) {
            PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> aElement = elements.get(elements.size() - 1 - lenSuffix);
            NormalizedCharacter[] aRef = aElement.getReferences();
            if (aRef.length > 1) {
                throw new RuntimeException("does not expect reference-length > 1");
            }
            for (NormalizedCharacter normalizedCharacter : aRef) {
                suffix.append(normalizedCharacter.orig);
            }
            if (collapse(suffix.toString()).length() >= borderSize) {
                break;
            }
        }
        double[] costIntegral = new double[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> element = elements.get(i);
            for (NormalizedCharacter reference : element.getReferences()) {
                if (CharMap.Return != reference.orig) {
                    sb.append(reference.orig);
                } else {
                    LOG.warn("in reference is still the character '{}' - delete this character.", CharMap.Return);
                }
            }
            ConfMatVector[] recos = element.getRecos();
            if (recos.length != 1) {
                throw new RuntimeException("expect only entries with length 1 but is " + recos.length);
            }
            confMatPart[i] = recos[0].vecOrig;
            costIntegral[i] = recos[0].costOffset + element.getCosts();
            if (i > 0) {
                costIntegral[i] += costIntegral[i - 1];
            }
        }
        //use "*Off" factors to correct previous shift in ConfMat-Costs
        PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> start = elements.get(0);
//        double startOff = 0;
        PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> end = elements.get(elements.size() - 1);
//        double endOff = offsetsSum[elements.size() - 1];
//        PathCalculatorGraph.IDistance<TextAligner.ConfMatVector, TextAligner.NormalizedCharacter> postStart = elements.get(Math.max(0, elements.size() - 1 - lenSuffix));
//        double postStartOff = offsetsSum[Math.max(0, elements.size() - 1 - lenSuffix)];
//        PathCalculatorGraph.IDistance<TextAligner.ConfMatVector, TextAligner.NormalizedCharacter> preEnd = elements.get(Math.min(lenPrefix, elements.size() - 1));
//        double preEndOff = offsetsSum[Math.min(lenPrefix, elements.size() - 1)];
        String reference = collapse(sb.toString());
        if (reference.trim().length() != reference.length()) {
            if (reference.indexOf(reference.trim()) > 0) {
                LOG.warn("first sign '{}' in reference '{}' with integer value {} is in reference - trim reference.", reference.charAt(0), reference, (int) reference.charAt(0));
            } else {
                LOG.warn("last sign '{}' in reference '{}' with integer value {} is in reference - trim reference.", reference.charAt(reference.length() - 1), reference, (int) reference.charAt(0));
            }
            reference = reference.trim();
        }
        return new BestPathPart(confMatPart,
                start.getPoint()[0] - 1,//correction factor in comparation to confmat
                end.getPoint()[0] - 1,
                start.getPoint()[1] - 1,
                end.getPoint()[1] - 1,
                (costIntegral[elements.size() - 1] - 0) / reference.length(),//costs of fist position also have to be taken
                (costIntegral[lenPrefix == elements.size() ? elements.size() - 1 : lenPrefix] - 0) / Math.min(reference.length(), borderSize),//costs of fist position also have to be taken
                (costIntegral[elements.size() - 1] - (lenSuffix == elements.size() ? 0 : costIntegral[elements.size() - 1 - lenSuffix])) / Math.min(reference.length(), borderSize),//costs of fist position also have to be taken
                reference);
    }

    private BestPathPart(double[][] orig, int startReco, int endReco, int startRef, int endRef, double costs, double costStart, double costEnd, String reference) {
        this.orig = orig;
        this.startReco = startReco;
        this.endReco = endReco;
        this.startRef = startRef;
        this.endRef = endRef;
        this.costs = costs;
        this.costStart = costStart;
        this.costEnd = costEnd;
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "ConfMatPart{" + "start=" + startReco + ", end=" + endReco + ", costs=" + costs + ", reference=" + reference + '}';
    }

}
