package com.compomics.util.gui.utils;

import java.util.HashSet;
import java.util.TreeSet;

/**
 * This class contains empty collections that can be used for empty results without creating new objects.
 *
 * @author Marc Vaudel
 */
public class EmptyCollections {

    /**
     * Empty default constructor
     */
    public EmptyCollections() {
    }
    
    /**
     * Convenience result of an empty set.
     */
    public static final HashSet<String> emptyStringSet = new HashSet<>(0);
    /**
     * Convenience result of an empty set.
     */
    public static final TreeSet<String> emptyStringTreeSet = new TreeSet<>();
    /**
     * Convenience result of an empty set.
     */
    public static final HashSet<Integer> emptyIntSet = new HashSet<>(0);
    /**
     * Convenience result of an empty set.
     */
    public static final TreeSet<Integer> emptyIntTreeSet = new TreeSet<>();

}
