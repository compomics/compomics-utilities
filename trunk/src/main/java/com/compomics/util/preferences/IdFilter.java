package com.compomics.util.preferences;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * This class achieves a pre-filtering of the identifications for PeptideShaker.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IdFilter implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 8416219001106063781L;
    /**
     * The minimal peptide length allowed.
     */
    private int minPepLength;
    /**
     * The maximal peptide length allowed.
     */
    private int maxPepLength;
    /**
     * Mascot maximal e-value allowed.
     *
     * @deprecated
     */
    private double mascotMaxEvalue;
    /**
     * OMSSA maximal e-value allowed.
     *
     * @deprecated
     */
    private double omssaMaxEvalue;
    /**
     * X!Tandem maximal e-value allowed.
     *
     * @deprecated
     */
    private double xtandemMaxEvalue;
    /**
     * MS-GF+ maximal e-value allowed.
     *
     * @deprecated
     */
    private double msgfMaxEvalue;
    /**
     * MS Amanda maximal e-value allowed.
     *
     * @deprecated
     */
    private double msAmandaMaxEvalue;
    /**
     * The maximal m/z deviation allowed.
     */
    private double maxMassDeviation;
    /**
     * Boolean indicating the unit of the allowed m/z deviation (true: ppm,
     * false: Da).
     */
    private boolean isPpm;
    /**
     * Boolean indicating whether peptides presenting unknown PTMs should be
     * ignored.
     */
    private boolean unknownPtm;

    /**
     * Constructor with default settings.
     */
    public IdFilter() {
        minPepLength = 4;
        maxPepLength = 30;
        maxMassDeviation = -1;
        isPpm = true;
        unknownPtm = true;
    }

    /**
     * Constructor for an Identification filter.
     *
     * @param minPepLength The minimal peptide length allowed (0 or less for
     * disabled)
     * @param maxPepLength The maximal peptide length allowed (0 or less for
     * disabled)
     * @param maxMzDeviation The maximal m/z deviation allowed (0 or less for
     * disabled)
     * @param isPpm Boolean indicating the unit of the allowed m/z deviation
     * (true: ppm, false: Da)
     * @param unknownPTM Shall peptides presenting unknownPTMs be ignored
     */
    public IdFilter(int minPepLength, int maxPepLength, double maxMzDeviation, boolean isPpm, boolean unknownPTM) {
        this.minPepLength = minPepLength;
        this.maxPepLength = maxPepLength;
        this.maxMassDeviation = maxMzDeviation;
        this.isPpm = isPpm;
        this.unknownPtm = unknownPTM;
    }

    /**
     * Validates the peptide based on the peptide length and share of Xs in the
     * sequence.
     *
     * @param peptide the peptide to validate
     * @param sequenceMatchingPreferences the sequence matching preferences
     * containing the maximal share of Xs allowed
     *
     * @return a boolean indicating whether the peptide passed the test
     */
    public boolean validatePeptide(Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences) {

        String peptideSequence = peptide.getSequence();
        int sequenceLength = peptideSequence.length();

        if ((maxPepLength > 0 && sequenceLength > maxPepLength)
                || (minPepLength > 0 && sequenceLength < minPepLength)) {
            return false;
        }

        double xShare = ((double) Util.getOccurrence(peptideSequence, 'X')) / sequenceLength;
        if (sequenceMatchingPreferences.hasLimitX() && xShare > sequenceMatchingPreferences.getLimitX()) {
            return false;
        }

        return true;
    }

    /**
     * Validates a peptide depending on its protein inference status. Maps the
     * peptide to proteins in case it was not done before using the default
     * protein tree of the sequence factory
     *
     * @param peptide the peptide
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @return a boolean indicating whether the peptide passed the test
     *
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public boolean validateProteins(Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        return validateProteins(peptide, sequenceMatchingPreferences, SequenceFactory.getInstance().getDefaultProteinTree());
    }

    /**
     * Validates a peptide depending on its protein inference status. Maps the
     * peptide to proteins in case it was not done before
     *
     * @param peptide the peptide
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param proteinTree the protein tree to use for peptide to protein mapping
     *
     * @return a boolean indicating whether the peptide passed the test
     *
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public boolean validateProteins(Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences, ProteinTree proteinTree)
            throws IOException, SQLException, ClassNotFoundException, InterruptedException {

        ArrayList<String> accessions = peptide.getParentProteins(sequenceMatchingPreferences, proteinTree);

        if (accessions.size() > 1) {
            boolean target = false;
            boolean decoy = false;
            for (String accession : accessions) {
                if (SequenceFactory.getInstance().isDecoyAccession(accession)) {
                    decoy = true;
                } else {
                    target = true;
                }
            }
            if (target && decoy) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the modifications of a peptide.
     *
     * @param peptide the peptide of interest
     * @param sequenceMatchingPreferences the sequence matching preferences
     * @param modificationProfile the modification profile of the identification
     *
     * @return a boolean indicating whether the peptide passed the test
     */
    public boolean validateModifications(Peptide peptide, SequenceMatchingPreferences sequenceMatchingPreferences, ModificationProfile modificationProfile) {

        // check it it's an unknown peptide
        if (unknownPtm) {
            ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
            for (ModificationMatch modMatch : modificationMatches) {
                String ptmName = modMatch.getTheoreticPtm();
                if (ptmName.equals(PTMFactory.unknownPTM.getName())) {
                    return false;
                }
            }
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();

        // get the variable ptms and the number of times they occur
        HashMap<Double, Integer> modMatches = new HashMap<Double, Integer>();
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            if (modMatch.isVariable()) {
                String modName = modMatch.getTheoreticPtm();
                PTM ptm = ptmFactory.getPTM(modName);
                double mass = ptm.getMass();
                if (!modMatches.containsKey(mass)) {
                    modMatches.put(mass, 1);
                } else {
                    modMatches.put(mass, modMatches.get(mass) + 1);
                }
            }
        }

        // check if there are more ptms than ptm sites
        for (double mass : modMatches.keySet()) {
            try {
                ArrayList<Integer> possiblePositions = peptide.getPotentialModificationSites(mass, sequenceMatchingPreferences, modificationProfile);
                if (possiblePositions.size() < modMatches.get(mass)) {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the mass deviation of a peptide assumption.
     *
     * @param assumption the considered peptide assumption
     * @param spectrumKey the key of the spectrum used to get the precursor the
     * precursor should be accessible via the spectrum factory
     *
     * @param spectrumFactory the spectrum factory
     * @return a boolean indicating whether the given assumption passes the
     * filter
     * @throws IOException
     * @throws MzMLUnmarshallerException
     */
    public boolean validatePrecursor(PeptideAssumption assumption, String spectrumKey, SpectrumFactory spectrumFactory) throws IOException, MzMLUnmarshallerException {
        Precursor precursor = spectrumFactory.getPrecursor(spectrumKey);
        return (maxMassDeviation <= 0 || Math.abs(assumption.getDeltaMass(precursor.getMz(), isPpm)) <= maxMassDeviation);
    }

    /**
     * Returns a boolean indicating whether unknown PTMs shall be removed.
     *
     * @return a boolean indicating whether unknown PTMs shall be removed
     */
    public boolean removeUnknownPTMs() {
        return unknownPtm;
    }

    /**
     * Set whether unknown PTMs shall be removed.
     *
     * @param unknownPtm whether unknown PTMs shall be removed
     */
    public void setRemoveUnknownPTMs(boolean unknownPtm) {
        this.unknownPtm = unknownPtm;
    }

    /**
     * Indicates whether the mass tolerance is in ppm (true) or Dalton (false).
     *
     * @return a boolean indicating whether the mass tolerance is in ppm (true)
     * or Dalton (false)
     */
    public boolean isIsPpm() {
        return isPpm;
    }

    /**
     * Sets whether the mass tolerance is in ppm (true) or Dalton (false).
     *
     * @param isPpm a boolean indicating whether the mass tolerance is in ppm
     * (true) or Dalton (false)
     */
    public void setIsPpm(boolean isPpm) {
        this.isPpm = isPpm;
    }

    /**
     * Returns the maximal m/z deviation allowed.
     *
     * @return the maximal mass deviation allowed
     */
    public double getMaxMzDeviation() {
        return maxMassDeviation;
    }

    /**
     * Sets the maximal m/z deviation allowed.
     *
     * @param maxMzDeviation the maximal mass deviation allowed
     */
    public void setMaxMzDeviation(double maxMzDeviation) {
        this.maxMassDeviation = maxMzDeviation;
    }

    /**
     * Returns the maximal peptide length allowed.
     *
     * @return the maximal peptide length allowed
     */
    public int getMaxPepLength() {
        return maxPepLength;
    }

    /**
     * Sets the maximal peptide length allowed.
     *
     * @param maxPepLength the maximal peptide length allowed
     */
    public void setMaxPepLength(int maxPepLength) {
        this.maxPepLength = maxPepLength;
    }

    /**
     * Returns the maximal peptide length allowed.
     *
     * @return the maximal peptide length allowed
     */
    public int getMinPepLength() {
        return minPepLength;
    }

    /**
     * Sets the maximal peptide length allowed.
     *
     * @param minPepLength the maximal peptide length allowed
     */
    public void setMinPepLength(int minPepLength) {
        this.minPepLength = minPepLength;
    }

    /**
     * Indicates whether this filter is the same as another one.
     *
     * @param anotherFilter another filter
     * @return a boolean indicating that the filters have the same parameters
     */
    public boolean equals(IdFilter anotherFilter) {
        return isPpm == anotherFilter.isPpm
                && unknownPtm == anotherFilter.removeUnknownPTMs()
                && minPepLength == anotherFilter.getMinPepLength()
                && maxPepLength == anotherFilter.getMaxPepLength()
                && maxMassDeviation == anotherFilter.getMaxMzDeviation();
    }
}
