/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uros.citlab.textalignment;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author gundram
 */
public class HyphenationProperty {

    public boolean skipSuffix = false;
    public boolean skipPrefix = false;
    public char[] prefixes = null;
    public char[] suffixes = null;
    public double hypCosts = 0.0;
    public Hyphenator.HyphenationPattern pattern = null;
    public static final Logger LOG = LoggerFactory.getLogger(HyphenationProperty.class);

    public static HyphenationProperty newInstance(String hyphenProperty, String hyphenLangProperty) {
        if (hyphenProperty == null) {
            return null;
        }
        try {
            double value = Double.valueOf(hyphenProperty);
            if (Double.isInfinite(value)) {
                return null;
            }
            return new HyphenationProperty(value, hyphenLangProperty == null ? null : Hyphenator.HyphenationPattern.valueOf(hyphenLangProperty.toUpperCase()));
        } catch (NumberFormatException ex) {
            try {
                if (hyphenLangProperty != null) {
                    throw new RuntimeException("property with value '" + hyphenLangProperty + "' is set but json-string '" + hyphenProperty + "' is set - double set of Language.");
                }
                return new Gson().fromJson(hyphenProperty, HyphenationProperty.class);
            } catch (Throwable e) {
                LOG.error("cannot parse property '" + hyphenProperty + "':caught exception 1/2:", ex);
                LOG.error("cannot parse property '" + hyphenProperty + "':caught exception 2/2:", e);
                throw new RuntimeException("cannot parse '" + hyphenProperty + "' to double value or Json-structure.", ex);
            }
        }

    }

    public HyphenationProperty(double hypenCosts, Hyphenator.HyphenationPattern pattern) {
        this();
        this.hypCosts = hypenCosts;
        this.pattern = pattern;
        this.suffixes = new char[]{'¬', '-', ':', '='};
    }

    public HyphenationProperty() {
    }

    public HyphenationProperty(boolean skipSuffix, boolean skipPrefix, char[] prefixes, char[] suffixes, double hypCosts) {
        this(skipSuffix, skipPrefix, prefixes, suffixes, hypCosts, null);
    }

    public HyphenationProperty(boolean skipSuffix, boolean skipPrefix, char[] prefixes, char[] suffixes, double hypCosts, Hyphenator.HyphenationPattern pattern) {
        this(hypCosts, pattern);
        this.skipSuffix = skipSuffix;
        this.skipPrefix = skipPrefix;
        this.prefixes = prefixes;
        this.suffixes = suffixes;
    }
}
