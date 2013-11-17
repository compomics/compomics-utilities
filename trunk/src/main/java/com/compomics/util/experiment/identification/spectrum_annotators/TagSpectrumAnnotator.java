package com.compomics.util.experiment.identification.spectrum_annotators;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.NeutralLoss;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.NeutralLossesMap;
import com.compomics.util.experiment.identification.SpectrumAnnotator;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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
     * Sets a new peptide to match.
     *
     * @param newTag the new peptide
     * @param precursorCharge the new precursor charge
     */
    public void setTag(Tag newTag, int precursorCharge) {
        if (this.tag == null || !this.tag.isSameAs(newTag) || this.precursorCharge != precursorCharge) {
            this.tag = newTag;
            this.precursorCharge = precursorCharge;
            theoreticalFragmentIons = fragmentFactory.getFragmentIons(newTag);
            if (massShift != 0 || massShiftNTerm != 0 || massShiftCTerm != 0) {
                for (Ion ion : theoreticalFragmentIons) {
                    if (ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION) {
                        if (ion.getSubType() == PeptideFragmentIon.A_ION || ion.getSubType() == PeptideFragmentIon.B_ION || ion.getSubType() == PeptideFragmentIon.C_ION) {
                            ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftNTerm);
                        } else if (ion.getSubType() == PeptideFragmentIon.X_ION || ion.getSubType() == PeptideFragmentIon.Y_ION || ion.getSubType() == PeptideFragmentIon.Z_ION) {
                            ion.setTheoreticMass(ion.getTheoreticMass() + massShift + massShiftCTerm);
                        }
                    }
                }
            }
            spectrumAnnotation.clear();
            unmatchedIons.clear();
        }
    }

    /**
     * Returns the possible neutral losses expected by default for a given
     * peptide. /!\ this method will work only if the PTM found in the peptide
     * are in the PTMFactory.
     *
     * @param tag the tag of interest
     * @return the expected possible neutral losses
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws InterruptedException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public static NeutralLossesMap getDefaultLosses(Tag tag) throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

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
                    if (aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.D)
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.E)
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.S)
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.T)) {
                        int index = i + offset;
                        aaMin = Math.min(index, aaMin);
                        aaMax = Math.max(index, aaMax);
                    }
                }
                offset += aminoAcidPattern.length();
            } else {
                offset++;
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
                    if (aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.D)
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.E)
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.S)
                            || aminoAcidPattern.getAminoAcidsAtTarget().contains(AminoAcid.T)) {
                        int index = i + offset;
                        aaMin = Math.min(index, aaMin);
                        aaMax = Math.max(index, aaMax);
                    }
                }
                offset += aminoAcidPattern.length();
            } else {
                offset++;
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
                            ArrayList<Integer> indexes = tag.getPotentialModificationSites(ptm, ProteinMatch.MatchingType.string, Double.NaN);
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
     * @param iontypes The expected ions to look for
     * @param neutralLosses Map of expected neutral losses: neutral loss ->
     * first position in the sequence (first aa is 1). let null if neutral
     * losses should not be considered.
     * @param charges List of expected charges
     * @param precursorCharge the precursor charge
     * @param spectrum The spectrum to match
     * @param tag The tag of interest
     * @param intensityLimit The intensity limit to use
     * @param mzTolerance The m/z tolerance to use
     * @param isPpm a boolean indicating whether the mass tolerance is in ppm or
     * in Da
     * @return an ArrayList of IonMatch containing the ion matches with the
     * given settings
     */
    public ArrayList<IonMatch> getSpectrumAnnotation(HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses,
            ArrayList<Integer> charges, int precursorCharge, MSnSpectrum spectrum, Tag tag, double intensityLimit, double mzTolerance, boolean isPpm) {

        ArrayList<IonMatch> result = new ArrayList<IonMatch>();

        if (spectrum != null) {
            setSpectrum(spectrum, intensityLimit);
        }

        setTag(tag, precursorCharge);
        setMassTolerance(mzTolerance, isPpm);

        ArrayList<Integer> precursorCharges = new ArrayList<Integer>();

        // we have to keep the precursor charges separate from the fragment ion charges
        for (int i = 1; i <= precursorCharge; i++) {
            precursorCharges.add(i);
        }

        if (theoreticalFragmentIons != null) {
            for (Ion fragmentIon : theoreticalFragmentIons) {

                if (iontypes.containsKey(fragmentIon.getType())
                        && iontypes.get(fragmentIon.getType()).contains(fragmentIon.getSubType())
                        && lossesValidated(neutralLosses, fragmentIon)) {

                    ArrayList<Integer> tempCharges;

                    // have to treat precursor charges separetly, as to not increase the max charge for the other ions
                    if (fragmentIon.getType() == Ion.IonType.PRECURSOR_ION) {
                        tempCharges = precursorCharges;
                    } else {
                        tempCharges = charges;
                    }

                    for (int charge : tempCharges) {
                        if (chargeValidated(fragmentIon, charge, precursorCharge)) {
                            String key = IonMatch.getPeakAnnotation(fragmentIon, new Charge(Charge.PLUS, charge));
                            if (!spectrumAnnotation.containsKey(key)
                                    && !unmatchedIons.contains(key)) {
                                matchInSpectrum(fragmentIon, charge);
                            }
                            if (!unmatchedIons.contains(key)) {
                                result.add(spectrumAnnotation.get(key));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public ArrayList<IonMatch> getCurrentAnnotation(MSnSpectrum spectrum, HashMap<Ion.IonType, ArrayList<Integer>> iontypes, NeutralLossesMap neutralLosses, ArrayList<Integer> charges) {
        return getSpectrumAnnotation(iontypes, neutralLosses, charges, precursorCharge, spectrum, tag, intensityLimit, mzTolerance, isPpm);
    }
}
