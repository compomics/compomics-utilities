package com.compomics.util.threading;

import java.util.concurrent.Semaphore;

/**
 * A simple semaphore where thread interrupted exception are thrown as runtime exception.
 *
 * @author Marc Vaudel
 */
public class SimpleSemaphore {
    
    /**
     * The semaphore to use.
     */
    private final Semaphore mutex;
    
    /**
     * Constructor.
     * 
     * @param nPermits the number of permits
     * @param fair boolean indicating whether threads should be processed in a fair way
     */
    public SimpleSemaphore(int nPermits, boolean fair) {
        
        this.mutex = new Semaphore(nPermits, fair);
        
    }
    
    /**
     * Constructor.
     * 
     * @param nPermits the number of permits
     */
    public SimpleSemaphore(int nPermits) {
        
        this.mutex = new Semaphore(nPermits);
        
    }
    
    /**
     * Acquires.
     */
    public void acquire() {
        
        try {
            
            mutex.acquire();
            
        } catch (Exception e) {
            
            throw new RuntimeException(e);
            
        }
    }
    
    /**
     * Releases.
     */
    public void release() {
        
        try {
            
            mutex.release();
            
        } catch (Exception e) {
            
            throw new RuntimeException(e);
            
        }
    }

}
