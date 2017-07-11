package com.compomics.util.experiment.identification.matches_iterators;

import com.compomics.util.experiment.identification.Identification;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.personalization.UrParameter;
import com.compomics.util.waiting.WaitingHandler;
import com.orientechnologies.orient.object.iterator.OObjectIteratorClass;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * An iterator for spectrum matches.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 * @author Dominik Kopczynski
 */
public class PsmIterator {
    /**
     * Iterator for spectrum matches
     */
    private OObjectIteratorClass<?> it;
    
    /**
     * Constructor.
     *
     * @param identification the identification where to get the matches from
     * @throws java.sql.SQLException
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     * @throws java.lang.InterruptedException
     */
    public PsmIterator(Identification identification) throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        it = identification.getIterator(SpectrumMatch.class.getSimpleName());
    }

    /**
     * Indicates whether the iterator is done iterating. Warning: this method
     * can be wrong when multi threading.
     *
     * @return false if the iterator is done iterating
     */
    public boolean hasNext() {
        return it.hasNext();
    }

    /**
     * Returns the next match and updates the buffer. Null if the iterator is
     * done iterating.
     *
     * @return the next match
     *
     * @throws SQLException exception thrown whenever an error occurred while
     * interacting with the matches database
     * @throws IOException exception thrown whenever an error occurred while
     * interacting with the matches database
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing a match from the database
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while retrieving the match
     */
    public SpectrumMatch next() throws SQLException, IOException, ClassNotFoundException, InterruptedException {
        return (SpectrumMatch)it.next();
    }
}
