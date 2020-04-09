package com.compomics.util.threading;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This mutex can be used to manage threads editing experiment objects using
 * their key.
 *
 * @author Marc Vaudel
 */
public class ObjectMutex {

    /**
     * The mutex for the mutex map.
     */
    private final SimpleSemaphore masterMutex = new SimpleSemaphore(1);
    /**
     * Map of mutexes for the different objects.
     */
    private final ConcurrentHashMap<Long, SimpleSemaphore> mutexMap = new ConcurrentHashMap<>();

    /**
     * Constructor.
     */
    public ObjectMutex() {

    }

    /**
     * Acquire function for the given key. If a thread gets interrupted an
     * exception is thrown as runtime exception.
     *
     * @param key the object key
     */
    public void acquire(
            long key
    ) {

        SimpleSemaphore objectMutex = mutexMap.get(key);

        if (objectMutex == null) {
            
            masterMutex.acquire();

            objectMutex = mutexMap.get(key);

            if (objectMutex == null) {

                objectMutex = new SimpleSemaphore(1);
                mutexMap.put(key, objectMutex);

            }

            masterMutex.release();

        }

        objectMutex.acquire();

    }

    /**
     * Release function for the given key.
     *
     * @param key the object key
     */
    public void release(long key) {

        SimpleSemaphore objectMutex = mutexMap.get(key);
        objectMutex.release();

    }

}
