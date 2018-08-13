package com.compomics.util.experiment.biology.genes;

import com.compomics.util.experiment.biology.genes.ensembl.GeneMapping;
import com.compomics.util.experiment.biology.genes.go.GoMapping;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The gene maps for a given project.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class GeneMaps {

    /**
     * The Ensembl versions for each species.
     */
    private HashMap<String, String> ensemblVersionsMap;
    /**
     * Gene name to EnsemblId map.
     */
    private HashMap<String, String> geneNameToEnsemblIdMap;
    /**
     * Gene name to chromosome name map.
     */
    private HashMap<String, String> geneNameToChromosomeMap;
    /**
     * Protein accession to GO terms map.
     */
    private HashMap<String, HashSet<String>> proteinToGoMap;
    /**
     * GO term to protein accession map.
     */
    private HashMap<String, HashSet<String>> goAccessionToProteinMap;
    /**
     * GO term accession to name map.
     */
    private HashMap<String, String> goNamesMap;

    /**
     * Creates new maps.
     */
    public GeneMaps() {
        ensemblVersionsMap = new HashMap<>();
        geneNameToEnsemblIdMap = new HashMap<>();
        geneNameToChromosomeMap = new HashMap<>();
        proteinToGoMap = new HashMap<>();
        goAccessionToProteinMap = new HashMap<>();
        goNamesMap = new HashMap<>();
    }

    /**
     * Imports the gene maps from a gene Mapping.
     *
     * @param geneMapping a gene mapping
     */
    public void importMaps(GeneMapping geneMapping) {
        geneNameToChromosomeMap.putAll(geneMapping.getGeneNameToChromosome());
        geneNameToEnsemblIdMap.putAll(geneMapping.getGeneNameToAccession());
    }

    /**
     * Imports the GO maps from a GO mapping.
     *
     * @param goMapping a go mapping
     */
    public void setMaps(GoMapping goMapping) {
        goNamesMap.putAll(goMapping.getGoNamesMap());
        HashMap<String, HashSet<String>> otherMap = goMapping.getProteinToGoMap();
        for (String accession : otherMap.keySet()) {
            HashSet<String> goTerms = proteinToGoMap.get(accession);
            if (goTerms == null) {
                goTerms = new HashSet<>();
                proteinToGoMap.put(accession, goTerms);
            }
            goTerms.addAll(otherMap.get(accession));
        }
        otherMap = goMapping.getGoToProteinMap();
        for (String accession : otherMap.keySet()) {
            HashSet<String> proteins = goAccessionToProteinMap.get(accession);
            if (proteins == null) {
                proteins = new HashSet<>();
                proteinToGoMap.put(accession, proteins);
            }
            proteins.addAll(otherMap.get(accession));
        }
    }

    /**
     * Returns the Ensembl version map.
     *
     * @return the Ensembl version map
     */
    public HashMap<String, String> getEnsemblVersionsMap() {
        return ensemblVersionsMap;
    }

    /**
     * Sets the Ensembl version map.
     *
     * @param ensemblVersionsMap the Ensembl version map
     */
    public void setEnsemblVersionsMap(HashMap<String, String> ensemblVersionsMap) {
        this.ensemblVersionsMap = ensemblVersionsMap;
    }

    /**
     * Returns the gene name to Ensembl ID map.
     *
     * @return the gene name to Ensembl ID map
     */
    public HashMap<String, String> getGeneNameToEnsemblIdMap() {
        return geneNameToEnsemblIdMap;
    }

    /**
     * Sets the gene name to Ensembl ID map.
     *
     * @param geneNameToEnsemblIdMap the gene name to Ensembl ID map
     */
    public void setGeneNameToEnsemblIdMap(HashMap<String, String> geneNameToEnsemblIdMap) {
        this.geneNameToEnsemblIdMap = geneNameToEnsemblIdMap;
    }

    /**
     * Returns the gene name to chromosome map.
     *
     * @return the gene name to chromosome map
     */
    public HashMap<String, String> getGeneNameToChromosomeMap() {
        return geneNameToChromosomeMap;
    }

    /**
     * Sets the gene name to chromosome map.
     *
     * @param geneNameToChromosomeMap the gene name to chromosome map
     */
    public void setGeneNameToChromosomeMap(HashMap<String, String> geneNameToChromosomeMap) {
        this.geneNameToChromosomeMap = geneNameToChromosomeMap;
    }

    /**
     * Returns the protein to GO terms accession map.
     *
     * @return the protein to GO terms accession map
     */
    public HashMap<String, HashSet<String>> getProteinToGoMap() {
        return proteinToGoMap;
    }

    /**
     * Sets the protein to GO terms accession map.
     *
     * @param proteinToGoMap the protein to GO terms accession map
     */
    public void setProteinToGoMap(HashMap<String, HashSet<String>> proteinToGoMap) {
        this.proteinToGoMap = proteinToGoMap;
    }

    /**
     * Returns the GO to protein accession map.
     *
     * @return the GO to protein accession map
     */
    public HashMap<String, HashSet<String>> getGoAccessionToProteinMap() {
        return goAccessionToProteinMap;
    }

    /**
     * Sets the GO to protein accession map.
     *
     * @param goAccessionToProteinMap the GO to protein accession map
     */
    public void setGoAccessionToProteinMap(HashMap<String, HashSet<String>> goAccessionToProteinMap) {
        this.goAccessionToProteinMap = goAccessionToProteinMap;
    }

    /**
     * Returns the GO accession to names map.
     *
     * @return the GO accession to names map
     */
    public HashMap<String, String> getGoNamesMap() {
        return goNamesMap;
    }

    /**
     * Sets the GO accession to names map.
     *
     * @param goNamesMap the GO accession to names map
     */
    public void setGoNamesMap(HashMap<String, String> goNamesMap) {
        this.goNamesMap = goNamesMap;
    }

    /**
     * Returns the Ensembl ID corresponding to the given gene name. Null if not
     * found.
     *
     * @param geneName a gene name
     *
     * @return the corresponding Ensembl ID
     */
    public String getEnsemblId(String geneName) {
        return geneNameToEnsemblIdMap.get(geneName);
    }

    /**
     * Returns the chromosome corresponding to a given gene name.
     *
     * @param geneName the gene name
     *
     * @return the chromosome name
     */
    public String getChromosome(String geneName) {
        return geneNameToChromosomeMap.get(geneName);
    }

    /**
     * Returns the go terms accessions for a protein accession. Null if not
     * found.
     *
     * @param proteinAccession a protein accession
     *
     * @return the go terms names
     */
    public HashSet<String> getGoTermsForProtein(String proteinAccession) {
        return proteinToGoMap.get(proteinAccession);
    }

    /**
     * Returns the protein accessions for a GO accession. Null if not found.
     *
     * @param goAccession a GO term accession
     *
     * @return the corresponding proteins
     */
    public HashSet<String> getProteinsForGoTerm(String goAccession) {
        return goAccessionToProteinMap.get(goAccession);
    }

    /**
     * Returns the name of a GO term.
     *
     * @param goAccession the accession of the GO term.
     *
     * @return the name of a GO term
     */
    public String getNameForGoTerm(String goAccession) {
        return goNamesMap.get(goAccession);
    }

    /**
     * Returns the GO Term accession corresponding to the given name. Null if
     * not found.
     *
     * @param goName the GO name
     *
     * @return the corresponding accession
     */
    public String getGoAccession(String goName) {
        for (String goAccession : goNamesMap.keySet()) {
            if (goNamesMap.get(goAccession).equals(goName)) {
                return goAccession;
            }
        }
        return null;
    }

    /**
     * Returns the go terms names for a protein accession. Null if not found.
     *
     * @param proteinAccession a protein accession
     *
     * @return the go terms names
     */
    public HashSet<String> getGoNamesForProtein(String proteinAccession) {
    
        HashSet<String> goTerms = getGoTermsForProtein(proteinAccession);
        
        if (goTerms != null) {
        
            HashSet<String> goNames = new HashSet<>(goTerms.size());
            
            for (String goTerm : goTerms) {
            
                String goName = getNameForGoTerm(goTerm);
                
                if (goName != null) {
                
                    goNames.add(goName);
                
                }
            }
            
            return goNames;
        
        }
        
        return null;
    
    }

    /**
     * Indicates whether the GO maps are populated.
     *
     * @return a boolean indicating whether the GO maps are populated
     */
    public boolean hasGoMappings() {
        return !goNamesMap.isEmpty() && !proteinToGoMap.isEmpty();
    }
}
