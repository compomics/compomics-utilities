package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.ModificationProfile;
import java.io.*;
import java.util.HashMap;

/**
 * This class groups the parameters used for identification.
 *
 * @author Marc Vaudel
 */
public class SearchParameters implements Serializable {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = -2773993307168773763L;

    /**
     * Possible mass accuracy types.
     */
    public enum PrecursorAccuracyType {

        PPM, DA
    };
    /**
     * The precursor accuracy type. Default is ppm.
     */
    private PrecursorAccuracyType currentPrecursorAccuracyType = PrecursorAccuracyType.PPM;
    /**
     * The precursor mass tolerance.
     */
    private Double precursorTolerance = 10.0;
    /**
     * The ms2 ion tolerance.
     */
    private Double fragmentIonMZTolerance = 0.5;
    /**
     * The expected modifications. Modified peptides will be grouped and
     * displayed according to this classification.
     */
    private ModificationProfile utilitiesModificationProfile = new ModificationProfile();
    /**
     * The enzyme used for digestion.
     */
    private Enzyme enzyme;
    /**
     * The allowed number of missed cleavages.
     */
    private Integer nMissedCleavages = 2;
    /**
     * The sequence database file used for identification.
     */
    private File fastaFile;
    /**
     * The corresponding searchGUI file.
     */
    private File parametersFile;
    /**
     * The list of fraction molecular weights. The key is the fraction file
     * path.
     */
    private HashMap<String, Double> fractionMolecularWeights = new HashMap<String, Double>();
    /**
     * The first kind of ions searched for (typically a, b or c).
     */
    private Integer forwardIon = PeptideFragmentIon.B_ION;
    /**
     * The second kind of ions searched for (typically x, y or z).
     */
    private Integer rewindIon = PeptideFragmentIon.Y_ION;
    /**
     * The minimal charge searched (in absolute value)
     */
    private Charge minChargeSearched = new Charge(Charge.PLUS, 2);
    /**
     * The minimal charge searched (in absolute value)
     */
    private Charge maxChargeSearched = new Charge(Charge.PLUS, 4);
    /**
     * Convenience array for forward ion type selection.
     */
    private static String[] forwardIons = {"a", "b", "c"};
    /**
     * Convenience array for rewind ion type selection.
     */
    private static String[] rewindIons = {"x", "y", "z"};
    /**
     * Maximal e-value cut-off.
     */
    private Double maxEValue = 100.0;
    /**
     * The maximal hitlist length (OMSSA setting).
     */
    private Integer hitListLength = 25;
    /**
     * The minimal charge to be considered for multiple fragment charges for
     * OMSSA.
     */
    private Charge minimalChargeForMultipleChargedFragments = new Charge(Charge.PLUS, 3);
    /**
     * The minimum peptide length.
     */
    private Integer minPeptideLength;
    /**
     * The maximal peptide length.
     */
    private Integer maxPeptideLength;
    /**
     * Indicates whether the precursor removal option of OMSSA is used.
     */
    private Boolean removePrecursor = true;
    /**
     * Indicates whether the precursor scaling option of OMSSA is used.
     */
    private Boolean scalePrecursor = false;
    /**
     * Indicates whether the precursor charge estimation option of OMSSA is
     * used.
     */
    private Boolean estimateCharge = true;

    /**
     * Constructor.
     */
    public SearchParameters() {
    }

    /**
     * Returns the modification profile of the project.
     *
     * @return the modification profile of the project
     */
    public ModificationProfile getModificationProfile() {
        return utilitiesModificationProfile;
    }

    /**
     * Sets the modification profile of the project.
     *
     * @param modificationProfile The modification profile
     */
    public void setModificationProfile(ModificationProfile modificationProfile) {
        this.utilitiesModificationProfile = modificationProfile;
    }

    /**
     * Returns the ms2 ion m/z tolerance.
     *
     * @return the ms2 ion m/z tolerance
     */
    public Double getFragmentIonAccuracy() {
        return fragmentIonMZTolerance;
    }

