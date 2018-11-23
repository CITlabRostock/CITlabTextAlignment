/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.textalignment;


import de.uros.citlab.confmat.CharMap;
import de.uros.citlab.confmat.ConfMat;
import de.uros.citlab.errorrate.types.PathCalculatorGraph;
import de.uros.citlab.errorrate.util.GroupUtil;
import de.uros.citlab.textalignment.costcalculator.*;
import de.uros.citlab.textalignment.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * @author gundram
 */
public class TextAligner {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(TextAligner.class.getName());
    private final PathCalculatorGraph<ConfMatVector, NormalizedCharacter> impl;
    LinkedList<NormalizedCharacter> refs;
    List<ConfMatVector> recos;
    private CharMap charMap;
    private String lbChars;
    //    List<PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter>> bestPath;
//    PathCalculatorGraph.DistanceMat<ConfMatVector, NormalizedCharacter> distMat;
    private Double costSkipWords;
    private Double costSkipConfMat;
    private Double costJumpConfMat;
    private double nacOffset = 0;
    private int maxVertexCount = -1;
    HyphenationProperty hp = null;
    ConfMatCollection cmc = null;
    private double threshold = 0.01;
//    private boolean useHyphens;
//    private Character[] hyphen_suffix;
//    private Character[] hyphen_prefix;

//    public void setMaxPathes(int maxAnz, Mode mode) {
//        impl.setFilter(maxAnz > 0 ? new PathFilterDefault(maxAnz, mode) : null);
//    }

//    public void setMaxPathes(double offset, Mode mode) {
//        impl.setFilter(offset > 0 ? new PathFilterOffsetDefault(offset, mode) : null);
//    }

    public void setHp(HyphenationProperty hp) {
        this.hp = hp;
        init();
    }

