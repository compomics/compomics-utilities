package com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators;

import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
import com.compomics.util.experiment.identification.spectrum_annotation.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.amino_acid_tags.MassGap;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Annotates a spectrum with information from a tag.
 *
 * @author Marc Vaudel
 */
public class TagSpectrumAnnotator extends SpectrumAnnotator {

    /**
     * Empty default constructor
     */
    public TagSpectrumAnnotator() {
    }

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
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param precursorCharge the new precursor charge
     */
    public void setTag(Tag newTag, ModificationParameters modificationParameters, SequenceMatchingParameters modificationsSequenceMatchingParameters, int precursorCharge) {
        if (this.tag == null || !this.tag.isSameAs(newTag, SequenceMatchingParameters.defaultStringMatching) || this.precursorCharge != precursorCharge) {

            // Set new values
            this.tag = newTag;
            this.precursorCharge = precursorCharge;
            theoreticalFragmentIons = fragmentFactory.getFragmentIons(newTag, modificationParameters, modificationsSequenceMatchingParameters);
            if (massShift != 0 || massShiftNTerm != 0 || massShiftCTerm != 0) {
                updateMassShifts();
            }
        }
    }

    /**
     * Returns the possible neutral losses expected by default for a given tag.
     * /!\ this method will work only if the modification found in the tag are in the
     * factory.
     *
     * @param tag the tag of interest
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     *
     * @return the expected possible neutral losses
     */
    public static NeutralLossesMap getDefaultLosses(Tag tag, ModificationParameters modificationParameters, SequenceMatchingParameters modificationsSequenceMatchingParameters) {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        NeutralLossesMap neutralLossesMap = new NeutralLossesMap();

        int tagLength = tag.getLengthInAminoAcid();
        int aaMin = tagLength;
        int aaMax = 0;

        int offset = 0;
        
        for (TagComponent component : tag.getContent()) {
            
            if (component instanceof AminoAcidSequence) {
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
            
            if (component instanceof AminoAcidSequence) {
            
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

        ArrayList<TagComponent> tagComponents = tag.getContent();
        offset = 0;
        
        for (int i = 0 ; i < tagComponents.size() ; i++) {
            
            TagComponent component = tagComponents.get(i);
        
            if (component instanceof AminoAcidSequence) {
                
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) component;
                String[] fixedModifications = aminoAcidSequence.getFixedModifications(i == 0, i == tagComponents.size() - 1, modificationParameters, modificationsSequenceMatchingParameters);
                String[] variableModifications = aminoAcidSequence.getIndexedVariableModifications();
            
                for (int j = 0; j <= aminoAcidSequence.length() + 1; j++) {
                    
                    String fixedModification = fixedModifications[j];
                    
                    if (fixedModification != null) {
                        
                        Modification modification = modificationFactory.getModification(fixedModification);
                        int site = ModificationUtils.getSite(j, aminoAcidSequence.length());
                        
                        for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {
                            
                            neutralLossesMap.addNeutralLoss(neutralLoss, site, tag.getLengthInAminoAcid() - site + 1);
                        
                        }
                    }
                    
                    String variableModification = variableModifications[j];
                    
                    if (variableModification != null) {
                        
                        Modification modification = modificationFactory.getModification(variableModification);
                        int site = ModificationUtils.getSite(j, aminoAcidSequence.length());
                        
                        for (NeutralLoss neutralLoss : modification.getNeutralLosses()) {
                            
                            neutralLossesMap.addNeutralLoss(neutralLoss, site, tag.getLengthInAminoAcid() - site + 1);
                        
                        }
                    }
                }
                
                offset += aminoAcidSequence.length();
            
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
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param tag the tag of interest
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public IonMatch[] getSpectrumAnnotation(AnnotationParameters annotationSettings, ModificationParameters modificationParameters, SequenceMatchingParameters modificationsSequenceMatchingParameters, SpecificAnnotationParameters specificAnnotationSettings, 
            Spectrum spectrum, Tag tag) {
        return getSpectrumAnnotation(annotationSettings, modificationParameters, modificationsSequenceMatchingParameters, specificAnnotationSettings, spectrum, tag, true);
    }

    /**
     * Returns the spectrum annotations of a spectrum in a list of IonMatches.
     *
     * Note that, except for +1 precursors, fragments ions will be expected to
     * have a charge strictly smaller than the precursor ion charge.
     *
     * @param annotationSettings the annotation settings
     * @param modificationParameters the modification parameters the
     * modification parameters
     * @param modificationsSequenceMatchingParameters the sequence matching
     * parameters to use for modifications
     * @param specificAnnotationSettings the specific annotation settings
     * @param spectrum the spectrum to match
     * @param tag the tag of interest
     * @param useIntensityFilter boolean indicating whether intensity filters should be used
     *
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public IonMatch[] getSpectrumAnnotation(AnnotationParameters annotationSettings, ModificationParameters modificationParameters, SequenceMatchingParameters modificationsSequenceMatchingParameters, SpecificAnnotationParameters specificAnnotationSettings, 
            Spectrum spectrum, Tag tag, boolean useIntensityFilter) {

        ArrayList<IonMatch> annotationList = new ArrayList<>(0);

        setMassTolerance(specificAnnotationSettings.getFragmentIonAccuracy(), specificAnnotationSettings.isFragmentIonPpm(), annotationSettings.getTiesResolution());
        if (spectrum != null) {
            double intensityLimit = useIntensityFilter ? spectrum.getIntensityLimit(annotationSettings.getIntensityThresholdType(), annotationSettings.getAnnotationIntensityLimit()) : 0.0;
            setSpectrum(spectrum, intensityLimit);
        }
        setTag(tag, modificationParameters, modificationsSequenceMatchingParameters, specificAnnotationSettings.getPrecursorCharge());

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
                                                annotationList.add(ionMatch);
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

        return annotationList.toArray(new IonMatch[annotationList.size()]);
    }

    @Override
    public IonMatch[] getCurrentAnnotation(Spectrum spectrum, AnnotationParameters annotationSettings, SpecificAnnotationParameters specificAnnotationSettings, 
            ModificationParameters modificationParameters, SequenceProvider sequenceProvider, SequenceMatchingParameters modificationsSequenceMatchingParameters, boolean useIntensityFilter) {
        return getSpectrumAnnotation(annotationSettings, modificationParameters, modificationsSequenceMatchingParameters, specificAnnotationSettings, spectrum, tag, useIntensityFilter);
    }
}