    /**
     * Sets the fragment ion m/z tolerance.
     *
     * @param fragmentIonMZTolerance
     */
    public void setFragmentIonAccuracy(Double fragmentIonMZTolerance) {
        this.fragmentIonMZTolerance = fragmentIonMZTolerance;
    }

    /**
     * Returns the enzyme used for digestion.
     *
     * @return the enzyme used for digestion
     */
    public Enzyme getEnzyme() {
        return enzyme;
    }

    /**
     * Sets the enzyme used for digestion.
     *
     * @param enzyme the enzyme used for digestion
     */
    public void setEnzyme(Enzyme enzyme) {
        this.enzyme = enzyme;
    }

    /**
     * Returns the parameters file loaded.
     *
     * @return the parameters file loaded
     */
    public File getParametersFile() {
        return parametersFile;
    }

    /**
     * Sets the parameter file loaded.
     *
     * @param parametersFile the parameter file loaded
     */
    public void setParametersFile(File parametersFile) {
        this.parametersFile = parametersFile;
    }

    /**
     * Returns the sequence database file used for identification.
     *
     * @return the sequence database file used for identification
     */
    public File getFastaFile() {
        return fastaFile;
    }

    /**
     * Sets the sequence database file used for identification.
     *
     * @param fastaFile the sequence database file used for identification
     */
    public void setFastaFile(File fastaFile) {
        this.fastaFile = fastaFile;
    }

    /**
     * Returns the allowed number of missed cleavages.
     *
     * @return the allowed number of missed cleavages
     */
    public Integer getnMissedCleavages() {
        return nMissedCleavages;
    }

    /**
     * Sets the allowed number of missed cleavages.
     *
     * @param nMissedCleavages the allowed number of missed cleavages
     */
    public void setnMissedCleavages(Integer nMissedCleavages) {
        this.nMissedCleavages = nMissedCleavages;
    }

    /**
     * Getter for the first kind of ion searched.
     *
     * @return the first kind of ion searched as an integer (see static fields
     * of the PeptideFragmentIon class)
     */
    public Integer getIonSearched1() {
        return forwardIon;
    }

    /**
     * Setter for the first kind of ion searched, indexed by its single letter
     * code, for example "a".
     *
     * @param ionSearched1 the first kind of ion searched
     */
    public void setIonSearched1(String ionSearched1) {
        if (ionSearched1.equals("a")) {
            this.forwardIon = PeptideFragmentIon.A_ION;
        } else if (ionSearched1.equals("b")) {
            this.forwardIon = PeptideFragmentIon.B_ION;
        } else if (ionSearched1.equals("c")) {
            this.forwardIon = PeptideFragmentIon.C_ION;
        } else if (ionSearched1.equals("x")) {
            this.forwardIon = PeptideFragmentIon.X_ION;
        } else if (ionSearched1.equals("y")) {
            this.forwardIon = PeptideFragmentIon.Y_ION;
        } else if (ionSearched1.equals("z")) {
            this.forwardIon = PeptideFragmentIon.Z_ION;
        }
    }

    /**
     * Getter for the second kind of ion searched.
     *
     * @return the second kind of ion searched as an integer (see static fields
     * of the PeptideFragmentIon class)
     */
    public Integer getIonSearched2() {
        return rewindIon;
    }

    /**
     * Setter for the second kind of ion searched, indexed by its single letter
     * code, for example "a".
     *
     * @param ionSearched2 the second kind of ion searched
     */
    public void setIonSearched2(String ionSearched2) {
        if (ionSearched2.equals("a")) {
            this.rewindIon = PeptideFragmentIon.A_ION;
        } else if (ionSearched2.equals("b")) {
            this.rewindIon = PeptideFragmentIon.B_ION;
        } else if (ionSearched2.equals("c")) {
            this.rewindIon = PeptideFragmentIon.C_ION;
        } else if (ionSearched2.equals("x")) {
            this.rewindIon = PeptideFragmentIon.X_ION;
        } else if (ionSearched2.equals("y")) {
            this.rewindIon = PeptideFragmentIon.Y_ION;
        } else if (ionSearched2.equals("z")) {
            this.rewindIon = PeptideFragmentIon.Z_ION;
        }
    }

