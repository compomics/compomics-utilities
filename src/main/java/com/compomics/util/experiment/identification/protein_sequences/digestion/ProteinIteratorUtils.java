package com.compomics.util.experiment.identification.protein_sequences.digestion;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.general.BoxedObject;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains utilities used during the iteration of protein sequences.
 *
 * @author Marc Vaudel
 */
public class ProteinIteratorUtils {

    /**
     * The maximal number of Xs allowed to derive peptide sequences. When
     * allowing multiple Xs all possible combinations will be generated.
     */
    private int maxXsInSequence = 2;
    /**
     * The fixed protein N-term modification.
     */
    private String fixedProteinNtermModification = null;
    /**
     * The fixed protein C-term modification.
     */
    private String fixedProteinCtermModification = null;
    /**
     * The fixed protein N-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedProteinNtermModificationsAtAa = new HashMap<>(0);
    /**
     * The fixed protein C-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedProteinCtermModificationsAtAa = new HashMap<>(0);
    /**
     * The fixed peptide N-term modification.
     */
    private String fixedPeptideNtermModification = null;
    /**
     * The fixed peptide C-term modification.
     */
    private String fixedPeptideCtermModification = null;
    /**
     * The fixed peptide N-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedPeptideNtermModificationsAtAa = new HashMap<>(0);
    /**
     * The fixed peptide C-term modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedPeptideCtermModificationsAtAa = new HashMap<>(0);
    /**
     * The fixed modifications at specific amino acids.
     */
    private HashMap<Character, String> fixedModificationsAtAa = new HashMap<>(0);
    /**
     * Map of modifications at specific amino acids (termini or not) targeting a
     * pattern of amino acids.
     */
    private HashMap<String, AminoAcidPattern> modificationPatternMap = new HashMap<>(1);
    /**
     * Convenience map of the amino acid masses.
     */
    private HashMap<String, Double> modificationsMasses;
    /**
     * The minimal mass a c-terminus modification can have. 0.0 by default for
     * no modification.
     */
    private double minCtermMass = 0.0;
    /**
     * The mass of water (H2O).
     */
    public static final double WATER_MASS = (2 * Atom.H.getMonoisotopicMass()) + Atom.O.getMonoisotopicMass();

    /**
     * Constructor.
     *
     * @param fixedModifications a list of fixed modifications to consider when
     * iterating the protein sequences.
     * @param maxX The maximal number of Xs allowed in a sequence to derive the
     * possible peptides
     */
    public ProteinIteratorUtils(ArrayList<String> fixedModifications, Integer maxX) {
        fillPtmMaps(fixedModifications);
        if (maxX != null) {
            maxXsInSequence = maxX;
        }
    }

