package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.identification_parameters.OmssaParameters;
import com.compomics.util.experiment.identification.identification_parameters.PepnovoParameters;
import com.compomics.util.experiment.identification.identification_parameters.XtandemParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.io.SerializationUtils;
import com.compomics.util.preferences.ModificationProfile;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.uib.jsparklines.data.XYDataPoint;

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
    public enum MassAccuracyType {

        PPM, DA
    };
    /**
     * The precursor accuracy type. Default is ppm.
     */
    private MassAccuracyType precursorAccuracyType = MassAccuracyType.PPM;
    /**
     * The fragment accuracy type. Default is Da.
     */
    private MassAccuracyType fragmentAccuracyType = MassAccuracyType.DA;
    /**
     * The precursor mass tolerance.
     */
    private Double precursorTolerance = 10.0;
    /**
     * The precursor mass tolerance in Dalton (for de novo searches).
     */
    private Double precursorToleranceDalton = 0.5;
    /**
     * The MS2 ion tolerance.
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
     *
     * @deprecated use fractionMolecularWeightRanges instead
     */
    private HashMap<String, Double> fractionMolecularWeights = new HashMap<String, Double>();
    /**
     * The list of fraction molecular weights. The key is the fraction file
     * path.
     */
    private HashMap<String, XYDataPoint> fractionMolecularWeightRanges = new HashMap<String, XYDataPoint>();
    /**
     * The first kind of ions searched for (typically a, b or c).
     */
    private Integer forwardIon = PeptideFragmentIon.B_ION;
    /**
     * The second kind of ions searched for (typically x, y or z).
     */
    private Integer rewindIon = PeptideFragmentIon.Y_ION;
    /**
     * The minimal charge searched (in absolute value).
     */
    private Charge minChargeSearched = new Charge(Charge.PLUS, 2);
    /**
     * The minimal charge searched (in absolute value).
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
     * The algorithm specific parameters.
     */
    private HashMap<Integer, IdentificationAlgorithmParameter> algorithmParameters;

    /**
     * Possible mass accuracy types.
     *
     * @deprecated use MassAccuracyType
     */
    public enum PrecursorAccuracyType {

        PPM, DA
    };
    /**
     * The precursor accuracy type. Default is ppm.
     *
     * @deprecated use precursorAccuracyType
     */
    private PrecursorAccuracyType currentPrecursorAccuracyType = PrecursorAccuracyType.PPM;
    /**
     * Maximal e-value cut-off. (OMSSA and X!Tandem only.)
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Double maxEValue = 100.0;
    /**
     * The maximal hit list length (OMSSA setting only).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Integer hitListLength = 25;
    /**
     * The maximal hit list length for PepNovo+.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Integer hitListLengthDeNovo = 10; // max is 20
    /**
     * The minimal charge to be considered for multiple fragment charges for
     * OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Charge minimalChargeForMultipleChargedFragments = new Charge(Charge.PLUS, 3);
    /**
     * The minimum peptide length (for semi and non tryptic searches with
     * OMSSA).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Integer minPeptideLength = 6;
    /**
     * The maximal peptide length (for semi and non tryptic searches with
     * OMSSA).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Integer maxPeptideLength = 30;
    /**
     * Indicates whether the precursor removal option of OMSSA is used.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Boolean removePrecursor = false;
    /**
     * Indicates whether the precursor scaling option of OMSSA is used.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Boolean scalePrecursor = true;
    /**
     * Indicates whether the precursor charge estimation option (OMSSA and
     * DeNovoGUI).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Boolean estimateCharge = true;
    /**
     * Indicates whether the precursor mass shall be corrected (DeNovoGUI
     * setting).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Boolean correctPrecursorMass = true;
    /**
     * Indicates whether the low quality spectra shall be discarded (DeNovoGUI
     * setting).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Boolean discardLowQualitySpectra = true;
    /**
     * DeNovoGUI fragmentation model.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private String fragmentationModel = "CID_IT_TRYP";
    /**
     * Indicates whether a blast query shall be generated (DeNovoGUI setting).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Boolean generateQuery = false;
    /**
     * A map from the PepNovo PTM symbols to the utilities PTM names.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     */
    private Map<String, String> pepNovoPtmMap;

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
     * Returns the MS2 ion m/z tolerance.
     *
     * @return the MS2 ion m/z tolerance
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
        for (String forwardIon1 : forwardIons) {
            ions[ions.length] = forwardIon1;
        }
        for (String rewindIon1 : rewindIons) {
            ions[ions.length] = rewindIon1;
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
     * Returns the precursor tolerance in Dalton (for de novo searches).
     *
     * @return the precursor tolerance
     */
    public Double getPrecursorAccuracyDalton() {
        if (precursorToleranceDalton == null) {
            precursorToleranceDalton = 0.5; // for backwards compatability
        }
        return precursorToleranceDalton;
    }

    /**
     * Sets the precursor tolerance in Dalton (for de novo searches).
     *
     * @param precursorToleranceDalton the precursor tolerance
     */
    public void setPrecursorAccuracyDalton(Double precursorToleranceDalton) {
        this.precursorToleranceDalton = precursorToleranceDalton;
    }

    /**
     * Returns the precursor accuracy type.
     *
     * @return the precursor accuracy type
     */
    public MassAccuracyType getPrecursorAccuracyType() {
        if (precursorAccuracyType == null) { // Backward compatibility check
            if (currentPrecursorAccuracyType == PrecursorAccuracyType.PPM) {
                precursorAccuracyType = MassAccuracyType.PPM;
            } else {
                precursorAccuracyType = MassAccuracyType.DA;
            }
        }
        return precursorAccuracyType;
    }

    /**
     * Sets the precursor accuracy type.
     *
     * @param precursorAccuracyType the precursor accuracy type
     */
    public void setPrecursorAccuracyType(MassAccuracyType precursorAccuracyType) {
        this.precursorAccuracyType = precursorAccuracyType;
    }

    /**
     * Returns the fragment accuracy type.
     *
     * @return the fragment accuracy type
     */
    public MassAccuracyType getFragmentAccuracyType() {
        if (fragmentAccuracyType == null) { // Backward compatibility check
            fragmentAccuracyType = MassAccuracyType.DA;
        }
        return fragmentAccuracyType;
    }

    /**
     * Sets the fragment accuracy type.
     *
     * @param fragmentAccuracyType the fragment accuracy type
     */
    public void setFragmentAccuracyType(MassAccuracyType fragmentAccuracyType) {
        this.fragmentAccuracyType = fragmentAccuracyType;
    }

    /**
     * Returns true if the current precursor accuracy type is ppm.
     *
     * @return true if the current precursor accuracy type is ppm
     */
    public Boolean isPrecursorAccuracyTypePpm() {
        return getPrecursorAccuracyType() == MassAccuracyType.PPM;
    }

    /**
     * Returns the user provided molecular weights of the fractions. The key is
     * the fraction file path.
     *
     * @deprecated use getFractionMolecularWeightRanges instead
     * @return the user provided molecular weights of the fractions
     */
    public HashMap<String, Double> getFractionMolecularWeights() {
        return fractionMolecularWeights;
    }

    /**
     * Set the user provided molecular weights of the fractions. The key is the
     * fraction file path.
     *
     * @deprecated use setFractionMolecularWeightRanges instead
     * @param fractionMolecularWeights the fractionMolecularWeights to set
     */
    public void setFractionMolecularWeights(HashMap<String, Double> fractionMolecularWeights) {
        this.fractionMolecularWeights = fractionMolecularWeights;
    }

    /**
     * Returns the user provided molecular weight ranges for the fractions. The
     * key is the fraction file path.
     *
     * @return the user provided molecular weight ranges of the fractions
     */
    public HashMap<String, XYDataPoint> getFractionMolecularWeightRanges() {
        return fractionMolecularWeightRanges;
    }

    /**
     * Set the user provided molecular weight ranges for the fractions. The key
     * is the fraction file path.
     *
     * @param fractionMolecularWeightRanges the fractionMolecularWeightRanges to
     * set
     */
    public void setFractionMolecularWeightRanges(HashMap<String, XYDataPoint> fractionMolecularWeightRanges) {
        this.fractionMolecularWeightRanges = fractionMolecularWeightRanges;
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
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return the maximal e-value searched for
     */
    public Double getMaxEValue() {
        return maxEValue;
    }

    /**
     * Sets the maximal e-value searched for.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param maxEValue the maximal e-value searched for
     */
    public void setMaxEValue(Double maxEValue) {
        this.maxEValue = maxEValue;
    }

    /**
     * Returns the length of the hit list for OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return the length of the hit list for OMSSA
     */
    public Integer getHitListLength() {
        return hitListLength;
    }

    /**
     * Sets the length of the hit list for OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param hitListLength the length of the hit list for OMSSA
     */
    public void setHitListLength(Integer hitListLength) {
        this.hitListLength = hitListLength;
    }

    /**
     * Returns the length of the hit list for PepNovo+.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return the length of the hit list for OMSSA
     */
    public Integer getHitListLengthDeNovo() {
        return hitListLengthDeNovo;
    }

    /**
     * Sets the length of the hit list for PepNovo.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param hitListLengthDeNovo the length of the hit list for PepNovo
     */
    public void setHitListLengthDeNovo(Integer hitListLengthDeNovo) {
        this.hitListLengthDeNovo = hitListLengthDeNovo;
    }

    /**
     * Returns the minimal precursor charge to account for multiply charged
     * fragments in OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
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
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param minimalChargeForMultipleChargedFragments the minimal precursor
     * charge to account for multiply charged fragments in OMSSA
     */
    public void setMinimalChargeForMultipleChargedFragments(Charge minimalChargeForMultipleChargedFragments) {
        this.minimalChargeForMultipleChargedFragments = minimalChargeForMultipleChargedFragments;
    }

    /**
     * Returns the maximal peptide length allowed.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return the maximal peptide length allowed
     */
    public Integer getMaxPeptideLength() {
        return maxPeptideLength;
    }

    /**
     * Sets the maximal peptide length allowed.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param maxPeptideLength the maximal peptide length allowed
     */
    public void setMaxPeptideLength(Integer maxPeptideLength) {
        this.maxPeptideLength = maxPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return the minimal peptide length allowed
     */
    public Integer getMinPeptideLength() {
        return minPeptideLength;
    }

    /**
     * Sets the minimal peptide length allowed.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param minPeptideLength the minimal peptide length allowed
     */
    public void setMinPeptideLength(Integer minPeptideLength) {
        this.minPeptideLength = minPeptideLength;
    }

    /**
     * Indicates whether the precursor charge shall be estimated for OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return a boolean indicating whether the precursor charge shall be
     * estimated for OMSSA
     */
    public Boolean isEstimateCharge() {
        return estimateCharge;
    }

    /**
     * Sets whether the precursor charge shall be estimated for OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param estimateCharge a boolean indicating whether the precursor charge
     * shall be estimated for OMSSA
     */
    public void setEstimateCharge(Boolean estimateCharge) {
        this.estimateCharge = estimateCharge;
    }

    /**
     * Indicates whether the precursor shall be removed for OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return a boolean indicating whether the precursor shall be removed for
     * OMSSA
     */
    public Boolean isRemovePrecursor() {
        return removePrecursor;
    }

    /**
     * Sets whether the precursor shall be removed for OMSSA
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param removePrecursor a boolean indicating whether the precursor shall
     * be removed for OMSSA
     */
    public void setRemovePrecursor(Boolean removePrecursor) {
        this.removePrecursor = removePrecursor;
    }

    /**
     * Indicates whether the precursor shall be scaled for OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return a boolean indicating whether the precursor shall be scaled for
     * OMSSA
     */
    public Boolean isScalePrecursor() {
        return scalePrecursor;
    }

    /**
     * Sets whether the precursor shall be scaled for OMSSA.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param scalePrecursor a boolean indicating whether the precursor shall be
     * scaled for OMSSA
     */
    public void setScalePrecursor(Boolean scalePrecursor) {
        this.scalePrecursor = scalePrecursor;
    }

    /**
     * Returns the algorithm specific parameters in a map: algorithm as indexed
     * in the Advocate class -> parameters. null if not set.
     *
     * @return the algorithm specific parameters in a map
     */
    public HashMap<Integer, IdentificationAlgorithmParameter> getAlgorithmSpecificParameters() {
        return algorithmParameters;
    }

    /**
     * Returns the algorithm specific parameters, null if not found.
     *
     * @param algorithmID the index of the search engine as indexed in the
     * Advocate class
     *
     * @return the algorithm specific parameters
     */
    public IdentificationAlgorithmParameter getIdentificationAlgorithmParameter(int algorithmID) {
        if (algorithmParameters == null) {
            return null;
        }
        return algorithmParameters.get(algorithmID);
    }

    /**
     * Adds identification algorithm specific paramters
     *
     * @param algorithmID the algorithm id as indexed in the Advocate class
     *
     * @param identificationAlgorithmParameter the specific parameters
     */
    public void setIdentificationAlgorithmParameter(int algorithmID, IdentificationAlgorithmParameter identificationAlgorithmParameter) {
        if (algorithmParameters == null) {
            algorithmParameters = new HashMap<Integer, IdentificationAlgorithmParameter>();
        }
        algorithmParameters.put(algorithmID, identificationAlgorithmParameter);
    }

    /**
     * Returns the algorithms for which specific parameters are stored. Warning:
     * this does not mean that the algorithm was actually used.
     *
     * @return the algorithms for which specific parameters are stored in a set
     * of indexes as listed in the Advocate class
     */
    public Set<Integer> getAlgorithms() {
        if (algorithmParameters == null) {
            return new HashSet<Integer>();
        }
        return algorithmParameters.keySet();
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
        SearchParameters result = (SearchParameters) SerializationUtils.readObject(file);

        // compatibility check
        ModificationProfile modificationProfile = result.getModificationProfile();
        if (!modificationProfile.hasOMSSAIndexes()) {
            PTMFactory.getInstance().setSearchedOMSSAIndexes(modificationProfile);
        }

        // compatibility check
        if (result.getAlgorithmSpecificParameters() == null) {

            OmssaParameters omssaParameters = new OmssaParameters();
            omssaParameters.setEstimateCharge(result.isEstimateCharge());
            omssaParameters.setHitListLength(result.getHitListLength());
            omssaParameters.setMaxEValue(result.getMaxEValue());
            omssaParameters.setMaxPeptideLength(result.getMaxPeptideLength());
            omssaParameters.setMinPeptideLength(result.getMinPeptideLength());
            omssaParameters.setMinimalChargeForMultipleChargedFragments(result.getMinimalChargeForMultipleChargedFragments());
            omssaParameters.setRemovePrecursor(result.isRemovePrecursor());
            omssaParameters.setScalePrecursor(result.isScalePrecursor());
            result.setIdentificationAlgorithmParameter(omssaParameters.getAlgorithm().getIndex(), omssaParameters);

            XtandemParameters xtandemParameters = new XtandemParameters();
            xtandemParameters.setMaxEValue(result.getMaxEValue());
            result.setIdentificationAlgorithmParameter(xtandemParameters.getAlgorithm().getIndex(), xtandemParameters);

            PepnovoParameters pepnovoParameters = new PepnovoParameters();
            pepnovoParameters.setDiscardLowQualitySpectra(result.getDiscardLowQualitySpectra());
            pepnovoParameters.setEstimateCharge(result.isEstimateCharge());
            pepnovoParameters.setFragmentationModel(result.getFragmentationModel());
            pepnovoParameters.setGenerateQuery(result.generateQuery());
            pepnovoParameters.setHitListLength(result.getHitListLengthDeNovo());
            pepnovoParameters.setPepNovoPtmMap(result.getPepNovoPtmMap());
            result.setIdentificationAlgorithmParameter(pepnovoParameters.getAlgorithm().getIndex(), pepnovoParameters);
        }
        return result;
    }

    /**
     * Saves the identification parameters to a serialized file.
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

    /**
     * Saves the identification parameters as a human readable text file.
     *
     * @param file the file
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void saveIdentificationParametersAsTextFile(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(toString());
        bw.close();
        fw.close();
    }

    public String toString() {
        return toString(false);
    }

    /**
     * Returns the search parameters as a string.
     *
     * @param html use HTML formatting
     * @return the search parameters as a string
     */
    public String toString(boolean html) {

        String newLine = System.getProperty("line.separator");

        if (html) {
            newLine = "<br>";
        }

        StringBuilder output = new StringBuilder();

        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append("# General Identification Parameters");
        output.append(newLine);
        output.append("# ------------------------------------------------------------------");
        output.append(newLine);
        output.append(newLine);

        output.append("DATABASE_FILE=");
        if (fastaFile != null) {
            output.append(fastaFile.getAbsolutePath());
        }
        output.append(newLine);

        output.append("ENZYME=");
        if (enzyme != null) {
            output.append(enzyme.getName());
        }
        output.append(newLine);

        output.append("FIXED_MODIFICATIONS=");
        if (utilitiesModificationProfile != null) {
            ArrayList<String> fixedPtms = utilitiesModificationProfile.getFixedModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("VARIABLE_MODIFICATIONS=");
        if (utilitiesModificationProfile != null) {
            ArrayList<String> fixedPtms = utilitiesModificationProfile.getVariableModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("REFINEMENT_FIXED_MODIFICATIONS=");
        if (utilitiesModificationProfile != null) {
            ArrayList<String> fixedPtms = utilitiesModificationProfile.getRefinementFixedModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("REFINEMENT_VARIABLE_MODIFICATIONS=");
        if (utilitiesModificationProfile != null) {
            ArrayList<String> fixedPtms = utilitiesModificationProfile.getRefinementVariableModifications();
            boolean first = true;
            for (String ptm : fixedPtms) {
                if (first) {
                    output.append(ptm);
                    first = false;
                } else {
                    output.append("//").append(ptm);
                }
            }
        }
        output.append(newLine);

        output.append("MAX_MISSED_CLEAVAGES=");
        output.append(nMissedCleavages);
        output.append(newLine);

        output.append("PRECURSOR_MASS_TOLERANCE=");
        output.append(precursorTolerance);
        output.append(newLine);

        output.append("PRECURSOR_MASS_TOLERANCE_UNIT=");
        if (getPrecursorAccuracyType() == MassAccuracyType.PPM) {
            output.append("ppm");
        } else {
            output.append("Da");
        }
        output.append(newLine);

        output.append("FRAGMENT_MASS_TOLERANCE=");
        output.append(fragmentIonMZTolerance);
        output.append(newLine);

        output.append("FRAGMENT_ION_TYPE_1=");
        if (forwardIon == PeptideFragmentIon.A_ION) {
            output.append("a");
        } else if (forwardIon == PeptideFragmentIon.B_ION) {
            output.append("b");
        } else if (forwardIon == PeptideFragmentIon.C_ION) {
            output.append("c");
        } else if (forwardIon == PeptideFragmentIon.X_ION) {
            output.append("x");
        } else if (forwardIon == PeptideFragmentIon.Y_ION) {
            output.append("y");
        } else if (forwardIon == PeptideFragmentIon.Z_ION) {
            output.append("z");
        }
        output.append(newLine);

        output.append("FRAGMENT_ION_TYPE_2=");
        if (rewindIon == PeptideFragmentIon.A_ION) {
            output.append("a");
        } else if (rewindIon == PeptideFragmentIon.B_ION) {
            output.append("b");
        } else if (rewindIon == PeptideFragmentIon.C_ION) {
            output.append("c");
        } else if (rewindIon == PeptideFragmentIon.X_ION) {
            output.append("x");
        } else if (rewindIon == PeptideFragmentIon.Y_ION) {
            output.append("y");
        } else if (rewindIon == PeptideFragmentIon.Z_ION) {
            output.append("z");
        }
        output.append(newLine);

        output.append("PRECURSOR_CHARGE_LOWER_BOUND=");
        output.append(minChargeSearched);
        output.append(newLine);

        output.append("PRECURSOR_CHARGE_UPPER_BOUND=");
        output.append(maxChargeSearched);
        output.append(newLine);

        for (int index : algorithmParameters.keySet()) {
            output.append(newLine);
            output.append(newLine);
            output.append(algorithmParameters.get(index).toString(html));
        }

        return output.toString();
    }

    /**
     * Returns true of the search parameter objects have identical settings.
     *
     * @param otherSearchParameters the parameters to compare to
     * @return true of the search parameter objects have identical settings
     */
    public boolean equals(SearchParameters otherSearchParameters) {

        if (otherSearchParameters == null) {
            return false;
        }
        if (this.getPrecursorAccuracyType() != otherSearchParameters.getPrecursorAccuracyType()) {
            return false;
        }
        if (this.getPrecursorAccuracy().doubleValue() != otherSearchParameters.getPrecursorAccuracy().doubleValue()) {
            return false;
        }
        if (this.getFragmentIonAccuracy().doubleValue() != otherSearchParameters.getFragmentIonAccuracy().doubleValue()) {
            return false;
        }
        if (this.getnMissedCleavages().intValue() != otherSearchParameters.getnMissedCleavages().intValue()) {
            return false;
        }
        if ((this.getFastaFile() == null && otherSearchParameters.getFastaFile() != null)
                || (this.getFastaFile() != null && otherSearchParameters.getFastaFile() == null)) {
            return false;
        }
        if (this.getFastaFile() != null && otherSearchParameters.getFastaFile() != null) {
            if (!this.getFastaFile().getAbsolutePath().equalsIgnoreCase(otherSearchParameters.getFastaFile().getAbsolutePath())) {
                return false;
            }
        }
        if (this.getIonSearched1().intValue() != otherSearchParameters.getIonSearched1().intValue()) {
            return false;
        }
        if (this.getIonSearched2().intValue() != otherSearchParameters.getIonSearched2().intValue()) {
            return false;
        }
        if (!this.getMinChargeSearched().equals(otherSearchParameters.getMinChargeSearched())) {
            return false;
        }
        if (!this.getMaxChargeSearched().equals(otherSearchParameters.getMaxChargeSearched())) {
            return false;
        }
        if ((this.getEnzyme() != null && otherSearchParameters.getEnzyme() != null)
                && (!this.getEnzyme().equals(otherSearchParameters.getEnzyme()))) {
            return false;
        }
        if ((this.getEnzyme() != null && otherSearchParameters.getEnzyme() == null)
                || (this.getEnzyme() == null && otherSearchParameters.getEnzyme() != null)) {
            return false;
        }
        if (this.getParametersFile() != null && otherSearchParameters.getParametersFile() != null
                && !this.getParametersFile().getAbsolutePath().equalsIgnoreCase(otherSearchParameters.getParametersFile().getAbsolutePath())) {
            return false;
        }
        if ((this.getParametersFile() != null && otherSearchParameters.getParametersFile() == null)
                || (this.getParametersFile() == null && otherSearchParameters.getParametersFile() != null)) {
            return false;
        }
        if (!this.getModificationProfile().equals(otherSearchParameters.getModificationProfile())) {
            return false;
        }
        if (this.getFractionMolecularWeightRanges() != null && otherSearchParameters.getFractionMolecularWeightRanges() != null) {
            if (!this.getFractionMolecularWeightRanges().equals(otherSearchParameters.getFractionMolecularWeightRanges())) {
                return false;
            }
        }
        if ((this.getFractionMolecularWeightRanges() != null && otherSearchParameters.getFractionMolecularWeightRanges() == null)
                || (this.getFractionMolecularWeightRanges() == null && otherSearchParameters.getFractionMolecularWeightRanges() != null)) {
            return false;
        }

        if (this.getAlgorithms().size() != otherSearchParameters.getAlgorithms().size()) {
            return false;
        }

        for (int se : getAlgorithms()) {
            IdentificationAlgorithmParameter otherParameter = otherSearchParameters.getIdentificationAlgorithmParameter(se);
            if (otherParameter == null) {
                return false;
            }
            IdentificationAlgorithmParameter thisParameter = getIdentificationAlgorithmParameter(se);
            if (!otherParameter.equals(thisParameter)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a boolean indicating whether the precursor mass shall be
     * corrected (TagDB setting).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return a boolean indicating whether the precursor mass shall be
     * corrected (TagDB setting)
     */
    public Boolean isCorrectPrecursorMass() {
        if (correctPrecursorMass != null) {
            return correctPrecursorMass;
        } else {
            return true;
        }
    }

    /**
     * Sets whether the precursor mass shall be corrected (TagDB setting).
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param correctPrecursorMass a boolean indicating whether the precursor
     * mass shall be corrected (TagDB setting)
     */
    public void correctPrecursorMass(Boolean correctPrecursorMass) {
        this.correctPrecursorMass = correctPrecursorMass;
    }

    /**
     * Returns a boolean indicating whether low quality spectra shall be
     * discarded.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return a boolean indicating whether low quality spectra shall be
     * discarded
     */
    public Boolean getDiscardLowQualitySpectra() {
        if (discardLowQualitySpectra != null) {
            return discardLowQualitySpectra;
        } else {
            return true;
        }
    }

    /**
     * Sets whether low quality spectra shall be discarded.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param discardLowQualitySpectra a boolean indicating whether low quality
     * spectra shall be discarded
     */
    public void setDiscardLowQualitySpectra(Boolean discardLowQualitySpectra) {
        this.discardLowQualitySpectra = discardLowQualitySpectra;
    }

    /**
     * Returns the name of the fragmentation model.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return the name of the fragmentation model
     */
    public String getFragmentationModel() {
        return fragmentationModel;
    }

    /**
     * Sets the name of the fragmentation model.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param fragmentationModel the name of the fragmentation model
     */
    public void setFragmentationModel(String fragmentationModel) {
        this.fragmentationModel = fragmentationModel;
    }

    /**
     * Returns a boolean indicating whether a blast query shall be generated.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return a boolean indicating whether a blast query shall be generated
     */
    public Boolean generateQuery() {
        return generateQuery;
    }

    /**
     * Sets a boolean indicating whether a blast query shall be generated.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param generateQuery a boolean indicating whether a blast query shall be
     * generated
     */
    public void setGenerateQuery(Boolean generateQuery) {
        this.generateQuery = generateQuery;
    }

    /**
     * Returns the PepNovo to utilities PTM map. Null if not set.
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @return the PepNovo to utilities PTM map, null if not set
     */
    public Map<String, String> getPepNovoPtmMap() {
        return pepNovoPtmMap;
    }

    /**
     * Set the PepNovo to utilities PTM map
     *
     * @deprecated use the appropriated IdentificationAlgorithmParameters
     * instead
     * @param pepNovoPtmMap the pepNovoPtmMap to set
     */
    public void setPepNovoPtmMap(Map<String, String> pepNovoPtmMap) {
        this.pepNovoPtmMap = pepNovoPtmMap;
    }
}
