package com.compomics.util.experiment.filtering;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Comparators for filter items.
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
     * The symbol to use.
     */
    public final String name;
    /**
     * The description to use.
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
    public boolean passes(String threshold, double value) {
        switch (this) {
            case equal:
                return Double.parseDouble(threshold) == value;
            
            case higher:
                return value > Double.parseDouble(threshold);
                
            case lower:
                return value < Double.parseDouble(threshold);
                
            case higherOrEqual:
                return value >= Double.parseDouble(threshold);
                
            case lowerOrEqual:
                return value <= Double.parseDouble(threshold);
            
            case contains:
                return Double.toString(value).contains(threshold);
            
            case excludes:
                return !Double.toString(value).contains(threshold);
            
            case matches:
                return Double.toString(value).matches(threshold);
            
            default:
                throw new IllegalArgumentException("Filter comparator not implemented for item " + this.name + ".");
        }
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
                double thresholdDouble = Double.parseDouble(threshold);
                double valueDouble = Double.parseDouble(value);
                return valueDouble > thresholdDouble;
                
            case lower:
                thresholdDouble = Double.parseDouble(threshold);
                valueDouble = Double.parseDouble(value);
                return valueDouble < thresholdDouble;
                
            case higherOrEqual:
                thresholdDouble = Double.parseDouble(threshold);
                valueDouble = Double.parseDouble(value);
                return valueDouble >= thresholdDouble;
                
            case lowerOrEqual:
                thresholdDouble = Double.parseDouble(threshold);
                valueDouble = Double.parseDouble(value);
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
    public boolean passes(String threshold, Collection<String> values) {
        
        return passes(threshold, values.stream());
        
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
        
        return passes(threshold, Arrays.stream(values));
        
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
    public boolean passes(String threshold, Stream<String> values) {
        
        switch (this) {
            case higher:
            case equal:
            case lower:
            case higherOrEqual:
            case lowerOrEqual:
            case contains:
            case matches:
                values.anyMatch(value -> passes(threshold, value));
                
            case excludes:
                values.allMatch(value -> passes(threshold, value));
                
            default:
                throw new IllegalArgumentException("Filter comparator not implemented for item " + this.name + ".");
        }
    }
    
    @Override
    public String toString() {
        return name;
    }

    /**
     * Empty default constructor
     */
    private FilterItemComparator() {
        name = "";
        description = "";
    }
}
