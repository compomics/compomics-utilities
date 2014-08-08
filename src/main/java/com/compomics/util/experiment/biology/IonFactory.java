package com.compomics.util.experiment.biology;

import com.compomics.util.experiment.biology.ions.ImmoniumIon;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.PrecursorIon;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.biology.ions.TagFragmentIon;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.tags.Tag;
import com.compomics.util.experiment.identification.tags.TagComponent;
import com.compomics.util.experiment.identification.tags.tagcomponents.MassGap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * This factory generates the expected ions from a peptide.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class IonFactory {

    /**
     * The instance of the factory.
     */
    private static IonFactory instance = null;
    /**
     * Neutral losses which will be looked for for every peptide independently
     * from the modifications found.
     */
    private static ArrayList<NeutralLoss> defaultNeutralLosses = new ArrayList<NeutralLoss>();

    /**
     * Constructor.
     */
    private IonFactory() {
    }

    /**
     * Static method which returns the instance of the factory.
     *
     * @return the instance of the factory
     */
    public static IonFactory getInstance() {
        if (instance == null) {
            instance = new IonFactory();
        }
        return instance;
    }

    /**
     * Adds a default neutral loss to the default neutral losses if the
     * corresponding loss was not here already.
     *
     * @param newNeutralLoss the new neutral loss
     */
    public void addDefaultNeutralLoss(NeutralLoss newNeutralLoss) {
        boolean found = false;
        for (NeutralLoss neutralLoss : defaultNeutralLosses) {
            if (newNeutralLoss.isSameAs(neutralLoss)) {
                found = true;
                break;
            }
        }
        if (!found) {
            defaultNeutralLosses.add(newNeutralLoss);
        }
    }

    /**
     * Returns the default neutral losses.
     *
     * @return the default neutral losses
     */
    public ArrayList<NeutralLoss> getDefaultNeutralLosses() {
        return defaultNeutralLosses;
    }

    /**
     * This method returns the theoretic ions expected from a peptide. /!\ this
     * method will work only if the PMTs found in the peptide are in the
     * PTMFactory.
     *
     * @param peptide The considered peptide
     * @return the expected fragment ions
     */
    public ArrayList<Ion> getFragmentIons(Peptide peptide) {

        ArrayList<Ion> result = new ArrayList<Ion>();
        String sequence = peptide.getSequence().toUpperCase();
        HashMap<Integer, ArrayList<PTM>> modifications = new HashMap<Integer, ArrayList<PTM>>();
        PTMFactory ptmFactory = PTMFactory.getInstance();
        ArrayList<String> taken = new ArrayList<String>();
        ArrayList<ReporterIon> reporterIons = new ArrayList<ReporterIon>();
        ArrayList<NeutralLoss> possibleNeutralLosses = new ArrayList<NeutralLoss>();
        possibleNeutralLosses.addAll(defaultNeutralLosses);

        for (ModificationMatch ptmMatch : peptide.getModificationMatches()) {
            int location = ptmMatch.getModificationSite();
            String ptmName = ptmMatch.getTheoreticPtm();
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (ptm == null) {
                throw new IllegalArgumentException("PTM " + ptmName + " not loaded in the PTM factory.");
            }
            if (!modifications.containsKey(location)) {
                modifications.put(location, new ArrayList<PTM>());
            }
            modifications.get(location).add(ptm);
            if (!taken.contains(ptmName)) {
                for (ReporterIon ptmReporterIon : ptm.getReporterIons()) {
                    boolean found = false;
                    for (ReporterIon reporterIon : reporterIons) {
                        if (ptmReporterIon.isSameAs(reporterIon)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        reporterIons.add(ptmReporterIon);
                    }
                }
                for (NeutralLoss ptmNeutralLoss : ptm.getNeutralLosses()) {
                    boolean found = false;
                    for (NeutralLoss neutralLoss : possibleNeutralLosses) {
                        // @TODO: we keep only different neutral losses. We might want to change that when people 
                        //       are working with modifications having reproducible motifs like ubiquitin or some glycons.
                        if (ptmNeutralLoss.isSameAs(neutralLoss)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        possibleNeutralLosses.add(ptmNeutralLoss);
                    }
                }
                taken.add(ptmName);
            }
        }

        result.addAll(reporterIons);

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = getAccountedNeutralLosses(possibleNeutralLosses);

        double forwardMass = 0;
        double rewindMass = Atom.O.getMonoisotopicMass();
        taken.clear();

        for (int aa = 0; aa < sequence.length() - 1; aa++) {

            char aaName = sequence.charAt(aa);
            if (!taken.contains(aaName + "")) {
                result.add(new ImmoniumIon(aaName));
                taken.add(aaName + "");
            }

            int faa = aa + 1;
            AminoAcid currentAA = AminoAcid.getAminoAcid(aaName);
            forwardMass += currentAA.monoisotopicMass;

            if (modifications.get(faa) != null) {
                for (PTM ptm : modifications.get(faa)) {
                    forwardMass += ptm.getMass();
                }
            }

            // add the a-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.A_ION, faa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses));
            }

            // add the b-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.B_ION, faa, forwardMass - getLossesMass(losses), losses));
            }

            // add the c-ion
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.C_ION, faa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses));
            }

            int raa = sequence.length() - aa - 1;
            currentAA = AminoAcid.getAminoAcid(sequence.charAt(raa));
            rewindMass += currentAA.monoisotopicMass;

            if (modifications.get(raa + 1) != null) {
                for (PTM ptm : modifications.get(raa + 1)) {
                    rewindMass += ptm.getMass();
                }
            }

            // add the x-ion
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.X_ION, faa, rewindMass + Atom.C.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses));
            }

            // add the y-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.Y_ION, faa, rewindMass + 2 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses));
            }

            // add the z-ions
            for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                result.add(new PeptideFragmentIon(PeptideFragmentIon.Z_ION, faa, rewindMass - Atom.N.getMonoisotopicMass() - getLossesMass(losses), losses));
            }
        }

        AminoAcid currentAA = AminoAcid.getAminoAcid(sequence.charAt(sequence.length() - 1));
        forwardMass += currentAA.monoisotopicMass;

        if (modifications.get(sequence.length()) != null) {
            for (PTM ptm : modifications.get(sequence.length())) {
                forwardMass += ptm.getMass();
            }
        }
        // add the precursor ion
        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
            result.add(new PrecursorIon(forwardMass + Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses));
        }

        return result;
    }

    /**
     * This method returns the theoretic ions expected from a tag.
     *
     * /!\ this method will work only if the PTMs found in the tag are in the
     * PTMFactory.
     *
     * @param tag The considered tag
     * @return the expected fragment ions
     */
    public ArrayList<Ion> getFragmentIons(Tag tag) {

        ArrayList<Ion> result = new ArrayList<Ion>();
        ArrayList<String> taken = new ArrayList<String>();
        ArrayList<ReporterIon> reporterIons = new ArrayList<ReporterIon>();
        ArrayList<NeutralLoss> possibleNeutralLosses = new ArrayList<NeutralLoss>();
        possibleNeutralLosses.addAll(defaultNeutralLosses);

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = getAccountedNeutralLosses(possibleNeutralLosses);

        taken.clear();

        int ionNumberOffset = 1;
        ArrayList<Double> massOffsets = new ArrayList<Double>();
        massOffsets.add(0.0);
        for (TagComponent tagComponent : tag.getContent()) {
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                ArrayList<Double> patternMasses = new ArrayList<Double>();
                for (int i = 0; i < aminoAcidPattern.length(); i++) {
                    ArrayList<Double> aminoAcidMasses = new ArrayList<Double>();
                    for (AminoAcid aminoAcid : aminoAcidPattern.getTargetedAA(i)) {
                        double mass = aminoAcid.monoisotopicMass;
                        for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i + 1)) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
                            mass += ptm.getMass();
                        }
                        if (!aminoAcidMasses.contains(mass)) {
                            aminoAcidMasses.add(mass);
                        }
                    }
                    for (double massOffset : massOffsets) {
                        ArrayList<Double> newPatternMassess = new ArrayList<Double>();
                        if (patternMasses.isEmpty()) {
                            for (double mass : aminoAcidMasses) {
                                int aa = ionNumberOffset + i;
                                int subaa = i + 1;
                                double forwardMass = massOffset + mass;

                                // add the a-ions
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    result.add(new TagFragmentIon(TagFragmentIon.A_ION, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                }

                                // add the b-ions
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    result.add(new TagFragmentIon(TagFragmentIon.B_ION, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                                }

                                // add the c-ion
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    result.add(new TagFragmentIon(TagFragmentIon.C_ION, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                }

                                if (!newPatternMassess.contains(mass)) {
                                    newPatternMassess.add(mass);
                                }
                            }
                        } else {
                            for (double patternMass : patternMasses) {
                                for (double mass : aminoAcidMasses) {
                                    int aa = ionNumberOffset + i;
                                    int subaa = i + 1;
                                    double patternFragmentMass = patternMass + mass;
                                    double forwardMass = massOffset + patternFragmentMass;

                                    // add the a-ions
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        result.add(new TagFragmentIon(TagFragmentIon.A_ION, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                    }

                                    // add the b-ions
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        result.add(new TagFragmentIon(TagFragmentIon.B_ION, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                                    }

                                    // add the c-ion
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        result.add(new TagFragmentIon(TagFragmentIon.C_ION, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                                    }

                                    if (!newPatternMassess.contains(patternFragmentMass)) {
                                        newPatternMassess.add(patternFragmentMass);
                                    }
                                }
                            }
                        }
                        patternMasses = newPatternMassess;
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    for (double mass : patternMasses) {
                        double newMass = offsetMass + mass;
                        if (!newOffsetMasses.contains(newMass)) {
                            newOffsetMasses.add(newMass);
                        }
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidPattern.length();
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                double sequenceMass = 0;
                for (int i = 0; i < aminoAcidSequence.length(); i++) {
                    AminoAcid aminoAcid = aminoAcidSequence.getAminoAcidAt(i);
                    double mass = aminoAcid.monoisotopicMass;
                    sequenceMass += mass;
                    for (double massOffset : massOffsets) {
                        int aa = ionNumberOffset + i;
                        int subaa = i + 1;
                        double forwardMass = massOffset + sequenceMass;

                        // add the a-ions
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            result.add(new TagFragmentIon(TagFragmentIon.A_ION, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                        }

                        // add the b-ions
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            result.add(new TagFragmentIon(TagFragmentIon.B_ION, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                        }

                        // add the c-ion
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            result.add(new TagFragmentIon(TagFragmentIon.C_ION, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                        }
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    double newMass = offsetMass + sequenceMass;
                    if (!newOffsetMasses.contains(newMass)) {
                        newOffsetMasses.add(newMass);
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidSequence.length();
            } else if (tagComponent instanceof MassGap) {
                double gapMass = tagComponent.getMass();
                int aa = ionNumberOffset;
                int subaa = 0;
                for (double massOffset : massOffsets) {
                    double forwardMass = massOffset + gapMass;

                    // add the a-ions
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        result.add(new TagFragmentIon(TagFragmentIon.A_ION, aa, subaa, forwardMass - Atom.C.getMonoisotopicMass() - Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                    }

                    // add the b-ions
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        result.add(new TagFragmentIon(TagFragmentIon.B_ION, aa, subaa, forwardMass - getLossesMass(losses), losses, massOffset));
                    }

                    // add the c-ion
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        result.add(new TagFragmentIon(TagFragmentIon.C_ION, aa, subaa, forwardMass + Atom.N.getMonoisotopicMass() + 3 * Atom.H.getMonoisotopicMass() - getLossesMass(losses), losses, massOffset));
                    }

                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    newOffsetMasses.add(offsetMass + gapMass);
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset++;
            } else {
                throw new UnsupportedOperationException("Fragment ion not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }

        ArrayList<TagComponent> reversedTag = new ArrayList<TagComponent>(tag.getContent());
        Collections.reverse(reversedTag);
        ionNumberOffset = 0;
        massOffsets.clear();
        massOffsets.add(0.0);
        for (TagComponent tagComponent : reversedTag) {
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                ArrayList<Double> patternMasses = new ArrayList<Double>();
                for (int i = aminoAcidPattern.length() - 1; i >= 0; i--) {
                    ArrayList<Double> aminoAcidMasses = new ArrayList<Double>();
                    for (AminoAcid aminoAcid : aminoAcidPattern.getTargetedAA(i)) {
                        double mass = aminoAcid.monoisotopicMass;
                        for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i + 1)) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationMatch.getTheoreticPtm());
                            mass += ptm.getMass();
                        }
                        if (!aminoAcidMasses.contains(mass)) {
                            aminoAcidMasses.add(mass);
                        }
                    }
                    for (double massOffset : massOffsets) {
                        ArrayList<Double> newPatternMassess = new ArrayList<Double>();
                        if (patternMasses.isEmpty()) {
                            for (double mass : aminoAcidMasses) {
                                int aa = ionNumberOffset + aminoAcidPattern.length() - i;
                                int subaa = aminoAcidPattern.length() - i;
                                double rewindMass = massOffset + mass;
                                double gap = 0;
                                if (massOffset != Atom.O.getMonoisotopicMass()) {
                                    gap = massOffset;
                                }

                                // add the x-ions
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    result.add(new TagFragmentIon(TagFragmentIon.X_ION, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                }

                                // add the y-ions
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    result.add(new TagFragmentIon(TagFragmentIon.Y_ION, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                }

                                // add the z-ion
                                for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                    result.add(new TagFragmentIon(TagFragmentIon.Z_ION, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                }

                                if (!newPatternMassess.contains(mass)) {
                                    newPatternMassess.add(mass);
                                }
                            }
                        } else {
                            for (double patternMass : patternMasses) {
                                for (double mass : aminoAcidMasses) {
                                    int aa = ionNumberOffset + aminoAcidPattern.length() - i;
                                    int subaa = aminoAcidPattern.length() - i;
                                    double patternFragmentMass = patternMass + mass;
                                    double rewindMass = massOffset + patternFragmentMass;
                                    double gap = 0;
                                    if (massOffset != Atom.O.getMonoisotopicMass()) {
                                        gap = massOffset;
                                    }

                                    // add the x-ions
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        result.add(new TagFragmentIon(TagFragmentIon.X_ION, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                    }

                                    // add the y-ions
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        result.add(new TagFragmentIon(TagFragmentIon.Y_ION, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                    }

                                    // add the z-ion
                                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                                        result.add(new TagFragmentIon(TagFragmentIon.Z_ION, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                                    }

                                    if (!newPatternMassess.contains(patternFragmentMass)) {
                                        newPatternMassess.add(patternFragmentMass);
                                    }
                                }
                            }
                        }
                        patternMasses = newPatternMassess;
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    for (double mass : patternMasses) {
                        double newMass = offsetMass + mass;
                        if (!newOffsetMasses.contains(newMass)) {
                            newOffsetMasses.add(newMass);
                        }
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidPattern.length();
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                double sequenceMass = 0;
                for (int i = aminoAcidSequence.length() - 1; i >= 0; i--) {
                    AminoAcid aminoAcid = aminoAcidSequence.getAminoAcidAt(i);
                    double mass = aminoAcid.monoisotopicMass;
                    sequenceMass += mass;
                    for (double massOffset : massOffsets) {
                        int aa = ionNumberOffset + aminoAcidSequence.length() - i;
                        int subaa = aminoAcidSequence.length() - i;
                        double rewindMass = massOffset + sequenceMass;
                        double gap = 0;
                        if (massOffset != Atom.O.getMonoisotopicMass()) {
                            gap = massOffset;
                        }

                        // add the x-ions
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            result.add(new TagFragmentIon(TagFragmentIon.X_ION, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                        }

                        // add the y-ions
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            result.add(new TagFragmentIon(TagFragmentIon.Y_ION, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                        }

                        // add the z-ion
                        for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                            result.add(new TagFragmentIon(TagFragmentIon.Z_ION, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                        }
                    }
                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    double newMass = offsetMass + sequenceMass;
                    if (!newOffsetMasses.contains(newMass)) {
                        newOffsetMasses.add(newMass);
                    }
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset += aminoAcidSequence.length();
            } else if (tagComponent instanceof MassGap) {
                double gapMass = tagComponent.getMass();
                int aa = ionNumberOffset;
                int subaa = 0;
                for (double massOffset : massOffsets) {
                    double gap = gapMass;
                    if (massOffset != Atom.O.getMonoisotopicMass()) {
                        gap += massOffset;
                    }
                    double rewindMass = massOffset + gapMass;

                    // add the x-ions
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        result.add(new TagFragmentIon(TagFragmentIon.X_ION, aa, subaa, rewindMass + Atom.C.getMonoisotopicMass() + 2 * Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                    }

                    // add the y-ions
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        result.add(new TagFragmentIon(TagFragmentIon.Y_ION, aa, subaa, rewindMass + 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                    }

                    // add the z-ion
                    for (ArrayList<NeutralLoss> losses : neutralLossesCombinations) {
                        result.add(new TagFragmentIon(TagFragmentIon.Z_ION, aa, subaa, rewindMass - Atom.N.getMonoisotopicMass() + Atom.O.getMonoisotopicMass() - getLossesMass(losses), losses, gap));
                    }

                }
                ArrayList<Double> newOffsetMasses = new ArrayList<Double>();
                for (double offsetMass : massOffsets) {
                    newOffsetMasses.add(offsetMass + gapMass);
                }
                massOffsets = newOffsetMasses;
                ionNumberOffset++;
            } else {
                throw new UnsupportedOperationException("Fragment ion not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }

        result.addAll(reporterIons);
        return result;
    }

    /**
     * Convenience method returning the possible neutral losses combination as
     * accounted by the factory, i.e., for now up to two neutral losses per
     * peak.
     *
     * @param possibleNeutralLosses the possible neutral losses
     * @return the possible combinations
     */
    public static ArrayList<ArrayList<NeutralLoss>> getAccountedNeutralLosses(ArrayList<NeutralLoss> possibleNeutralLosses) {

        // We will account for up to two neutral losses per ion maximum
        ArrayList<ArrayList<NeutralLoss>> neutralLossesCombinations = new ArrayList<ArrayList<NeutralLoss>>();
        ArrayList<NeutralLoss> tempList = new ArrayList<NeutralLoss>();
        neutralLossesCombinations.add(tempList);

        for (NeutralLoss neutralLoss1 : possibleNeutralLosses) {
            boolean found = false;
            for (ArrayList<NeutralLoss> accountedCombination : neutralLossesCombinations) {
                if (accountedCombination.size() == 1 && accountedCombination.get(0).isSameAs(neutralLoss1)) {
                    found = true;
                }
            }
            if (!found) {
                tempList = new ArrayList<NeutralLoss>();
                tempList.add(neutralLoss1);
                neutralLossesCombinations.add(tempList);
            }
            for (NeutralLoss neutralLoss2 : possibleNeutralLosses) {
                if (!neutralLoss1.isSameAs(neutralLoss2)) {
                    found = false;
                    for (ArrayList<NeutralLoss> accountedCombination : neutralLossesCombinations) {
                        if (accountedCombination.size() == 2) {
                            if (accountedCombination.get(0).isSameAs(neutralLoss1) && accountedCombination.get(1).isSameAs(neutralLoss2)) {
                                found = true;
                                break;
                            }
                            if (accountedCombination.get(0).isSameAs(neutralLoss2) && accountedCombination.get(1).isSameAs(neutralLoss1)) {
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        tempList = new ArrayList<NeutralLoss>();
                        tempList.add(neutralLoss1);
                        tempList.add(neutralLoss2);
                        neutralLossesCombinations.add(tempList);
                    }
                }
            }
        }

        return neutralLossesCombinations;
    }

    /**
     * Convenience summing the masses of various neutral losses.
     *
     * @param neutralLosses list of neutral losses
     * @return the summ of the masses
     */
    public static double getLossesMass(ArrayList<NeutralLoss> neutralLosses) {
        double result = 0;
        for (NeutralLoss neutralLoss : neutralLosses) {
            result += neutralLoss.mass;
        }
        return result;
    }
}
