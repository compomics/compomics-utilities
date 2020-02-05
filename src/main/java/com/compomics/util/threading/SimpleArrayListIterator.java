package com.compomics.util.threading;

import java.util.ArrayList;

/**
 * Simple synchronized ArrayList iterator.
 *
 * @param <K> the class of objects in the array
 *
 * @author Marc Vaudel
 */
public class SimpleArrayListIterator<K> {

    /**
     * The array to iterate.
     */
    private final ArrayList<K> array;
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
    public SimpleArrayListIterator(
            ArrayList<K> array
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

        if (i >= array.size()) {

            mutex.release();

            return null;

        }

        K result = array.get(i);

        i++;

        mutex.release();

        return result;

    }

}
