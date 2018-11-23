package de.uros.citlab.textalignment.types;

import de.uros.citlab.confmat.ConfMat;

public class LineMatch {

    private ConfMat cm;
    private String reference;
    private double confidence;

    public LineMatch(ConfMat cm, String reference, double confidence) {
        this.cm = cm;
        this.reference = reference;
        this.confidence = confidence;
    }

    public ConfMat getCm() {
        return cm;
    }

    public String getReference() {
        return reference;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "LineMatch{" +
                "cm=" + cm +
                ", reference='" + reference + '\'' +
                ", confidence=" + confidence +
                '}';
    }
}

