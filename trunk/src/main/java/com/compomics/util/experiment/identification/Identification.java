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
     * Add a protein identification to the identification results
     *
     * @param proteinMatch the protein identification match
     */
    public void addProteinMatch(ProteinMatch proteinMatch) throws Exception {
        for (PeptideMatch peptideMatch : proteinMatch.getPeptideMatches().values()) {
            for (SpectrumMatch spectrumMatch : peptideMatch.getSpectrumMatches().values()) {
                addSpectrumMatch(spectrumMatch);
            }
        }
    }

    /**
     * Add protein identifications to the identification results
     *
     * @param proteinMatches the list of protein identification matches
     */
    public void addProteinMatch(ArrayList<ProteinMatch> proteinMatches) throws Exception {
        for (ProteinMatch proteinMatch : proteinMatches) {
            addProteinMatch(proteinMatch);
        }
    }

    /**
     * Add a spectrum match to the model.
     *
     * @param newMatch the new spectrum match
     * @throws Exception exception thrown when one tries to assign more than one identification per advocate to the same spectrum
     */
    public void addSpectrumMatch(SpectrumMatch newMatch) throws Exception {
        String proteinKey, peptideKey, spectrumKey = newMatch.getKey();
        Peptide peptide;
        SpectrumMatch oldMatch = spectrumIdentification.get(spectrumKey);
        if (!spectrumIdentification.containsKey(spectrumKey)) {
            spectrumIdentification.put(spectrumKey, newMatch);
            oldMatch = newMatch;
        } else {
            for (int searchEngine : newMatch.getAdvocates()) {
                oldMatch.addFirstHit(searchEngine, newMatch.getFirstHit(searchEngine));
            }
        }
        for (int searchEngine : newMatch.getAdvocates()) {
            peptide = newMatch.getFirstHit(searchEngine).getPeptide();
            peptideKey = peptide.getKey();
            if (peptideIdentification.containsKey(peptideKey) && !peptideIdentification.get(peptideKey).getSpectrumMatches().containsKey(spectrumKey)) {
                peptideIdentification.get(peptideKey).addSpectrumMatch(spectrumIdentification.get(spectrumKey));
            } else if (!peptideIdentification.containsKey(peptideKey)) {
                peptideIdentification.put(peptideKey, new PeptideMatch(peptide, oldMatch));
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
