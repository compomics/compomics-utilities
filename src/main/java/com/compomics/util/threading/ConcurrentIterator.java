package com.compomics.util.threading;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Iterator based on a ConcurrentLinkedQueue.
 *
 * @param <K> the class of objects in the array
 *
 * @author Marc Vaudel
 */
public class ConcurrentIterator<K> {

    /**
     * The queue to iterate.
     */
    private final ConcurrentLinkedQueue<K> queue;

    /**
     * Constructor.
     *
     * @param array The array to iterate.
     */
    public ConcurrentIterator(
            K[] array
    ) {

        this.queue = Arrays.stream(array)
                .collect(
                        Collectors.toCollection(ConcurrentLinkedQueue<K>::new)
                );

    }

    /**
     * Constructor.
     *
     * @param collection The collection to iterate.
     */
    public ConcurrentIterator(
            Collection<K> collection
    ) {

        this.queue = new ConcurrentLinkedQueue<>(collection);

    }

    /**
     * Returns the next object. Null if the end of the array was reached.
     *
     * @return The next object.
     */
    public K next() {

        return queue.poll();

    }

}