    public void setUpdateScheme(PathCalculatorGraph.UpdateScheme updateScheme) {
        impl.setUpdateScheme(updateScheme);
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private void init() {
        impl.resetCostCalculators();
        impl.addCostCalculator(new CostCalculatorCharChar(2));
        impl.addCostCalculator(new CostCalculatorCharNac(1));
        impl.addCostCalculator(new CostCalculatorNacChar(1));
        impl.addCostCalculator(new CostCalculatorContinue());
        if (costJumpConfMat != null) {
            impl.addCostCalculator(new CostCalculatorJumpConfMat(costJumpConfMat));
        }
        if (costSkipWords != null) {
            impl.addCostCalculator(new CostCalculatorSkipWord(costSkipWords));
        }
        if (costSkipConfMat != null) {
            impl.addCostCalculator(new CostCalculatorSkipConfMat(costSkipConfMat, 0.0));
        }
        if (hp != null) {
            int cntSkipHyphens = 2;//for the Return-Character
            boolean addCostCalculatorSkipChar = false;
            if (hp.prefixes != null && hp.prefixes.length > 0) {
                cntSkipHyphens += 2;//for the prefix
                if (hp.skipPrefix) {
                    addCostCalculatorSkipChar = true;
                }
            }
            if (hp.suffixes != null && hp.suffixes.length > 0) {
                cntSkipHyphens += 2;//for the suffix
                if (hp.skipSuffix) {
                    addCostCalculatorSkipChar = true;
                }
            }
            impl.addCostCalculator(new CostCalculatorSkipHyphenation(1 + cntSkipHyphens, 1));
            if (addCostCalculatorSkipChar) {
                //skip one character, which is 1 NaC and 1 Char
                impl.addCostCalculator(new CostCalculatorSkipChar(1 + 2, 1));
            }
        }
    }

    public void setNacOffset(double nacOffset) {
        this.nacOffset = nacOffset;
    }

    public void setMaxVertexCount(int maxVertexCount) {
        this.maxVertexCount = maxVertexCount;
    }

    public TextAligner(String lineBreakCharacters, Double costSkipWords, Double costSkipConfMat, Double costJumpConfMat) {
        this.lbChars = lineBreakCharacters;
        this.impl = new PathCalculatorGraph<>();
        this.costSkipWords = costSkipWords;
        this.costSkipConfMat = costSkipConfMat;
        this.costJumpConfMat = costJumpConfMat;
        init();
//        setHyphenSuffix(new Character[]{'-', '¬', '=', ':'});
//        setHyphenPrefix(new Character[0]);
    }

    private void setReference(List<String> references) {
        if (charMap == null) {
            throw new RuntimeException("apply setRecognition(..) first");
        }
        int indexNaC = charMap.get(CharMap.NaC);
        char returnSymbol = cmc.getReturnSymbol();
        int indexReturn = charMap.get(returnSymbol);
        NormalizedCharacter ncNaC = new NormalizedCharacter(CharMap.NaC, new char[]{CharMap.NaC}, new int[]{indexNaC}, false, NormalizedCharacter.Type.Dft, false);
        NormalizedCharacter ncNaCIsHyphen = new NormalizedCharacter(CharMap.NaC, new char[]{CharMap.NaC}, new int[]{indexNaC}, true, NormalizedCharacter.Type.Dft, false);
        NormalizedCharacter ncLineBreak = new NormalizedCharacter('\n', new char[]{'\n'}, new int[]{indexReturn}, false, NormalizedCharacter.Type.Return, false);
        NormalizedCharacter ncRet = hp != null ? new NormalizedCharacter(returnSymbol, new char[]{returnSymbol}, new int[]{indexReturn}, true, hp.hypCosts, false) : null;
        NormalizedCharacter prefix = hp != null ? new NormalizedCharacter(hp.prefixes != null && hp.prefixes.length > 0 ? hp.prefixes[0] : '¬', hp.prefixes, getIndexes(charMap, hp.prefixes), true, NormalizedCharacter.Type.Dft, hp.skipPrefix) : null;
        NormalizedCharacter suffix = hp != null ? new NormalizedCharacter(hp.suffixes != null && hp.suffixes.length > 0 ? hp.suffixes[0] : '¬', hp.suffixes, getIndexes(charMap, hp.suffixes), true, NormalizedCharacter.Type.Dft, hp.skipSuffix) : null;
        refs = new LinkedList<>();
        refs.add(ncLineBreak);//first sign in ConfMatCollection will be a return - so it will match the linebreak
        for (String line : references) {
            if (!line.trim().equals(line)) {
                LOG.warn("line '{}' has spaces at the beginning or end. The line will be trimmed.", line);
                line = line.trim();
            }
            List<String> hypenParts = hp == null ? Arrays.asList(line) : Hyphenator.getInstance(hp.pattern).hyphenate(line);
            for (int k = 0; k < hypenParts.size(); k++) {
                char[] ref = hypenParts.get(k).toCharArray();
                for (int i = 0; i < ref.length; i++) {
                    char c = ref[i];
                    refs.add(ncNaC);
                    refs.add(new NormalizedCharacter(c,
                            charMap.get(c) == null ? null : new char[]{c},
                            getIndexes(charMap, new char[]{c}),
                            false,
                            lbChars.indexOf(c) >= 0 ? NormalizedCharacter.Type.SpaceLineBreak : NormalizedCharacter.Type.Dft,
                            false));
                }
                //add optional hyphenation
                if (k < hypenParts.size() - 1) {
                    if (hp.suffixes != null && hp.suffixes.length > 0) {
                        refs.add(ncNaCIsHyphen);
                        refs.add(suffix);
                    }
                    refs.add(ncNaCIsHyphen);
                    refs.add(ncRet);
                    if (hp.prefixes != null && hp.prefixes.length > 0) {
                        refs.add(ncNaCIsHyphen);
                        refs.add(prefix);
                    }
                }
            }
            refs.add(ncNaC);
            refs.add(ncLineBreak);//last sign in ConfMatCollection will be a return - so it will match the linebreak
        }
//        for (int i = 2; i < refs.size(); i++) {
//            NormalizedCharacter first = refs.get(i - 2);
//            NormalizedCharacter second = refs.get(i - 1);
//            NormalizedCharacter third = refs.get(i);
////            if (first.isNaC == second.isNaC) {
////                throw new RuntimeException("two nacs after each other");
////            }
////            if (first.isNaC != third.isNaC) {
////                throw new RuntimeException("two different things 2 position away");
////            }
//
//        }
    }

    private static int[] getIndexes(CharMap cm, char[] chars) {
        if (chars == null) {
            return null;
        }
        List<Integer> res = new LinkedList<>();
        for (char aChar : chars) {
            Integer key = cm.get(aChar);
            if (key != null) {
                res.add(key);
            }
        }
        if (res.isEmpty()) {
            return null;
//            throw new RuntimeException("cannot find any channel for chars '" + String.copyValueOf(chars) + "'");
        }
        int[] res1 = new int[res.size()];
        for (int i = 0; i < res.size(); i++) {
            res1[i] = res.get(i);
        }
        return res1;
    }

    public void setDebugOutput(int size, File file) {
        impl.setSizeProcessViewer(size);
        impl.setFileDynMat(file);
    }

    public List<LineMatch> getAlignmentResult(List<String> refs, List<ConfMat> recos) {
        setRecognition(recos);
        setReference(refs);
        List<PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter>> bestPath = impl.calcBestPath(this.recos, this.refs);

        if (bestPath == null) {
            return null;
        }
        if (LOG.isDebugEnabled()) {
            for (PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> distance : bestPath) {
                LOG.debug(distance.toString());
            }
        }
        List<BestPathPart> grouping = GroupUtil.getGrouping(bestPath,
                new GroupUtil.Joiner<PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter>>() {

                    @Override
                    public boolean isGroup(
                            List<PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter>> list,
                            PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> element) {
                        return keepElement(element);
                    }

                    @Override
                    public boolean keepElement(PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter> element) {
                        if (element.getManipulation().startsWith("SKIP_")) {
                            return false;
                        }
                        ConfMatVector[] recos = element.getRecos();
                        if (recos == null || recos.length == 0) {
                            return false;
                        }
                        int returnIndex = charMap.get(CharMap.Return);
                        for (ConfMatVector cmVec : recos) {
                            if (cmVec.maxIndex == returnIndex) {
                                if (recos.length != 1) {
                                    throw new RuntimeException("unexpected length");
                                }
                                return false;
                            }
                        }
                        return true;
                    }
                },
                new GroupUtil.Mapper<PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter>, BestPathPart>() {
                    @Override
                    public BestPathPart map(List<PathCalculatorGraph.IDistance<ConfMatVector, NormalizedCharacter>> elements) {
                        BestPathPart bestPathPart = BestPathPart.newInstance(elements);
                        if (bestPathPart.reference.indexOf(cmc.getReturnSymbol()) >= 0) {
                            LOG.warn("in reference '{}' is still the character '{}' - delete this character.", bestPathPart.reference, cmc.getReturnSymbol());
                            bestPathPart.reference = bestPathPart.reference.replace("" + cmc.getReturnSymbol(), "");
                        }
                        return bestPathPart;
                    }
                });
        HashMap<ConfMat, LineMatch> refMap = new LinkedHashMap<>();
        for (BestPathPart cmp : grouping) {
            ConfMat cmOld = cmc.getOrigConfMat(cmp.startReco);
            double conf = cmp.getConf();
            if (conf > threshold) {
                //found match!
                if (refMap.containsKey(cmOld) && refMap.get(cmOld).getConfidence() < conf) {
                    continue;
                }
                refMap.put(cmOld, new LineMatch(cmOld, cmp.reference, conf));
            }
        }
        List<LineMatch> res = new LinkedList<>();
        for (int i = 0; i < recos.size(); i++) {
            res.add(refMap.get(recos.get(i)));
        }
        return res;
    }

    private void setRecognition(List<ConfMat> confMats) {
        cmc = ConfMatCollection.newInstance(
                1.0,
                confMats,
                nacOffset);

        charMap = cmc.getCharMap();
//        double[][] recoVecs = cmc.getLLHMat().copyTo(null);
        double[][] recoVecs = cmc.getMatrix();
        recos = new ArrayList<>(recoVecs.length);
        for (int i = 0; i < recoVecs.length; i++) {
            double[] recoVec = recoVecs[i];
            recos.add(new ConfMatVector(recoVec, charMap, lbChars));
        }
    }

    private static boolean isSkipPointSoft(NormalizedCharacter nc) {
        return nc.type != NormalizedCharacter.Type.Dft;
    }

    private static boolean isSkipPointHard(NormalizedCharacter nc) {
        return isSkipPointSoft(nc) && nc.type != NormalizedCharacter.Type.HypenLineBreak;
    }

}