    /**
     * Getter for the list of ion symbols used.
     *
     * @return the list of ion symbols used
     */
    public static String[] getIons() {
        String[] ions = new String[forwardIons.length + rewindIons.length];
        for (int i = 0; i < forwardIons.length; i++) {
            ions[ions.length] = forwardIons[i];
        }
        for (int i = 0; i < rewindIons.length; i++) {
            ions[ions.length] = rewindIons[i];
        }
        return ions;
    }

    /**
     * Returns the list of forward ions.
     *
     * @return the forwardIons
     */
    public static String[] getForwardIons() {
        return forwardIons;
    }

    /**
     * Returns the list of rewind ions.
     *
     * @return the rewindIons
     */
    public static String[] getRewindIons() {
        return rewindIons;
    }

    /**
     * Returns the precursor tolerance.
     *
     * @return the precursor tolerance
     */
    public Double getPrecursorAccuracy() {
        return precursorTolerance;
    }

    /**
     * Sets the precursor tolerance.
     *
     * @param precursorTolerance the precursor tolerance
     */
    public void setPrecursorAccuracy(Double precursorTolerance) {
        this.precursorTolerance = precursorTolerance;
    }

    /**
     * Returns the precursor accuracy type.
     *
     * @return the precursor accuracy type
     */
    public PrecursorAccuracyType getPrecursorAccuracyType() {
        return currentPrecursorAccuracyType;
    }

    /**
     * Sets the precursor accuracy type.
     *
     * @param currentPrecursorAccuracyType the precursor accuracy type
     */
    public void setPrecursorAccuracyType(PrecursorAccuracyType currentPrecursorAccuracyType) {
        this.currentPrecursorAccuracyType = currentPrecursorAccuracyType;
    }

    /**
     * Returns true if the current precursor accuracy type is ppm.
     *
     * @return true if the current precursor accuracy type is ppm
     */
    public Boolean isPrecursorAccuracyTypePpm() {
        return currentPrecursorAccuracyType == PrecursorAccuracyType.PPM;
    }

    /**
     * Returns the user provided molecular weights of the fractions. The key is
     * the fraction file path.
     *
     * @return the user provided molecular weights of the fractions
     */
    public HashMap<String, Double> getFractionMolecularWeights() {
        return fractionMolecularWeights;
    }

    /**
     * Set the user provided molecular weights of the fractions. The key is the
     * fraction file path.
     *
     * @param fractionMolecularWeights the fractionMolecularWeights to set
     */
    public void setFractionMolecularWeights(HashMap<String, Double> fractionMolecularWeights) {
        this.fractionMolecularWeights = fractionMolecularWeights;
    }

    /**
     * Returns the maximal charge searched.
     *
     * @return the maximal charge searched
     */
    public Charge getMaxChargeSearched() {
        return maxChargeSearched;
    }

    /**
     * Sets the maximal charge searched.
     *
     * @param maxChargeSearched the maximal charge searched
     */
    public void setMaxChargeSearched(Charge maxChargeSearched) {
        this.maxChargeSearched = maxChargeSearched;
    }

    /**
     * Returns the minimal charge searched.
     *
     * @return the minimal charge searched
     */
    public Charge getMinChargeSearched() {
        return minChargeSearched;
    }

    /**
     * Sets the minimal charge searched.
     *
     * @param minChargeSearched the minimal charge searched
     */
    public void setMinChargeSearched(Charge minChargeSearched) {
        this.minChargeSearched = minChargeSearched;
    }

    /**
     * Returns the maximal e-value searched for.
     *
     * @return the maximal e-value searched for
     */
    public Double getMaxEValue() {
        return maxEValue;
    }

