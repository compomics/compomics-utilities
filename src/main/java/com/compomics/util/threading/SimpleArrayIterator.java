package com.compomics.util.threading;

/**
 * Simple synchronized array iterator.
 *
 * @param <K> the class of objects in the array
 * 
 * @author Marc Vaudel
 */
public class SimpleArrayIterator<K> {
    
    /**
     * The array to iterate.
     */
    private final K[] array;
    /**
     * Mutex for synchronization.
     */
    private final SimpleSemaphore mutex = new SimpleSemaphore(1);
    /**
     * The current index.
     */
    private int i = 0;
    
    /**
     * Constructor.
     * 
     * @param array The array to iterate.
     */
    public SimpleArrayIterator(
            K[] array
    ) {
        
        this.array = array;
        
    }
    
    /**
     * Returns the next object. Null if the end of the array was reached.
     * 
     * @return The next object.
     */
    public K next() {
        
        mutex.acquire();
        
        if (i >= array.length) {
            
            return null;
            
        }
        
        K result = array[i];
        
        i++;
        
        mutex.release();
        
        return result;
        
    }
    
}
