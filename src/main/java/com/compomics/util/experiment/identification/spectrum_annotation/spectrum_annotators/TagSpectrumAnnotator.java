package com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.amino_acid_tags.MassGap;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.MathException;

/**
 * Annotates a spectrum with information from a tag.
 *
 * @author Marc Vaudel
 */
public class TagSpectrumAnnotator extends SpectrumAnnotator {

    /**
     * The tag to annotate on the spectrum.
     */
    private Tag tag;

    /**
     * Returns the tag to annotate.
     *
     * @return the tag to annotate
     */
    public Tag getTag() {
        return tag;
    }

    /**
     * Sets a new tag to match.
     *
     * @param newTag the new tag
     * @param precursorCharge the new precursor charge
     */
    public void setTag(Tag newTag, int precursorCharge) {
        if (this.tag == null || !this.tag.isSameAs(newTag, SequenceMatchingParameters.defaultStringMatching) || this.precursorCharge != precursorCharge) {

            // Set new values
            this.tag = newTag;
            this.precursorCharge = precursorCharge;
            theoreticalFragmentIons = fragmentFactory.getFragmentIons(newTag);
            if (massShift != 0 || massShiftNTerm != 0 || massShiftCTerm != 0) {
                updateMassShifts();
            }
        }
    }

    /**
     * Returns the possible neutral losses expected by default for a given tag.
     * /!\ this method will work only if the PTM found in the tag are in the
     * PTMFactory.
     *
     * @param tag the tag of interest
     * @param ptmSequenceMatchingSettings the sequence matching settings for PTM
     * to peptide mapping
     *
     * @return the expected possible neutral losses
     */
    public static NeutralLossesMap getDefaultLosses(Tag tag, SequenceMatchingParameters ptmSequenceMatchingSettings) {

        ModificationFactory pTMFactory = ModificationFactory.getInstance();
        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        int tagLength = tag.getLengthInAminoAcid();
        int aaMin = tagLength;
        int aaMax = 0;

        int offset = 0;
        for (TagComponent component : tag.getContent()) {
            if (component instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) component;
                for (int i = 0; i < aminoAcidPattern.length(); i++) {
                    if (aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.D.getSingleLetterCodeAsChar())
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.E.getSingleLetterCodeAsChar())
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.S.getSingleLetterCodeAsChar())
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.T.getSingleLetterCodeAsChar())) {
                        int index = i + offset;
                        aaMin = Math.min(index, aaMin);
                        aaMax = Math.max(index, aaMax);
                    }
                }
                offset += aminoAcidPattern.length();
            } else if (component instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) component;
                for (int i = 0; i < aminoAcidSequence.length(); i++) {
                    if (aminoAcidSequence.charAt(i) == 'D'
                            || aminoAcidSequence.charAt(i) == 'E'
                            || aminoAcidSequence.charAt(i) == 'S'
                            || aminoAcidSequence.charAt(i) == 'T') {
                        int index = i + offset;
                        aaMin = Math.min(index, aaMin);
                        aaMax = Math.max(index, aaMax);
                    }
                }
                offset += aminoAcidSequence.length();
            } else if (component instanceof MassGap) {
                offset++;
            } else {
                throw new UnsupportedOperationException("Spectrum annotator not implemented for " + component.getClass() + ".");
            }
        }
        if (aaMin < tagLength) {
            neutralLossesMap.addNeutralLoss(NeutralLoss.H2O, aaMin + 1, tagLength - aaMax);
        }

        aaMin = tagLength;
        aaMax = 0;

        offset = 0;
        for (TagComponent component : tag.getContent()) {
            if (component instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) component;
                for (int i = 0; i < aminoAcidPattern.length(); i++) {
                    if (aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.K.getSingleLetterCodeAsChar())
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.N.getSingleLetterCodeAsChar())
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.Q.getSingleLetterCodeAsChar())
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.R.getSingleLetterCodeAsChar())) {
                        int index = i + offset;
                        aaMin = Math.min(index, aaMin);
                        aaMax = Math.max(index, aaMax);
                    }
                }
                offset += aminoAcidPattern.length();
            } else if (component instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) component;
                for (int i = 0; i < aminoAcidSequence.length(); i++) {
                    if (aminoAcidSequence.charAt(i) == 'K'
                            || aminoAcidSequence.charAt(i) == 'N'
                            || aminoAcidSequence.charAt(i) == 'Q'
                            || aminoAcidSequence.charAt(i) == 'R') {
                        int index = i + offset;
                        aaMin = Math.min(index, aaMin);
                        aaMax = Math.max(index, aaMax);
                    }
                }
                offset += aminoAcidSequence.length();
            } else if (component instanceof MassGap) {
                offset++;
            } else {
                throw new UnsupportedOperationException("Spectrum annotator not implemented for " + component.getClass() + ".");
            }
        }
        if (aaMin < tagLength) {
            neutralLossesMap.addNeutralLoss(NeutralLoss.NH3, aaMin + 1, tagLength - aaMax);
        }

        int modMin = tagLength;
        int modMax = 0;

        offset = 0;
        for (TagComponent component : tag.getContent()) {
            if (component instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) component;
                for (int i = 1; i <= aminoAcidPattern.length(); i++) {
                    for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i)) {
                        Modification ptm = pTMFactory.getModification(modificationMatch.getModification());
                        if (ptm == null) {
                            throw new IllegalArgumentException("PTM " + modificationMatch.getModification() + " not loaded in PTM factory.");
                        }
                        for (NeutralLoss neutralLoss : ptm.getNeutralLosses()) {
                            ArrayList<Integer> indexes = tag.getPotentialModificationSites(ptm, ptmSequenceMatchingSettings); // @TODO: could end in a null pointer?
                            if (!indexes.isEmpty()) {
                                Collections.sort(indexes);
                                modMin = indexes.get(0);
                                modMax = indexes.get(indexes.size() - 1);
                            }
                            neutralLossesMap.addNeutralLoss(neutralLoss, modMin, tag.getLengthInAminoAcid() - modMax + 1);
                        }
                    }
                }
                offset += aminoAcidPattern.length();
            } else {
                offset++;
            }
        }

        return neutralLossesMap;
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches using an intensity filter.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param tag the tag of interest
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public ArrayList<IonMatch> getSpectrumAnnotation(AnnotationParameters annotationSettings, SpecificAnnotationParameters specificAnnotationSettings, 
            Spectrum spectrum, Tag tag) {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, tag, true);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param tag the tag of interest
     * @param useIntensityFilter boolean indicating whether intensity filters should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public ArrayList<IonMatch> getSpectrumAnnotation(AnnotationParameters annotationSettings, SpecificAnnotationParameters specificAnnotationSettings, 
            Spectrum spectrum, Tag tag, boolean useIntensityFilter) {

        ArrayList<IonMatch> result = new ArrayList<>();

        setMassTolerance(specificAnnotationSettings.getFragmentIonAccuracy(), specificAnnotationSettings.isFragmentIonPpm(), annotationSettings.getTiesResolution());
        if (spectrum != null) {
            double intensityLimit = useIntensityFilter ? spectrum.getIntensityLimit(annotationSettings.getIntensityThresholdType(), annotationSettings.getAnnotationIntensityLimit()) : 0.0;
            setSpectrum(spectrum, intensityLimit);
        }
        setTag(tag, specificAnnotationSettings.getPrecursorCharge());

        ArrayList<Integer> precursorCharges = new ArrayList<>();

        // we have to keep the precursor charges separate from the fragment ion charges
        for (int i = 1; i <= precursorCharge; i++) {
            precursorCharges.add(i);
        }

        HashMap<Ion.IonType, HashSet<Integer>> ionTypes = specificAnnotationSettings.getIonTypes();
        if (theoreticalFragmentIons != null) {
            for (Ion.IonType ionType : ionTypes.keySet()) {
                HashMap<Integer, ArrayList<Ion>> ionMap = theoreticalFragmentIons.get(ionType.index);
                if (ionMap != null) {
                    HashSet<Integer> subtypes = ionTypes.get(ionType);
                    for (int subType : subtypes) {
                        ArrayList<Ion> ions = ionMap.get(subType);
                        if (ions != null) {
                            for (Ion ion : ions) {

                                if (lossesValidated(specificAnnotationSettings.getNeutralLossesMap(), ion)) {

                                    ArrayList<Integer> tempCharges;
                                    // have to treat precursor charges separately, as to not increase the max charge for the other ions
                                    if (ionType == Ion.IonType.PRECURSOR_ION) {
                                        tempCharges = precursorCharges;
                                    } else {
                                        tempCharges = specificAnnotationSettings.getSelectedCharges();
                                    }

                                    for (int charge : tempCharges) {
                                        if (chargeValidated(ion, charge, precursorCharge)) {
                                            IonMatch ionMatch = matchInSpectrum(ion, charge);
                                            if (ionMatch != null) {
                                                result.add(ionMatch);
                                            }
                                        }
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

    @Override
    public ArrayList<IonMatch> getCurrentAnnotation(Spectrum spectrum, AnnotationParameters annotationSettings, SpecificAnnotationParameters specificAnnotationSettings, boolean useIntensityFilter) {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, tag, useIntensityFilter);
    }
}