    /**
     * Sets the maximal e-value searched for.
     *
     * @param maxEValue the maximal e-value searched for
     */
    public void setMaxEValue(Double maxEValue) {
        this.maxEValue = maxEValue;
    }

    /**
     * Returns the length of the hitlist for OMSSA.
     *
     * @return the length of the hitlist for OMSSA
     */
    public Integer getHitListLength() {
        return hitListLength;
    }

    /**
     * Sets the length of the hitlist for OMSSA.
     *
     * @param hitListLength the length of the hitlist for OMSSA
     */
    public void setHitListLength(Integer hitListLength) {
        this.hitListLength = hitListLength;
    }

    /**
     * Returns the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @return the minimal precursor charge to account for multiply charged
     * fragments in OMSSA
     */
    public Charge getMinimalChargeForMultipleChargedFragments() {
        return minimalChargeForMultipleChargedFragments;
    }

    /**
     * Sets the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @param minimalChargeForMultipleChargedFragments the minimal precursor
     * charge to account for multiply charged fragments in OMSSA
     */
    public void setMinimalChargeForMultipleChargedFragments(Charge minimalChargeForMultipleChargedFragments) {
        this.minimalChargeForMultipleChargedFragments = minimalChargeForMultipleChargedFragments;
    }

    /**
     * Returns the maximal peptide length allowed.
     *
     * @return the maximal peptide length allowed
     */
    public Integer getMaxPeptideLength() {
        return maxPeptideLength;
    }

    /**
     * Sets the maximal peptide length allowed.
     *
     * @param maxPeptideLength the maximal peptide length allowed
     */
    public void setMaxPeptideLength(Integer maxPeptideLength) {
        this.maxPeptideLength = maxPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @return the minimal peptide length allowed
     */
    public Integer getMinPeptideLength() {
        return minPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @param minPeptideLength the minimal peptide length allowed
     */
    public void setMinPeptideLength(Integer minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Indicates whether the precursor charge shall be estimated for OMSSA.
     *
     * @return a boolean indicating whether the precursor charge shall be
     * estimated for OMSSA
     */
    public Boolean isEstimateCharge() {
        return estimateCharge;
    }

    /**
     * Sets whether the precursor charge shall be estimated for OMSSA.
     *
     * @param estimateCharge a boolean indicating whether the precursor charge
     * shall be estimated for OMSSA
     */
    public void setEstimateCharge(Boolean estimateCharge) {
        this.estimateCharge = estimateCharge;
    }

    /**
     * Indicates whether the precursor shall be removed for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be removed for
     * OMSSA
     */
    public Boolean isRemovePrecursor() {
        return removePrecursor;
    }

    /**
     * Sets whether the precursor shall be removed for OMSSA
     *
     * @param removePrecursor a boolean indicating whether the precursor shall
     * be removed for OMSSA
     */
    public void setRemovePrecursor(Boolean removePrecursor) {
        this.removePrecursor = removePrecursor;
    }

    /**
     * Indicates whether the precursor shall be scaled for OMSSA.
     *
     * @return a boolean indicating whether the precursor shall be scaled for
     * OMSSA
     */
    public Boolean isScalePrecursor() {
        return scalePrecursor;
    }

    /**
     * Sets whether the precursor shall be scaled for OMSSA.
     *
     * @param scalePrecursor a boolean indicating whether the precursor shall be
     * scaled for OMSSA
     */
    public void setScalePrecursor(Boolean scalePrecursor) {
        this.scalePrecursor = scalePrecursor;
    }

    /**
     * Loads the identification parameters from a serialized file.
     *
     * @param file the file
     * @return the modification file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static SearchParameters getIdentificationParameters(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
        return (SearchParameters) SerializationUtils.readObject(file);
    }

    /**
     * Saves the a modification profile from a serialized file.
     *
     * @param identificationParameters
     * @param file the file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void saveIdentificationParameters(SearchParameters identificationParameters, File file) throws FileNotFoundException, IOException, ClassNotFoundException {
        SerializationUtils.writeObject(identificationParameters, file);
    }
}