    /**
     * Fills the fixed modification attributes of the class based on the given
     * list of modifications.
     *
     * @param fixedModifications the list of fixed modifications to consider.
     */
    private void fillPtmMaps(ArrayList<String> fixedModifications) {
        modificationsMasses = new HashMap<>(fixedModifications.size());
        modificationsMasses.put(null, 0.0);
        PTMFactory ptmFactory = PTMFactory.getInstance();
        for (String ptmName : fixedModifications) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            switch (ptm.getType()) {
                case PTM.MODN:
                    if (fixedProteinNtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the protein N-terminus.");
                    }
                    fixedProteinNtermModification = ptmName;
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODC:
                    if (fixedProteinCtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the protein C-terminus.");
                    }
                    fixedProteinCtermModification = ptmName;
                    Double ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODNP:
                    if (fixedPeptideNtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the peptide N-terminus.");
                    }
                    fixedPeptideNtermModification = ptmName;
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODCP:
                    if (fixedPeptideCtermModification != null) {
                        throw new IllegalArgumentException("Only one fixed modification supported for the peptide C-terminus.");
                    }
                    fixedPeptideCtermModification = ptmName;
                    ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODNAA:
                    AminoAcidPattern ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedProteinNtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per protein N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedProteinNtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODCAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedProteinCtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per protein C-term amino acid. Found two at " + aa + ".");
                        }
                        fixedProteinCtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODNPAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedPeptideNtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per peptide N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedPeptideNtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
                case PTM.MODCPAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedPeptideCtermModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per peptide N-term amino acid. Found two at " + aa + ".");
                        }
                        fixedPeptideCtermModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    ptmMass = ptm.getMass();
                    modificationsMasses.put(ptmName, ptmMass);
                    if (ptmMass < minCtermMass) {
                        minCtermMass = ptmMass;
                    }
                    break;
                case PTM.MODAA:
                    ptmPattern = ptm.getPattern();
                    for (Character aa : ptmPattern.getAminoAcidsAtTarget()) {
                        String modificationAtAa = fixedModificationsAtAa.get(aa);
                        if (modificationAtAa != null) {
                            throw new IllegalArgumentException("Only one fixed modification supported per amino acid. Found two at " + aa + ".");
                        }
                        fixedModificationsAtAa.put(aa, ptm.getName());
                    }
                    if (ptmPattern.length() > 1) {
                        if (modificationPatternMap == null) {
                            modificationPatternMap = new HashMap<>(1);
                        }
                        modificationPatternMap.put(ptmName, ptmPattern);
                    }
                    modificationsMasses.put(ptmName, ptm.getMass());
                    break;
            }
        }
    }

    /**
     * Returns the N-term modification for the given amino acid. Null if no
     * modification is found.
     *
     * @param proteinNTerm boolean indicating whether the amino acid is at the
     * protein N-terminus
     * @param nTermAaChar the amino acid as character
     * @param proteinSequence the protein sequence
     *
     * @return the N-term modification for the given amino acid.
     */
    public String getNtermModification(boolean proteinNTerm, char nTermAaChar, String proteinSequence) {
        if (proteinNTerm) {
            if (fixedProteinNtermModification != null) {
                return fixedProteinNtermModification;
            }
            String fixedProteinNtermModificationAtAa = fixedProteinNtermModificationsAtAa.get(nTermAaChar);
            if (fixedProteinNtermModificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinNtermModificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                    return fixedProteinNtermModificationAtAa;
                }
            }
        }
        if (fixedPeptideNtermModification != null) {
            return fixedPeptideNtermModification;
        }
        String fixedPeptideNtermModificationAtAa = fixedPeptideNtermModificationsAtAa.get(nTermAaChar);
        if (fixedPeptideNtermModificationAtAa != null) {
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideNtermModificationAtAa);
            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, 0)) {
                return fixedPeptideNtermModificationAtAa;
            }
        }
        return null;
    }

    /**
     * Returns the c-terminal modification for the given peptide draft.
     *
     * @param peptideDraft the peptide draft of interest
     * @param proteinSequence the protein sequence
     * @param indexOnProtein the index of the peptide draft on the protein
     *
     * @return the c-terminal modification for the given peptide draft
     */
    public String getCtermModification(PeptideDraft peptideDraft, String proteinSequence, int indexOnProtein) {

        char[] peptideSequence = peptideDraft.getSequence();
        char aaChar = peptideSequence[peptideSequence.length - 1];

        if (indexOnProtein == proteinSequence.length() - peptideDraft.length()) {

            if (fixedProteinCtermModification != null) {
                return fixedProteinCtermModification;
            }

            String fixedProteinCtermModificationAtAa = fixedProteinCtermModificationsAtAa.get(aaChar);
            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedProteinCtermModificationAtAa);

            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, proteinSequence.length() - 1)) {
                return fixedProteinCtermModification;
            }
        }

        if (fixedPeptideCtermModification != null) {
            return fixedPeptideCtermModification;
        }

        String fixedPeptideCtermModificationAtAa = fixedPeptideCtermModificationsAtAa.get(aaChar);

        if (fixedPeptideCtermModificationAtAa != null) {

            AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(fixedPeptideCtermModificationAtAa);

            if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, indexOnProtein + peptideDraft.length())) {
                return fixedPeptideCtermModificationAtAa;
            }
        }

        return null;
    }

    /**
     * Returns the mass corresponding to a given modification.
     *
     * @param modificationName the name of the modification
     *
     * @return the mass of the modification
     */
    public double getModificationMass(String modificationName) {
        return modificationsMasses.get(modificationName);
    }

    /**
     * Returns the fixed modification that can be found at the given amino acid.
     * Null if none.
     *
     * @param aa the one letter code of the amino acid
     *
     * @return the fixed modification that can be found at the given amino acid
     */
    public String getFixedModificationAtAa(char aa) {
        return fixedModificationsAtAa.get(aa);
    }

    /**
     * Returns the modification pattern that is targeted by the given
     * modification. Null if no pattern longer than one is targeted.
     *
     * @param modificationName the name of the modification
     *
     * @return the modification pattern that is targeted
     */
    public AminoAcidPattern getModificationPattern(String modificationName) {
        return modificationPatternMap.get(modificationName);
    }

    /**
     * Returns the maximal number of Xs to account for in a sequence.
     *
     * @return the maximal number of Xs to account for in a sequence
     */
    public int getMaxXsInSequence() {
        return maxXsInSequence;
    }

    /**
     * Returns the minimal mass to consider for a c-terminus.
     *
     * @return the minimal mass to consider for a c-terminus
     */
    public double getMinCtermMass() {
        return minCtermMass;
    }

    /**
     * Returns a peptide from the given sequence. The sequence should not
     * contain ambiguous amino acids. Peptides are filtered according to the
     * given masses. Filters are ignored if null.
     *
     * @param proteinSequence the protein sequence where this peptide was found
     * @param indexOnProtein the index on the protein
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return a peptide from the given sequence
     */
    public Peptide getPeptideFromProtein(char[] proteinSequence, int indexOnProtein, double massMin, double massMax) {
        return ProteinIteratorUtils.this.getPeptideFromProtein(proteinSequence, new String(proteinSequence), indexOnProtein, massMin, massMax);
    }

    /**
     * Returns a peptide from the given sequence on the given protein. The
     * sequence should not contain ambiguous amino acids. Peptides are filtered
     * according to the given masses. Filters are ignored if null.
     *
     * @param peptideSequence the peptide sequence
     * @param proteinSequence the protein sequence where this peptide was found
     * @param indexOnProtein the index on the protein
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     *
     * @return a peptide from the given sequence
     */
    public Peptide getPeptideFromProtein(char[] peptideSequence, String proteinSequence, int indexOnProtein, Double massMin, Double massMax) {
        return getPeptideFromProtein(peptideSequence, proteinSequence, indexOnProtein, massMin, massMax, new BoxedObject<>(Boolean.TRUE));
    }

    /**
     * Returns a peptide from the given sequence on the given protein. The
     * sequence should not contain ambiguous amino acids. Peptides are filtered
     * according to the given masses. Filters are ignored if null.
     *
     * @param peptideSequence the peptide sequence
     * @param proteinSequence the protein sequence where this peptide was found
     * @param indexOnProtein the index on the protein
     * @param massMin the minimal mass
     * @param massMax the maximal mass
     * @param smallMass an encapsulated boolean indicating whether the peptide
     * passed the maximal mass filter
     *
     * @return a peptide from the given sequence
     */
    public Peptide getPeptideFromProtein(char[] peptideSequence, String proteinSequence, int indexOnProtein, Double massMin, Double massMax, BoxedObject<Boolean> smallMass) {

        char nTermAaChar = peptideSequence[0];
        String nTermModification = getNtermModification(indexOnProtein == 0, nTermAaChar, proteinSequence);
        HashMap<Integer, String> peptideModifications = new HashMap<>(1);
        double peptideMass = modificationsMasses.get(nTermModification);

        for (int i = 0; i < peptideSequence.length; i++) {

            char aaChar = peptideSequence[i];
            AminoAcid aminoAcid = AminoAcid.getAminoAcid(aaChar);
            peptideMass += aminoAcid.getMonoisotopicMass();

            if (massMax != null && peptideMass + minCtermMass > massMax) {
                smallMass.setObject(Boolean.FALSE);
                return null;
            }

            String modificationAtAa = fixedModificationsAtAa.get(aaChar);

            if (modificationAtAa != null) {
                AminoAcidPattern aminoAcidPattern = modificationPatternMap.get(modificationAtAa);
                if (aminoAcidPattern == null || aminoAcidPattern.matchesAt(proteinSequence, SequenceMatchingPreferences.defaultStringMatching, i)) {
                    peptideModifications.put(i + 1, modificationAtAa);
                    peptideMass += modificationsMasses.get(modificationAtAa);
                }
            }
        }

        PeptideDraft peptideDraft = new PeptideDraft(peptideSequence, nTermModification, peptideModifications, peptideMass);

        String cTermModification = getCtermModification(peptideDraft, proteinSequence, indexOnProtein);
        if (cTermModification != null) {
            double modificationMass = modificationsMasses.get(cTermModification);
            peptideMass = peptideDraft.getMass() + modificationMass;
            peptideDraft.setMass(peptideMass);
            peptideDraft.setcTermModification(cTermModification);
        }

        return peptideDraft.getPeptide(massMin, massMax);
    }
}
