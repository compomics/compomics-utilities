package com.compomics.util.experiment.identification.matches;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.IdentificationMatch;
import java.util.Arrays;

/**
 * This class models a peptide match.
 *
 * @author Marc Vaudel
 * @author Dominik Kopczynski
 */
public class PeptideMatch extends IdentificationMatch {

    /**
     * The version UID for serialization/deserialization compatibility.
     */
    static final long serialVersionUID = 7195830246336841081L;
    /**
     * The peptide.
     */
    private Peptide peptide;
    /**
     * The key of the match.
     */
    private long key;
    /**
     * The keys of the spectrum matches linking to this peptide match.
     */
    private long[] spectrumMatchesKeys;
    /**
     * Is the peptide match a decoy hit?
     */
    private boolean isDecoy = false;

    /**
     * Indicates whether the peptide maps to a decoy sequence.
     *
     * @return a boolean indicating whether the peptide maps to a decoy sequence
     */
    public boolean getIsDecoy() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return isDecoy;
        
    }

    /**
     * Sets a boolean indicating whether the peptide maps to a decoy sequence.
     *
     * @param isDecoy a boolean indicating whether the peptide maps to a decoy
     * sequence
     */
    public void setIsDecoy(boolean isDecoy) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.isDecoy = isDecoy;
        
    }

    @Override
    public long getKey() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return key;
    }

    /**
     * Sets a new key for the match.
     *
     * @param newKey a new key for the match
     */
    public void setKey(long newKey) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.key = newKey;
    }

    /**
     * Constructor for the peptide match.
     *
     * @param peptide the matching peptide
     * @param matchKey the key of the match as referenced in the identification
     * @param spectrumMatchKey the key of a spectrum match linked to this peptide
     */
    public PeptideMatch(Peptide peptide, long matchKey, long spectrumMatchKey) {
        
        this.peptide = peptide;
        this.key = matchKey;
        
        spectrumMatchesKeys = new long[1];
        spectrumMatchesKeys[0] = spectrumMatchKey;
        
    }

    /**
     * Getter for the peptide.
     *
     * @return the peptide
     */
    public Peptide getPeptide() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return peptide;
    }

    /**
     * Setter for the peptide.
     *
     * @param peptide a peptide
     */
    public void setPeptide(Peptide peptide) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.peptide = peptide;
    }

    /**
     * Returns the keys of all spectra matched.
     *
     * @return the keys of all spectrum matches
     */
    public long[] getSpectrumMatchesKeys() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return spectrumMatchesKeys;
        
    }

    /**
     * Sets the spectrum matches keys.
     *
     * @param spectrumMatchesKeys the keys
     */
    public void setSpectrumMatchesKeys(long[] spectrumMatchesKeys) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        this.spectrumMatchesKeys = spectrumMatchesKeys;
        
    }

    /**
     * Add a spectrum match key.
     *
     * @param spectrumMatchKey the key of a spectrum match
     */
    public void addSpectrumMatchKey(long spectrumMatchKey) {
        
        ObjectsDB.increaseRWCounter();
        zooActivateWrite();
        ObjectsDB.decreaseRWCounter();
        
        spectrumMatchesKeys =  Arrays.copyOf(spectrumMatchesKeys, spectrumMatchesKeys.length + 1);
        
        spectrumMatchesKeys[spectrumMatchesKeys.length - 1] = spectrumMatchKey;
        
    }

    /**
     * Returns the number of spectra matched.
     *
     * @return spectrum count
     */
    public int getSpectrumCount() {
        
        ObjectsDB.increaseRWCounter();
        zooActivateRead();
        ObjectsDB.decreaseRWCounter();
        
        return spectrumMatchesKeys.length;
    }

    @Override
    public MatchType getType() {
        return MatchType.Peptide;
    }
}
