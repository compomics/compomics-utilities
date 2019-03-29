package com.compomics.util.experiment.identification.protein_sequences;

import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashSet;

/**
 * Sequence provider for a single protein.
 *
 * @author Marc Vaudel
 */
public class SingleProteinSequenceProvider implements SequenceProvider {

    /**
     * The protein.
     */
    private final Protein protein;
    
    /**
     * Empty default constructor.
     */
    public SingleProteinSequenceProvider() {
        protein = null;
    }
    
    /**
     * Constructor.
     * 
     * @param protein the protein
     */
    public SingleProteinSequenceProvider(Protein protein) {
        this.protein = protein;
    }

    @Override
    public Collection<String> getAccessions() {
      return  Lists.newArrayList(protein.getAccession());
    }

    @Override
    public HashSet<String> getDecoyAccessions() {
        throw new UnsupportedOperationException("Not available for this sequence provider.");
    }

    @Override
    public String getSequence(String proteinAccession) {
        return protein.getSequence();
    }

    @Override
    public String getSubsequence(String accession, int start, int end) {
        return protein.getSequence().substring(start, end);
    }

    @Override
    public String getHeader(String proteinAccession) {
        throw new UnsupportedOperationException("Not available for this sequence provider.");
    }
 
}
