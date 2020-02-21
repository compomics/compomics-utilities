package com.compomics.util;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Convenience methods to work with lambdas.
 *
 * @author Marc Vaudel
 */
public class StreamUtil {
    
    /**
     * Returns a concatenation of the streams of the given collections. If a collection is null it is ignored. Returns null if all collections are null.
     * 
     * @param collections The collections.
     * 
     * @return The concatenated stream.
     */
    public static <T> Stream<T>  concatenate(Collection<T>... collections) {
        
        Stream<T> result = null;
        
        for (Collection<T> collection : collections) {
            
            if (collection != null) {
                
                result = result == null ? collection.stream() : Stream.concat(result, collection.stream());
                
            }
        }
        
        return result;
        
    }

}
