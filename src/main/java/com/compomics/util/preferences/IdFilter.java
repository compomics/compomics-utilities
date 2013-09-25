package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.PeptideAssumption;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
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
     */
    private double mascotMaxEvalue;
    /**
     * OMSSA maximal e-value allowed.
     */
    private double omssaMaxEvalue;
    /**
     * X!Tandem maximal e-value allowed.
     */
    private double xtandemMaxEvalue;
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
        mascotMaxEvalue = -1;
        omssaMaxEvalue = -1;
        xtandemMaxEvalue = -1;
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
     * @param mascotMaxEvalue The maximal Mascot e-value allowed (0 or less for
     * disabled)
     * @param omssaMaxEvalue The maximal OMSSA e-value allowed (0 or less for
     * disabled)
     * @param xtandemMaxEvalue The maximal X!Tandem e-value allowed (0 or less
     * for disabled)
     * @param maxMzDeviation The maximal m/z deviation allowed (0 or less for
     * disabled)
     * @param isPpm Boolean indicating the unit of the allowed m/z deviation
     * (true: ppm, false: Da)
     * @param unknownPTM Shall peptides presenting unknownPTMs be ignored
     */
    public IdFilter(int minPepLength, int maxPepLength, double mascotMaxEvalue, double omssaMaxEvalue, double xtandemMaxEvalue, double maxMzDeviation, boolean isPpm, boolean unknownPTM) {
        this.minPepLength = minPepLength;
        this.maxPepLength = maxPepLength;
        this.mascotMaxEvalue = mascotMaxEvalue;
        this.omssaMaxEvalue = omssaMaxEvalue;
        this.xtandemMaxEvalue = xtandemMaxEvalue;
        this.maxMassDeviation = maxMzDeviation;
        this.isPpm = isPpm;
        this.unknownPtm = unknownPTM;
    }

    /**
     * Validates the peptide assumption based on the peptide length and maximal
     * e-values allowed.
     *
     * @param assumption the assumption to validate
     * @return a boolean indicating whether the assumption passed the test
     */
    public boolean validatePeptideAssumption(PeptideAssumption assumption) {

        int pepLength = assumption.getPeptide().getSequence().length();

        if (maxPepLength > 0 && pepLength > maxPepLength
                || minPepLength > 0 && pepLength < minPepLength) {
            return false;
        }

        int searchEngine = assumption.getAdvocate();
        double eValue = assumption.getScore();

        if ((searchEngine == Advocate.MASCOT && mascotMaxEvalue > 0 && eValue > mascotMaxEvalue)
                || (searchEngine == Advocate.OMSSA && omssaMaxEvalue > 0 && eValue > omssaMaxEvalue)
                || (searchEngine == Advocate.XTANDEM && xtandemMaxEvalue > 0 && eValue > xtandemMaxEvalue)) {
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
     * @param matchingType the desired peptide to protein matching type
     * @param massTolerance the ms2 mass tolerance
     * @param proteinTree the protein tree to use for peptide to protein mapping
     *
     * @return a boolean indicating whether the peptide passed the test
     */
    public boolean validateProteins(Peptide peptide, ProteinMatch.MatchingType matchingType, Double massTolerance) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        return validateProteins(peptide, matchingType, massTolerance, SequenceFactory.getInstance().getDefaultProteinTree());
    }

    /**
     * Validates a peptide depending on its protein inference status. Maps the
     * peptide to proteins in case it was not done before
     *
     * @param peptide the peptide
     * @param matchingType the desired peptide to protein matching type
     * @param massTolerance the ms2 mass tolerance
     * @param proteinTree the protein tree to use for peptide to protein mapping
     *
     * @return a boolean indicating whether the peptide passed the test
     */
    public boolean validateProteins(Peptide peptide, ProteinMatch.MatchingType matchingType, Double massTolerance, ProteinTree proteinTree) throws IOException, SQLException, ClassNotFoundException, InterruptedException {
        ArrayList<String> accessions = peptide.getParentProteins(matchingType, massTolerance, proteinTree);
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
     * @param matchingType the peptide-protein matching type
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a boolean indicating whether the peptide passed the test
     */
    public boolean validateModifications(Peptide peptide, ProteinMatch.MatchingType matchingType, Double massTolerance) {

        // check it it's an unknown peptide
        if (unknownPtm) {
            ArrayList<ModificationMatch> modificationMatches = peptide.getModificationMatches();
            for (ModificationMatch modMatch : modificationMatches) {
                if (modMatch.getTheoreticPtm().equals(PTMFactory.unknownPTM.getName())) {
                    return false;
                }
            }
        }

        PTMFactory ptmFactory = PTMFactory.getInstance();

        // get the variable ptms and the number of times they occur
        HashMap<String, ArrayList<ModificationMatch>> modMatches = new HashMap<String, ArrayList<ModificationMatch>>();
        for (ModificationMatch modMatch : peptide.getModificationMatches()) {
            if (modMatch.isVariable()) {
                String modName = modMatch.getTheoreticPtm();
                PTM ptm = ptmFactory.getPTM(modName);
                if (ptm.getType() == PTM.MODAA) {
                    if (!modMatches.containsKey(modName)) {
                        modMatches.put(modName, new ArrayList<ModificationMatch>());
                    }
                    modMatches.get(modName).add(modMatch);
                }
            }
        }

        // check if there are more ptms than ptm sites
        for (String modName : modMatches.keySet()) {
            try {
                ArrayList<Integer> possiblePositions = peptide.getPotentialModificationSites(ptmFactory.getPTM(modName), matchingType, massTolerance);
                if (possiblePositions.size() < modMatches.get(modName).size()) {
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

        if (maxMassDeviation > 0 && Math.abs(assumption.getDeltaMass(precursor.getMz(), isPpm)) > maxMassDeviation) {
            return false;
        }

        return true;
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
     * Returns the maximal Mascot e-value allowed.
     *
     * @return the maximal Mascot e-value allowed
     */
    public double getMascotMaxEvalue() {
        return mascotMaxEvalue;
    }

    /**
     * Sets the maximal Mascot e-value allowed.
     *
     * @param mascotMaxEvalue the maximal Mascot e-value allowed
     */
    public void setMascotMaxEvalue(double mascotMaxEvalue) {
        this.mascotMaxEvalue = mascotMaxEvalue;
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
     * Returns the OMSSA maximal e-value allowed.
     *
     * @return the OMSSA maximal e-value allowed
     */
    public double getOmssaMaxEvalue() {
        return omssaMaxEvalue;
    }

    /**
     * Sets the OMSSA maximal e-value allowed.
     *
     * @param omssaMaxEvalue the OMSSA maximal e-value allowed
     */
    public void setOmssaMaxEvalue(double omssaMaxEvalue) {
        this.omssaMaxEvalue = omssaMaxEvalue;
    }

    /**
     * Returns the maximal X!Tandem e-value allowed.
     *
     * @return the OMSSA maximal e-value allowed
     */
    public double getXtandemMaxEvalue() {
        return xtandemMaxEvalue;
    }

    /**
     * Sets the OMSSA maximal e-value allowed.
     *
     * @param xtandemMaxEvalue the OMSSA maximal e-value allowed
     */
    public void setXtandemMaxEvalue(double xtandemMaxEvalue) {
        this.xtandemMaxEvalue = xtandemMaxEvalue;
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
                && mascotMaxEvalue == anotherFilter.getMascotMaxEvalue()
                && omssaMaxEvalue == anotherFilter.getOmssaMaxEvalue()
                && xtandemMaxEvalue == anotherFilter.getXtandemMaxEvalue()
                && maxMassDeviation == anotherFilter.getMaxMzDeviation();
    }
}
