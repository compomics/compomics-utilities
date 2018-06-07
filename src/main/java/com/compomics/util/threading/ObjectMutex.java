package com.compomics.util.threading;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * This mutex can be used to manage threads editing experiment objects using
 * their key.
 *
 * @author Marc Vaudel
 */
public class ObjectMutex {

    /**
     * Master mutex.
     */
    private final Semaphore masterMutex = new Semaphore(1);

    /**
     * Map of mutexes for the different objects.
     */
    private final HashMap<Long, Semaphore> mutexMap = new HashMap<>();

    /**
     * Constructor.
     */
    public ObjectMutex() {

    }

    /**
     * Acquire function for the given key. If a thread gets interrupted an exception is thrown as runtime exception.
     * 
     * @param key the object key
     */
    public void acquire(long key) {
        
        try {

        Semaphore objectMutex = mutexMap.get(key);

        if (objectMutex == null) {

            masterMutex.acquire();

            objectMutex = mutexMap.get(key);

            if (objectMutex == null) {
                
                objectMutex = new Semaphore(1);
                mutexMap.put(key, masterMutex);

            }
            
            masterMutex.release();
            
        }
        
        objectMutex.acquire();
        
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } 
        
    }
    
    /**
     * Release function for the given key.
     * 
     * @param key the object key
     */
    public void release(long key) {
        
        Semaphore objectMutex = mutexMap.get(key);
        objectMutex.release();

    }

}
