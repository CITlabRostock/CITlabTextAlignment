package de.uros.citlab.textalignment.types;

import de.uros.citlab.confmat.CharMap;

public class NormalizedCharacter {

    public final char orig;
    public final char[] normalized;
    public final int[] index;
    public final boolean isHyphen;
    public final boolean isNaC;
    public final boolean isSkipable;
    public final Type type;
    public final boolean isAnyChar;
    public final double costsHyphenLineBreak;

    @Override
    public String toString() {
        return ("NormalizedCharacter{" + "orig=" + orig + ", hyp=" + isHyphen + ", skip=" + isSkipable + ", type=" + type + ", anyChar=" + isAnyChar + '}').replace(CharMap.NaC, '*').replace("\n", "\\n");
    }

    public enum Type {
        Dft,
        SpaceLineBreak,
        Return,
        HypenLineBreak
    }

    public NormalizedCharacter(char orig, char[] normalized, int[] indexes, boolean isHyphen, double costsHypenLineBreak, boolean isSkipable) {
        this(orig, normalized, indexes, isHyphen, Type.HypenLineBreak, costsHypenLineBreak, isSkipable);
    }

    private NormalizedCharacter(char orig, char[] normalized, int[] indexes, boolean isHyphen, Type type, double costsHypenLineBreak, boolean isSkipable) {
        this.orig = orig;
        this.normalized = normalized;
        isAnyChar = (indexes == null || indexes.length == 0);
        this.isHyphen = isHyphen;
        this.isSkipable = isSkipable;
        this.type = type;
//            if (this.isLineBreak != null && this.isLineBreak == true) {
//                LOG.log(Logger.INFO, "stop");
//            }
        this.index = indexes;
        isNaC = orig == CharMap.NaC;
        this.costsHyphenLineBreak = costsHypenLineBreak;

    }

    public NormalizedCharacter(char orig, char[] normalized, int[] indexes, boolean isHyphen, Type type, boolean isSkipable) {
        this(orig, normalized, indexes, isHyphen, type, 0.0, isSkipable);
    }

}
