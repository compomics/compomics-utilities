package com.compomics.util.experiment.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Comparators for filter items
 *
 * @author Marc Vaudel
 */
public enum FilterItemComparator {

    equal("=", "Equals"),
    higher(">", "Higher"),
    lower("<", "Lower"),
    higherOrEqual(">=", "Higher or Equal"),
    lowerOrEqual("<=", "Lower or Equal"),
    contains("contains", "Contains"),
    excludes("excludes", "Does not contain"),
    matches("matches", "Matches the given regular expression");
    /**
     * The symbol to use
     */
    public final String name;
    /**
     * The description to use
     */
    public final String description;
    /**
     * Array of possibilities used for a boolean value.
     */
    public static final String[] trueFalse = {"Yes", "No"};

    /**
     * Constructor.
     *
     * @param symbol symbol to use
     * @param description description to use
     */
    private FilterItemComparator(String symbol, String description) {
        this.name = symbol;
        this.description = description;
    }

    /**
     * Indicates whether a given value passes a threshold using this comparator.
     *
     * @param threshold the threshold as string
     * @param value the value as string
     *
     * @return a boolean indicating whether a given value passes a threshold
     * using this comparator
     */
    public boolean passes(String threshold, String value) {
        switch (this) {
            case equal:
                return threshold.equals(value);
            case higher:
                Double thresholdDouble = new Double(threshold);
                Double valueDouble = new Double(value);
                return valueDouble > thresholdDouble;
            case lower:
                thresholdDouble = new Double(threshold);
                valueDouble = new Double(value);
                return valueDouble < thresholdDouble;
            case higherOrEqual:
                thresholdDouble = new Double(threshold);
                valueDouble = new Double(value);
                return valueDouble >= thresholdDouble;
            case lowerOrEqual:
                thresholdDouble = new Double(threshold);
                valueDouble = new Double(value);
                return valueDouble <= thresholdDouble;
            case contains:
                return value.contains(threshold);
            case excludes:
                return !value.contains(threshold);
            case matches:
                return value.matches(threshold);
            default:
                throw new IllegalArgumentException("Filter comparator not implemented for item " + this.name + ".");
        }
    }

    /**
     * Indicates whether a set of values passes a threshold using this
     * comparator.
     *
     * @param threshold the threshold as string
     * @param values list of values
     *
     * @return a boolean indicating whether a given value passes a threshold
     * using this comparator
     */
    public boolean passes(String threshold, List<String> values) {
        switch (this) {
            case equal:
                for (String value : values) {
                    if (threshold.equals(value)) {
                        return true;
                    }
                }
                return false;
            case higher:
                Double thresholdDouble = new Double(threshold);
                for (String value : values) {
                    Double valueDouble = new Double(value);
                    if (valueDouble > thresholdDouble) {
                        return true;
                    }
                }
                return false;
            case lower:
                thresholdDouble = new Double(threshold);
                for (String value : values) {
                    Double valueDouble = new Double(value);
                    if (valueDouble < thresholdDouble) {
                        return true;
                    }
                }
                return false;
            case higherOrEqual:
                thresholdDouble = new Double(threshold);
                for (String value : values) {
                    Double valueDouble = new Double(value);
                    if (valueDouble >= thresholdDouble) {
                        return true;
                    }
                }
                return false;
            case lowerOrEqual:
                thresholdDouble = new Double(threshold);
                for (String value : values) {
                    Double valueDouble = new Double(value);
                    if (valueDouble <= thresholdDouble) {
                        return true;
                    }
                }
                return false;
            case contains:
                for (String value : values) {
                    if (value.contains(threshold)) {
                        return true;
                    }
                }
                return false;
            case excludes:
                for (String value : values) {
                    if (value.contains(threshold)) {
                        return false;
                    }
                }
                return true;
            case matches:
                for (String value : values) {
                    if (value.matches(threshold)) {
                        return true;
                    }
                }
                return false;
            default:
                throw new IllegalArgumentException("Filter comparator not implemented for item " + this.name + ".");
        }
    }

    /**
     * Indicates whether a set of values passes a threshold using this
     * comparator.
     *
     * @param threshold the threshold as string
     * @param values list of values
     *
     * @return a boolean indicating whether a given value passes a threshold
     * using this comparator
     */
    public boolean passes(String threshold, String[] values) {
        return passes(threshold, Arrays.asList(values));
    }

}
