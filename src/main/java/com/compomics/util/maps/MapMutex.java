package com.compomics.util.maps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Semaphore;

/**
 * Map of semaphores acting like a map mutex to block some parts of the maps.
 *
 * @author Marc Vaudel
 *
 * @param <K> the type of keys to use
 */
public class MapMutex<K> {

    /**
     * The maximal number of keys to keep in the map.
     */
    private Integer cacheLimitSize = null;

    /**
     * The number of permits per key.
     */
    public final int permits;

    /**
     * Map of semaphores used to control the threads.
     */
    private final HashMap<K, Semaphore> semaphoreMap;

    /**
     * Mutex for teh edition of the semaphore map.
     */
    private Semaphore mutex = new Semaphore(1, true);

    /**
     * Boolean indicating whether the map is being edited.
     */
    private boolean writing = false;
    
    /**
     * Constructor.
     * 
     * @param permits the number of permits per key, 1 if null
     * @param cacheLimitSize the size limit where semaphores will be removed upon release of all permits, ignored if null
     * @param initialSize the initial size of the map, the default HashMap size if null
     */
    public MapMutex(Integer permits, Integer cacheLimitSize, Integer initialSize) {
        if (permits != null) {
        this.permits = permits;
        }else {
            this.permits = 1;
        }
        if (cacheLimitSize != null) {
        this.cacheLimitSize = cacheLimitSize;
        }
        if (initialSize != null) {
        this.semaphoreMap = new HashMap<K, Semaphore>(initialSize);
        } else {
            this.semaphoreMap = new HashMap<K, Semaphore>();
        }
    }
    
    /**
     * Constructor with one permit per key and not cache limit size.
     */
    public MapMutex() {
        this(null, null, null);
    }
    
    /**
     * Constructor with no cache limit size.
     * 
     * @param permits the number of permits per key
     */
    public MapMutex(int permits) {
        this(permits, null, null);
    }

    /**
     * Acquires a permit for the given key.
     *
     * @param key the key
     *
     * @throws InterruptedException exception thrown if the thread is
     * interrupted
     */
    public void aquire(K key) throws InterruptedException {
        // Block if the map is being edited
        if (writing) {
            mutex.acquire();
            mutex.release();
        }
        // Get semaphore from map
        Semaphore semaphore = semaphoreMap.get(key);
        // Add semaphore to map if none is present and acquire
        if (semaphore == null) {
            mutex.acquire();
            if (semaphore == null) {
                semaphore = new Semaphore(permits);
                semaphoreMap.put(key, semaphore);
            }
            semaphore.acquire();
            mutex.release();
        } else {
            semaphore.acquire();
        }
    }

    /**
     * Releases the permit for the given key. If the size of the map exceeds the
     * cache size semaphores with all available permits will be removed from the
     * map. Despite my best efforts no guarantee that it does not overwrite an
     * acquire.
     *
     * @param key the key to release
     *
     * @throws InterruptedException exception thrown if the thread is
     * interrupted
     */
    public void release(K key) throws InterruptedException {
        // Block if the map is being edited
        if (writing) {
            mutex.acquire();
            mutex.release();
        }
        // Edit map if over max size
        if (cacheLimitSize != null && semaphoreMap.size() > cacheLimitSize) {
            writing = true;
            mutex.acquire();
            writing = true;
            HashSet<K> keys = new HashSet<K>(semaphoreMap.keySet());
            for (K tempKey : keys) {
                Semaphore tempSemaphore = semaphoreMap.get(tempKey);
                if (!tempSemaphore.hasQueuedThreads() && tempSemaphore.availablePermits() == permits) {
                    semaphoreMap.remove(tempKey);
                }
            }
            writing = false;
            mutex.release();
        }
        // Get semaphore from map
        Semaphore semaphore = semaphoreMap.get(key);
        // Release
        semaphore.release();
    }

}
