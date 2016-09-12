package com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

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
        if (this.tag == null || !this.tag.isSameAs(newTag, SequenceMatchingPreferences.defaultStringMatching) || this.precursorCharge != precursorCharge) {
            
            // Clear caches
            spectrumAnnotation.clear();
            unmatchedIons.clear();
            
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
     *
     * @throws IOException exception thrown whenever an error occurred while
     * interacting with a file while mapping potential modification sites
     * @throws InterruptedException exception thrown whenever a threading issue
     * occurred while mapping potential modification sites
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing an object from the ProteinTree
     */
    public static NeutralLossesMap getDefaultLosses(Tag tag, SequenceMatchingPreferences ptmSequenceMatchingSettings)
            throws IOException, InterruptedException, ClassNotFoundException {

        PTMFactory pTMFactory = PTMFactory.getInstance();
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
                        PTM ptm = pTMFactory.getPTM(modificationMatch.getTheoreticPtm());
                        if (ptm == null) {
                            throw new IllegalArgumentException("PTM " + modificationMatch.getTheoreticPtm() + " not loaded in PTM factory.");
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
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
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
    public ArrayList<IonMatch> getSpectrumAnnotation(AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings, MSnSpectrum spectrum, Tag tag) {

        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        setMassTolerance(specificAnnotationSettings.getFragmentIonAccuracy(), specificAnnotationSettings.isFragmentIonPpm(), annotationSettings.isHighResolutionAnnotation());
        if (spectrum != null) {
            setSpectrum(spectrum, spectrum.getIntensityLimit(annotationSettings.getAnnotationIntensityLimit()));
        }
        setTag(tag, specificAnnotationSettings.getPrecursorCharge());

        ArrayList<Integer> precursorCharges = new ArrayList<Integer>();

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
                                            String key = IonMatch.getMatchKey(ion, charge, ionMatchKeysCache);
                                            boolean matchFound = false;
                                            boolean alreadyAnnotated = spectrumAnnotation.containsKey(key);
                                            if (!alreadyAnnotated && !unmatchedIons.contains(key)) {
                                                matchFound = matchInSpectrum(ion, charge);
                                            }
                                            if (alreadyAnnotated || matchFound) {
                                                result.add(spectrumAnnotation.get(key));
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
    public ArrayList<IonMatch> getCurrentAnnotation(MSnSpectrum spectrum, AnnotationSettings annotationSettings, SpecificAnnotationSettings specificAnnotationSettings) {
        return getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, tag);
    }
}
