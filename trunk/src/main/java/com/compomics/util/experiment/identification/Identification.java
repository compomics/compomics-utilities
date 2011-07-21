package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.matches.PeptideMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.personalization.ExperimentObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * This class contains identification results.
 * User: Marc
 * Date: Nov 11, 2010
 * Time: 3:56:15 PM
 */
public abstract class Identification extends ExperimentObject {

    /**
     * The version UID for Serialization/Deserialization compatibility
     */
    static final long serialVersionUID = -2551700699384242554L;
    /**
     * The identification results as a proteine match map, indexed by protein accession.
     */
    protected HashMap<String, ProteinMatch> proteinIdentification = new HashMap<String, ProteinMatch>();
    /**
     * The identification results as a peptide match map, indexed by peptide index
     */
    protected HashMap<String, PeptideMatch> peptideIdentification = new HashMap<String, PeptideMatch>();
    /**
     * The identification results as a spectrum match map, indexed by spectrum id: FILE_TITLE
     */
    protected HashMap<String, SpectrumMatch> spectrumIdentification = new HashMap<String, SpectrumMatch>();
    /**
     * a map linking protein accessions to all their protein matches
     */
    protected HashMap<String, ArrayList<ProteinMatch>> proteinMap = new HashMap<String, ArrayList<ProteinMatch>>();
    /**
     * The method used.
     */
    protected int methodUsed;

    /**
     * Returns the identification results associated to the corresponding method in a protein identification map (accession -> protein match).
     *
     * @return the corresponding identification results
     */
    public HashMap<String, ProteinMatch> getProteinIdentification() {
        return proteinIdentification;
    }

    /**
     * Returns the identification results in a peptide identification map (peptide index -> peptideMatch)
     *
     * @return the corresponding identification results
     */
    public HashMap<String, PeptideMatch> getPeptideIdentification() {
        return peptideIdentification;
    }

    /**
     * Returns the identification results in a spectrum identification map (spectrumMatch id -> spectrumMatch)
     *
     * @return the corresponding identification resutls
     */
    public HashMap<String, SpectrumMatch> getSpectrumIdentification() {
        return spectrumIdentification;
    }

    /**
     * Add a spectrum match to the spectrum matches map.
     *
     * @param newMatch the new spectrum match
     */
    public void addSpectrumMatch(SpectrumMatch newMatch) {
        String spectrumKey = newMatch.getKey();
        SpectrumMatch oldMatch = spectrumIdentification.get(spectrumKey);
        if (oldMatch == null) {
            spectrumIdentification.put(spectrumKey, newMatch);
        } else {
            for (int searchEngine : newMatch.getAdvocates()) {
                oldMatch.addHit(searchEngine, newMatch.getFirstHit(searchEngine));
            }
        }
    }

    /**
     * Creates the peptides and protein instances based on the spectrum matches. Note that the attribute bestAssumption should be set for every spectrum match at this point.
     */
    public void buildPeptidesAndProteins() {
        String peptideKey, proteinKey;
        Peptide peptide;
        for (SpectrumMatch spectrumMatch : getSpectrumIdentification().values()) {
            peptide = spectrumMatch.getBestAssumption().getPeptide();
            peptideKey = peptide.getKey();
            if (peptideIdentification.containsKey(peptideKey)) {
                peptideIdentification.get(peptideKey).addSpectrumMatch(spectrumMatch);
            } else {
                peptideIdentification.put(peptideKey, new PeptideMatch(peptide, spectrumMatch));
            }
            proteinKey = ProteinMatch.getProteinMatchKey(peptide);
            if (proteinIdentification.containsKey(proteinKey) && !proteinIdentification.get(proteinKey).getPeptideMatches().containsKey(peptideKey)) {
                proteinIdentification.get(proteinKey).addPeptideMatch(peptideIdentification.get(peptideKey));
            } else if (!proteinIdentification.containsKey(proteinKey)) {
                ProteinMatch proteinMatch = new ProteinMatch(peptideIdentification.get(peptideKey));
                proteinIdentification.put(proteinKey, proteinMatch);
                for (Protein protein : peptide.getParentProteins()) {
                    if (!proteinMap.containsKey(protein.getAccession())) {
                        proteinMap.put(protein.getAccession(), new ArrayList<ProteinMatch>());
                    }
                    proteinMap.get(protein.getAccession()).add(proteinMatch);
                }
            }
        }
    }
    
/**
 * Add a set of spectrumMatches to the model
 *
 * @param spectrumMatches The spectrum matches
 * @throws Exception exception thrown when one tries to assign more than one identification per advocate to the same spectrum
 */
public void addSpectrumMatch(Set<SpectrumMatch> spectrumMatches) throws Exception {
        for (SpectrumMatch spectrumMatch : spectrumMatches) {
            addSpectrumMatch(spectrumMatch);
        }
    }

    /**
     * Getter for the identification method used
     *
     * @return the identification method used
     */
    public int getMethodUsed() {
        return methodUsed;
    }

    /**
     * Returns a map of all the protein matches which can be ascribed to a protein indexed by its accession.
     * @return a map of all the protein matches which can be ascribed to a protein indexed by its accession.
     */
    public HashMap<String, ArrayList<ProteinMatch>> getProteinMap() {
        return proteinMap;
    }
}
