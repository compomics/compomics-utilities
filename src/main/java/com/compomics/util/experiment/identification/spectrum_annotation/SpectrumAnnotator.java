package com.compomics.util.experiment.identification.spectrum_annotation;

import com.compomics.util.experiment.identification.spectrum_assumptions.TagAssumption;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.biology.ions.IonFactory;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.Ion.IonType;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.impl.TagFragmentIon;
import com.compomics.util.experiment.identification.SpectrumIdentificationAssumption;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.TagSpectrumAnnotator;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.mass_spectrometry.indexes.SpectrumIndex;
import com.compomics.util.gui.interfaces.SpectrumAnnotation;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * The spectrum annotator annotates peaks in a spectrum.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public abstract class SpectrumAnnotator {

    /**
     * Empty default constructor
     */
    public SpectrumAnnotator() {
    }

    /**
     * Enum of the possibilities for ties resolution when multiple peaks can be
     * annotated.
     */
    public enum TiesResolution {
        /**
         * The most intense peak is retained. If two peaks have the same
         * intensity, the one with the most accurate m/z is retained.
         */
        mostIntense("Higest intensity"),
        /**
         * The peak of most accurate m/z is retained. If two peaks have the same
         * error the most intense is retained.
         */
        mostAccurateMz("Most accurate m/z");

        /**
         * The description.
         */
        public final String description;

        /**
         * Constructor.
         *
         * @param description the description
         */
        private TiesResolution(String description) {
            this.description = description;
        }
    }
    /**
     * The precursor charge as deduced by the search engine.
     */
    protected int precursorCharge;
    /**
     * The precursor charges to inspect by default.
     */
    protected ArrayList<Integer> defaultPrecursorCharges;
    /**
     * The theoretic fragment ions.
     */
    protected HashMap<Integer, HashMap<Integer, ArrayList<Ion>>> theoreticalFragmentIons;
    /**
     * The Fragment factory which will generate the fragment ions.
     */
    protected IonFactory fragmentFactory = IonFactory.getInstance();
    /**
     * The file of the currently loaded spectrum.
     */
    private String spectrumFile = "";
    /**
     * The title of the currently loaded spectrum.
     */
    private String spectrumTitle = "";
    /**
     * The intensity limit to use.
     */
    protected double intensityLimit = 0;
    /**
     * Index for the spectrum.
     */
    private SpectrumIndex spectrumIndex;
    /**
     * The m/z tolerance for peak matching.
     */
    protected double mzTolerance;
    /**
     * Boolean indicating whether the tolerance is in ppm (true) or in Dalton
     * (false).
     */
    protected boolean isPpm;
    /**
     * Minimal isotopic correction when matching an ion.
     */
    protected static final boolean subtractIsotope = false;
    /**
     * The minimal isotope correction. By default only the monoisotopic peak is
     * annotated (min=0).
     */
    protected static final Integer minIsotopicCorrection = 0;
    /**
     * The maximal isotope correction. By default only the monoisotopic peak is
     * annotated (max=0).
     */
    protected static final Integer maxIsotopicCorrection = 0;
    /**
     * m/z shift applied to all theoretic peaks.
     */
    protected double massShift = 0;
    /**
     * N-terminal m/z shift applied to all forward ions.
     */
    protected double massShiftNTerm = 0;
    /**
     * C-terminal m/z shift applied to all reverse ions.
     */
    protected double massShiftCTerm = 0;
    /**
     * The methods to use to select the best peak when multiple are possible.
     */
    protected TiesResolution tiesResolution;
    /**
     * If provided, the annotator will only look for the ions included in the
     * specific annotation settings.
     */
    protected SpecificAnnotationParameters specificAnnotationSettings = null;
    /**
     * The cache to use for the ion match keys.
     */
    protected IonMatchKeysCache ionMatchKeysCache = new IonMatchKeysCache();

    /**
     * Translates the list of ion matches into a vector of annotations which can
     * be read by the SpectrumPanel.
     *
     * @param ionMatches list of ion matches
     *
     * @return vector of default spectrum annotations
     */
    public static Vector<SpectrumAnnotation> getSpectrumAnnotation(IonMatch[] ionMatches) {

        return Arrays.stream(ionMatches)
                .map(
                        ionMatch -> new DefaultSpectrumAnnotation(
                                ionMatch.peakMz,
                                ionMatch.getAbsoluteError(minIsotopicCorrection, maxIsotopicCorrection),
                                SpectrumPanel.determineFragmentIonColor(ionMatch.ion, true),
                                ionMatch.getPeakAnnotation()
                        )
                )
                .collect(
                        Collectors.toCollection(
                                Vector::new
                        )
                );

    }

    /**
     * Matches a theoretic ion in the spectrum. Returns an IonMatch containing
     * the ion and the peak. Null if not found.
     *
     * @param spectrumMz the m/z array of the spectrum
     * @param spectrumIntensity the intensity array of the spectrum
     * @param theoreticIon the theoretic ion
     * @param inspectedCharge the expected charge
     *
     * @return the IonMatch between the ion and the peak
     */
    protected IonMatch matchInSpectrum(
            double[] spectrumMz,
            double[] spectrumIntensity,
            Ion theoreticIon,
            Integer inspectedCharge
    ) {

        double fragmentMz = theoreticIon.getTheoreticMz(inspectedCharge);

        // Get the peaks matching the desired m/z
        int[] matchedPeaksIndexes = spectrumIndex.getMatchingPeaks(fragmentMz);

        if (matchedPeaksIndexes.length == 0) {
            return null;
        }

        // Select the most accurate or most intense according to the annotation settings
        IonMatch ionMatch = new IonMatch(
                0.0,
                0.0,
                theoreticIon,
                inspectedCharge
        );
        ionMatch = setBestPeak(
                spectrumMz,
                spectrumIntensity,
                matchedPeaksIndexes,
                ionMatch
        );

        return ionMatch;
    }

    /**
     * Returns the peak to retain of the matched peaks according to the ties
     * resolution setting.
     *
     * @param spectrumMz The m/z array of the spectrum.
     * @param spectrumIntensity The intensity array of the spectrum.
     * @param matchedPeaksIndexes The indexes of the peaks matched.
     * @param ionMatch The ion match with the ion to be matched.
     *
     * @return The ion match with the peak information filled.
     */
    protected IonMatch setBestPeak(
            double[] spectrumMz,
            double[] spectrumIntensity,
            int[] matchedPeaksIndexes,
            IonMatch ionMatch
    ) {

        if (matchedPeaksIndexes.length == 1) {

            int index = matchedPeaksIndexes[0];

            ionMatch.peakMz = spectrumMz[index];
            ionMatch.peakIntensity = spectrumIntensity[index];

            return ionMatch;

        }

        int bestIndex = -1;
        switch (tiesResolution) {
            case mostAccurateMz:

                double bestPeakError = Double.NaN;

                for (int index : matchedPeaksIndexes) {

                    if (Double.isNaN(bestPeakError)) {

                        bestIndex = index;
                        ionMatch.peakMz = spectrumMz[index];
                        bestPeakError = Math.abs(ionMatch.getError(isPpm));

                    } else {

                        ionMatch.peakMz = spectrumMz[index];
                        double peakError = Math.abs(ionMatch.getError(isPpm));

                        if (peakError < bestPeakError) {

                            bestIndex = index;
                            bestPeakError = peakError;

                        } else if (peakError == bestPeakError && spectrumIntensity[index] > spectrumIntensity[bestIndex]) {

                            bestIndex = index;

                        }
                    }
                }

                ionMatch.peakMz = spectrumMz[bestIndex];
                ionMatch.peakIntensity = spectrumIntensity[bestIndex];

                return ionMatch;

            case mostIntense:

                for (int index : matchedPeaksIndexes) {

                    if (bestIndex == -1 || spectrumIntensity[index] > spectrumIntensity[bestIndex]) {

                        bestIndex = index;

                    } else if (spectrumIntensity[index] == spectrumIntensity[bestIndex]) {

                        ionMatch.peakMz = spectrumMz[bestIndex];
                        bestPeakError = Math.abs(ionMatch.getError(isPpm));
                        ionMatch.peakMz = spectrumMz[index];
                        double peakError = Math.abs(ionMatch.getError(isPpm));

                        if (peakError < bestPeakError) {

                            bestIndex = index;

                        }
                    }
                }

                ionMatch.peakMz = spectrumMz[bestIndex];
                ionMatch.peakIntensity = spectrumIntensity[bestIndex];

                return ionMatch;

            default:
                throw new UnsupportedOperationException("Ties resolution method " + tiesResolution + " not implemented.");
        }
    }

    /**
     * Sets a new spectrum to annotate.
     *
     * @param spectrumFile the file of the spectrum to annotate.
     * @param spectrumTitle the title of the spectrum to annotate.
     * @param spectrum the spectrum to inspect
     * @param intensityLimit the minimal intensity to account for
     */
    protected void setSpectrum(
            String spectrumFile,
            String spectrumTitle,
            Spectrum spectrum,
            double intensityLimit
    ) {
        if (spectrumIndex == null || !spectrumTitle.equals(this.spectrumTitle) || !spectrumFile.equals(this.spectrumFile) || this.intensityLimit != intensityLimit) {

            // Save spectrum number and intensity limit
            this.spectrumFile = spectrumFile;
            this.spectrumTitle = spectrumTitle;
            this.intensityLimit = intensityLimit;

            // See whether the index was previously stored
            spectrumIndex = new SpectrumIndex();
            spectrumIndex = (SpectrumIndex) spectrum.getUrParam(spectrumIndex);

            // Create new index if needed
            if (spectrumIndex == null || spectrumIndex.intensityLimit != intensityLimit || spectrumIndex.tolerance != mzTolerance) {

                spectrumIndex = new SpectrumIndex(
                        spectrum.mz,
                        spectrum.intensity,
                        intensityLimit,
                        mzTolerance,
                        isPpm
                );
                spectrum.addUrParam(spectrumIndex);

            }
        }
    }

    /**
     * Sets a new m/z tolerance for peak matching.
     *
     * @param mzTolerance the new m/z tolerance (in m/z, Th)
     * @param isPpm a boolean indicating whether the mass tolerance is in ppm or
     * in Da
     * @param tiesResolution the method used to resolve ties
     */
    protected void setMassTolerance(
            double mzTolerance,
            boolean isPpm,
            TiesResolution tiesResolution
    ) {

        if (mzTolerance != this.mzTolerance || tiesResolution != this.tiesResolution) {

            // Clear previous index
            spectrumIndex = null;

            // Save new values
            this.mzTolerance = mzTolerance;
            this.isPpm = isPpm;
            this.tiesResolution = tiesResolution;
        }
    }

    /**
     * Returns a boolean indicating whether the neutral loss should be accounted
     * for.
     *
     * @param neutralLosses map of expected neutral losses
     * @param neutralLoss the neutral loss of interest
     * @param ion the fragment ion of interest
     *
     * @return boolean indicating whether the neutral loss should be considered
     */
    public boolean isAccounted(
            NeutralLossesMap neutralLosses,
            NeutralLoss neutralLoss,
            Ion ion
    ) {

        if (neutralLosses == null || neutralLosses.isEmpty()) {
            return false;
        }

        for (String neutralLossName : neutralLosses.getAccountedNeutralLosses()) {

            NeutralLoss neutralLossRef = NeutralLoss.getNeutralLoss(neutralLossName);

            if (neutralLoss.isSameAs(neutralLossRef)) {
                switch (ion.getType()) {
                    case PEPTIDE_FRAGMENT_ION:
                        PeptideFragmentIon peptideFragmentIon = ((PeptideFragmentIon) ion);
                        switch (ion.getSubType()) {
                            case PeptideFragmentIon.A_ION:
                            case PeptideFragmentIon.B_ION:
                            case PeptideFragmentIon.C_ION:
                                return neutralLosses.getForwardStart(neutralLossName) <= peptideFragmentIon.getNumber();
                            case PeptideFragmentIon.X_ION:
                            case PeptideFragmentIon.Y_ION:
                            case PeptideFragmentIon.Z_ION:
                                return neutralLosses.getRewindStart(neutralLossName) <= peptideFragmentIon.getNumber();
                            default:
                                throw new UnsupportedOperationException("Fragment ion type " + ion.getSubTypeAsString() + " not implemented in the spectrum annotator.");
                        }
                    case TAG_FRAGMENT_ION:
                        TagFragmentIon tagFragmentIon = ((TagFragmentIon) ion);
                        switch (ion.getSubType()) {
                            case TagFragmentIon.A_ION:
                            case TagFragmentIon.B_ION:
                            case TagFragmentIon.C_ION:
                                return neutralLosses.getForwardStart(neutralLossName) <= tagFragmentIon.getNumber();
                            case TagFragmentIon.X_ION:
                            case TagFragmentIon.Y_ION:
                            case TagFragmentIon.Z_ION:
                                return neutralLosses.getRewindStart(neutralLossName) <= tagFragmentIon.getNumber();
                            default:
                                throw new UnsupportedOperationException("Fragment ion type " + ion.getSubTypeAsString() + " not implemented in the spectrum annotator.");
                        }
                    default:
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a boolean indicating whether the neutral losses of the given
     * fragment ion fit the requirement of the given neutral losses map.
     *
     * @param neutralLosses map of expected neutral losses: neutral loss
     * @param theoreticIon the ion of interest
     *
     * @return a boolean indicating whether the neutral losses of the given
     * fragment ion are fit the requirement of the given neutral losses map
     */
    public boolean lossesValidated(
            NeutralLossesMap neutralLosses,
            Ion theoreticIon
    ) {
        if (theoreticIon.hasNeutralLosses()) {
            for (NeutralLoss neutralLoss : theoreticIon.getNeutralLosses()) {
                if (!isAccounted(neutralLosses, neutralLoss, theoreticIon)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a boolean indicating whether the given charge can be found on the
     * given fragment ion.
     *
     * @param theoreticIon the ion of interest
     * @param charge the candidate charge
     * @param precursorCharge the precursor charge
     *
     * @return a boolean indicating whether the given charge can be found on the
     * given fragment ion
     */
    public boolean chargeValidated(
            Ion theoreticIon,
            int charge,
            int precursorCharge
    ) {
        if (charge == 1) {
            return true;
        }
        switch (theoreticIon.getType()) {
            case IMMONIUM_ION:
            case RELATED_ION: // note: it is possible to implement higher charges but then modify IonMatch.getPeakAnnotation(boolean html) as well to see the charge displayed on the spectrum
                return false;
            case REPORTER_ION: // note: it is possible to implement higher charges but then modify IonMatch.getPeakAnnotation(boolean html) as well to see the charge displayed on the spectrum
                return false;
            case PEPTIDE_FRAGMENT_ION:
                PeptideFragmentIon peptideFragmentIon = ((PeptideFragmentIon) theoreticIon);
                return charge <= peptideFragmentIon.getNumber() && charge < precursorCharge;
            case TAG_FRAGMENT_ION:
                TagFragmentIon tagFragmentIon = ((TagFragmentIon) theoreticIon);
                return charge <= tagFragmentIon.getNumber() && charge < precursorCharge;
            case PRECURSOR_ION:
                return charge >= precursorCharge;
            default:
                throw new UnsupportedOperationException("Ion type " + theoreticIon.getTypeAsString() + " not implemented in the spectrum annotator.");
        }
    }

    /**
     * Returns the currently matched ions with the given settings using the
     * intensity filter.
     *
     * @param spectrum the spectrum of interest
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     *
     * @return the currently matched ions with the given settings
     */
    public IonMatch[] getCurrentAnnotation(
            Spectrum spectrum,
            AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationsSequenceMatchingParameters
    ) {
        return getCurrentAnnotation(
                spectrum,
                annotationSettings,
                specificAnnotationSettings,
                modificationParameters,
                sequenceProvider,
                modificationsSequenceMatchingParameters,
                true
        );
    }

    /**
     * Returns the currently matched ions with the given settings.
     *
     * @param spectrum the spectrum of interest
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param useIntensityFilter boolean indicating whether intensity filters
     * should be used
     *
     * @return the currently matched ions with the given settings
     */
    public abstract IonMatch[] getCurrentAnnotation(
            Spectrum spectrum,
            AnnotationParameters annotationSettings,
            SpecificAnnotationParameters specificAnnotationSettings,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationsSequenceMatchingParameters,
            boolean useIntensityFilter
    );

    /**
     * Returns the file of the spectrum currently inspected.
     *
     * @return the file of the spectrum currently inspected
     */
    public String getCurrentlyLoadedSpectrumFile() {
        return spectrumFile;
    }

    /**
     * Returns the title of the spectrum currently inspected.
     *
     * @return the title of the spectrum currently inspected
     */
    public String getCurrentlyLoadedSpectrumTitle() {
        return spectrumTitle;
    }

    /**
     * Returns the m/z shift applied to the fragment ions.
     *
     * @return the m/z shift applied to the fragment ions
     */
    public double getMassShift() {
        return massShift;
    }

    /**
     * Returns the N-terminal m/z shift applied to all forward ions.
     *
     * @return the N-terminal m/z shift applied to all forward ions
     */
    public double getMassShiftNTerm() {
        return massShiftNTerm;
    }

    /**
     * Returns the C-terminal m/z shift applied to all reverse ions.
     *
     * @return the C-terminal m/z shift applied to all reverse ions
     */
    public double getMassShiftCTerm() {
        return massShiftNTerm;
    }

    /**
     * Sets an m/z shift on all ions. The previous mass main shift will be
     * removed.
     *
     * @param massShift the m/z shift to apply
     */
    public void setMassShift(
            double massShift
    ) {
        this.massShift = massShift;
        updateMassShifts();
    }

    /**
     * Sets the m/z shifts. The previous mass shifts will be removed.
     *
     * @param massShift the m/z shift to apply
     * @param massShiftNTerm the n-terminal mass shift to apply to all forward
     * ions
     * @param massShiftCTerm the c-terminal mass shift to apply to all reverse
     * ions
     */
    public void setMassShifts(
            double massShift,
            double massShiftNTerm,
            double massShiftCTerm
    ) {
        this.massShift = massShift;
        this.massShiftNTerm = massShiftNTerm;
        this.massShiftCTerm = massShiftCTerm;
        updateMassShifts();
    }

    /**
     * Sets the terminal m/z shifts.
     *
     * @param massShiftNTerm the n-terminal mass shift to apply to all forward
     * ions
     * @param massShiftCTerm the c-terminal mass shift to apply to all reverse
     * ions
     */
    public void setTerminalMassShifts(
            double massShiftNTerm,
            double massShiftCTerm
    ) {
        this.massShiftNTerm = massShiftNTerm;
        this.massShiftCTerm = massShiftCTerm;
        updateMassShifts();
    }

    /**
     * Updates the mass shifts.
     */
    protected void updateMassShifts() {

        if (theoreticalFragmentIons != null) {

            HashMap<Integer, ArrayList<Ion>> peptideFragmentIons = theoreticalFragmentIons.get(IonType.PEPTIDE_FRAGMENT_ION.index);
            ArrayList<Ion> ions = peptideFragmentIons.get(PeptideFragmentIon.A_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);

                }
            }

            ions = peptideFragmentIons.get(PeptideFragmentIon.B_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);

                }
            }

            ions = peptideFragmentIons.get(PeptideFragmentIon.C_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);

                }
            }

            ions = peptideFragmentIons.get(PeptideFragmentIon.X_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);

                }
            }

            ions = peptideFragmentIons.get(PeptideFragmentIon.Y_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);

                }
            }

            ions = peptideFragmentIons.get(PeptideFragmentIon.Z_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);

                }
            }

            HashMap<Integer, ArrayList<Ion>> tagFragmentIons = theoreticalFragmentIons.get(IonType.TAG_FRAGMENT_ION.index);
            ions = tagFragmentIons.get(TagFragmentIon.A_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);

                }
            }

            ions = tagFragmentIons.get(TagFragmentIon.B_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);

                }
            }

            ions = tagFragmentIons.get(TagFragmentIon.C_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);

                }
            }

            ions = tagFragmentIons.get(TagFragmentIon.X_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);

                }
            }

            ions = tagFragmentIons.get(TagFragmentIon.Y_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);

                }
            }

            ions = tagFragmentIons.get(TagFragmentIon.Z_ION);

            if (ions != null) {

                for (Ion ion : ions) {

                    ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);

                }
            }
        }
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide. /!\ this method will work only if the PTM found in the peptide
     * are in the PTMFactory.
     *
     * @param spectrumIdentificationAssumption the
     * spectrumIdentificationAssumption of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param sequenceProvider a protein sequence provider
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param spectrumAnnotator the spectrum annotator
     *
     * @return the expected possible neutral losses
     */
    public static NeutralLossesMap getDefaultLosses(
            SpectrumIdentificationAssumption spectrumIdentificationAssumption,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters modificationsSequenceMatchingParameters,
            SpectrumAnnotator spectrumAnnotator
    ) {

        if (spectrumIdentificationAssumption instanceof PeptideAssumption) {

            PeptideAssumption peptideAssumption = (PeptideAssumption) spectrumIdentificationAssumption;
            return ((PeptideSpectrumAnnotator) spectrumAnnotator).getDefaultLosses(peptideAssumption.getPeptide(), modificationParameters, sequenceProvider, modificationsSequenceMatchingParameters);

        } else if (spectrumIdentificationAssumption instanceof TagAssumption) {

            TagAssumption tagAssumption = (TagAssumption) spectrumIdentificationAssumption;
            return ((TagSpectrumAnnotator) spectrumAnnotator).getDefaultLosses(tagAssumption.getTag(), modificationParameters, modificationsSequenceMatchingParameters);

        } else {

            throw new IllegalArgumentException("Default neutral loss map not implemented for SpectrumIdentificationAssumption " + spectrumIdentificationAssumption.getClass() + ".");

        }
    }

    /**
     * This method matches the potential fragment ions of a given peptide with a
     * given peak. Note: fragment ions need to be initiated by the
     * SpectrumAnnotator extending class.
     *
     * @param specificAnnotationSettings The specific annotation settings.
     * @param peakMz The m/z of the peak to match.
     * @param peakIntensity The intensity of the peak to match.
     *
     * @return A list of potential ion matches
     */
    protected ArrayList<IonMatch> matchPeak(
            SpecificAnnotationParameters specificAnnotationSettings,
            double peakMz,
            double peakIntensity
    ) {

        ArrayList<IonMatch> result = new ArrayList<>();

        HashMap<Ion.IonType, HashSet<Integer>> ionTypes = specificAnnotationSettings.getIonTypes();

        for (Ion.IonType ionType : ionTypes.keySet()) {

            HashMap<Integer, ArrayList<Ion>> ionMap = theoreticalFragmentIons.get(ionType.index);

            if (ionMap != null) {

                HashSet<Integer> subtypes = ionTypes.get(ionType);

                for (int subType : subtypes) {

                    ArrayList<Ion> ions = ionMap.get(subType);

                    if (ions != null) {

                        for (Ion ion : ions) {

                            for (int charge : specificAnnotationSettings.getSelectedCharges()) {

                                if (chargeValidated(ion, charge, specificAnnotationSettings.getPrecursorCharge())
                                        && lossesValidated(specificAnnotationSettings.getNeutralLossesMap(), ion)) {

                                    IonMatch ionMatch = new IonMatch(
                                            peakMz,
                                            peakIntensity,
                                            ion,
                                            charge
                                    );

                                    double absError = Math.abs(
                                            ionMatch.getError(
                                                    specificAnnotationSettings.isFragmentIonPpm(),
                                                    minIsotopicCorrection,
                                                    maxIsotopicCorrection
                                            )
                                    );

                                    if (absError <= specificAnnotationSettings.getFragmentIonAccuracy()) {

                                        result.add(ionMatch);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the expected ions in a map indexed by the possible charges.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * Note: fragment ions need to be initiated by the SpectrumAnnotator
     * extending class.
     *
     * @param specificAnnotationSettings the specific annotation settings
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    protected HashMap<Integer, ArrayList<Ion>> getExpectedIons(
            SpecificAnnotationParameters specificAnnotationSettings
    ) {

        HashMap<Integer, ArrayList<Ion>> result = new HashMap<>();

        HashMap<Ion.IonType, HashSet<Integer>> ionTypes = specificAnnotationSettings.getIonTypes();
        
        for (Ion.IonType ionType : ionTypes.keySet()) {

            HashMap<Integer, ArrayList<Ion>> ionMap = theoreticalFragmentIons.get(ionType.index);

            if (ionMap != null) {

                HashSet<Integer> subtypes = ionTypes.get(ionType);

                for (int subType : subtypes) {

                    ArrayList<Ion> ions = ionMap.get(subType);

                    if (ions != null) {

                        for (Ion ion : ions) {

                            if (lossesValidated(specificAnnotationSettings.getNeutralLossesMap(), ion)) {

                                for (int charge : specificAnnotationSettings.getSelectedCharges()) {

                                    if (chargeValidated(ion, charge, precursorCharge)) {

                                        ArrayList<Ion> resultsAtCharge = result.get(charge);

                                        if (resultsAtCharge == null) {

                                            resultsAtCharge = new ArrayList<>(1);
                                            result.put(charge, resultsAtCharge);

                                        }

                                        resultsAtCharge.add(ion);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Convenience method to match a reporter ion in a spectrum. The charge is
     * assumed to be 1.
     *
     * @param theoreticIon the theoretic ion to look for
     * @param charge the charge of the ion
     * @param spectrum the spectrum
     * @param massTolerance the mass tolerance to use
     *
     * @return a list of all the ion matches
     */
    public static ArrayList<IonMatch> matchReporterIon(
            Ion theoreticIon, 
            int charge, 
            Spectrum spectrum, 
            double massTolerance
    ) {
    
        ArrayList<IonMatch> result = new ArrayList<>(1);
        
        double targetMass = theoreticIon.getTheoreticMz(charge);
        
        for (int i = 0 ; i < spectrum.getNPeaks() ; i++) {
            
            double mz = spectrum.mz[i];
            double intensity = spectrum.intensity[i];
            
            if (Math.abs(mz - targetMass) <= massTolerance) {
                
                result.add(
                        new IonMatch(
                                mz,
                                intensity, 
                                theoreticIon, 
                                charge
                        )
                );
            }
            
            if (mz > targetMass + massTolerance) {
                break;
            }
            
        }
        return result;
    }
}
