package com.compomics.util.experiment.identification.protein_inference;

import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.PeptideVariantMatches;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Class used to model the mapping of a peptide to a protein sequence.
 *
 * @author Marc Vaudel
 */
public class PeptideProteinMapping {

    /**
     * Accession of the protein.
     */
    private final String proteinAccession;
    /**
     * The peptide sequence.
     */
    private final String peptideSequence;
    /**
     * Index on the protein sequence, 0 is the first amino acid.
     */
    private final int index;
    /**
     * Eventual modifications.
     */
    private final ArrayList<ModificationMatch> modificationMatches;
    /**
     * Eventual variants.
     */
    private final PeptideVariantMatches peptideVariantMatches;

    /**
     * Constructor.
     *
     * @param proteinAccession the accession of the protein
     * @param peptideSequence the peptide sequence
     * @param index the index on the protein
     * @param modificationMatches eventual modification matches
     * @param peptideVariantMatches eventual sequence variants
     */
    public PeptideProteinMapping(String proteinAccession, String peptideSequence, int index, ArrayList<ModificationMatch> modificationMatches, PeptideVariantMatches peptideVariantMatches) {

        this.proteinAccession = proteinAccession;
        this.peptideSequence = peptideSequence;
        this.index = index;
        this.modificationMatches = modificationMatches;
        this.peptideVariantMatches = peptideVariantMatches;

    }

    /**
     * Constructor.
     *
     * @param proteinAccession the accession of the protein
     * @param peptideSequence the peptide sequence
     * @param index the index on the protein
     */
    public PeptideProteinMapping(String proteinAccession, String peptideSequence, int index) {
        this(proteinAccession, peptideSequence, index, null, null);
    }

    /**
     * Constructor.
     *
     * @param proteinAccession the accession of the protein
     * @param peptideSequence the peptide sequence
     * @param index the index on the protein
     * @param modificationMatches modification matches
     */
    public PeptideProteinMapping(String proteinAccession, String peptideSequence, int index, ArrayList<ModificationMatch> modificationMatches) {
        this(proteinAccession, peptideSequence, index, modificationMatches, null);
    }

    /**
     * Returns the accession of the protein.
     *
     * @return the accession of the protein
     */
    public String getProteinAccession() {
        return proteinAccession;
    }

    /**
     * Returns the peptide sequence.
     *
     * @return the peptide sequence
     */
    public String getPeptideSequence() {
        return peptideSequence;
    }

    /**
     * Returns the index on the protein.
     *
     * @return the index on the protein
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns eventual modifications found.
     *
     * @return eventual modifications found
     */
    public ArrayList<ModificationMatch> getModificationMatches() {
        return modificationMatches;
    }

    /**
     * Returns eventual variants found.
     *
     * @return eventual variants found
     */
    public PeptideVariantMatches getPeptideVariantMatches() {
        return peptideVariantMatches;
    }

    /**
     * Returns a map made from the given mappings containing the indexes of the
     * peptides in the protein sequences indexed by peptide sequence and protein
     * accession.
     *
     * @param peptideProteinMappings a list of peptide to protein mappings
     *
     * @return a map of the mapping
     */
    public static HashMap<String, HashMap<String, int[]>> getPeptideProteinIndexesMap(ArrayList<PeptideProteinMapping> peptideProteinMappings) {
        /*
        HashMap<String, HashMap<String, int[]>> resultMap = peptideProteinMappings.stream()
                .collect(Collectors.groupingBy(PeptideProteinMapping::getProteinAccession)).entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.groupingBy(PeptideProteinMapping::getPeptideSequence)).entrySet().stream()
                                .collect(Collectors.toMap(
                                        Entry::getKey,
                                        entry2 -> entry2.getValue().stream()
                                                .collect(Collectors.toCollection(HashSet::new)).stream()
                                                .mapToInt(peptideProteinMapping -> peptideProteinMapping.getIndex())
                                                .sorted()
                                                .toArray(),
                                        (a, b) -> {throw new IllegalStateException("Duplicate key in groupingBy.");},
                                        HashMap::new)),
                        (a, b) -> {throw new IllegalStateException("Duplicate key in groupingBy.");},
                        HashMap::new));
        */
        
        HashMap<String, HashMap<String, ArrayList<Integer>>> resultMapTmp = new HashMap<>();
        
        for (int i = 0; i < peptideProteinMappings.size(); ++i){
            PeptideProteinMapping ppm = peptideProteinMappings.get(i);
            String accession = ppm.getProteinAccession();
            String sec = ppm.getPeptideSequence();
            int index = ppm.getIndex();
            if (!resultMapTmp.containsKey(sec)) resultMapTmp.put(sec, new HashMap<String, ArrayList<Integer>>());
            HashMap<String, ArrayList<Integer>> accessions = resultMapTmp.get(sec);
            if (!accessions.containsKey(accession)) accessions.put(accession, new ArrayList<Integer>());
            ArrayList<Integer> indexes = accessions.get(accession);
            indexes.add(index);
        }
        
        
        HashMap<String, HashMap<String, int[]>> resultMap = new HashMap<>();
        for (String sec : resultMapTmp.keySet()){
            HashMap<String, int[]> accessions = new HashMap<>();
            for (String accession : resultMapTmp.get(sec).keySet()){
                ArrayList<Integer> list = resultMapTmp.get(sec).get(accession);
                Collections.sort(list);
                int[] indexes = new int[list.size()];
                for (int i = 0; i < list.size(); ++i) indexes[i] = list.get(i);
                accessions.put(accession, indexes);
            }
            resultMap.put(sec, accessions);
        }
        
        return resultMap;
    }

    /**
     * Returns a map made from the given mappings containing protein accessions
     * for every peptide sequence.
     *
     * @param peptideProteinMappings a list of peptide to protein mappings
     *
     * @return a map of the mapping
     */
    public static HashMap<String, HashSet<String>> getPeptideProteinMap(ArrayList<PeptideProteinMapping> peptideProteinMappings) {

      return peptideProteinMappings.stream()
                .collect(Collectors.groupingBy(PeptideProteinMapping::getPeptideSequence)).entrySet().stream()
              .collect(Collectors.toMap(
                      Entry::getKey, 
                      entry -> entry.getValue().stream()
                              .map(peptideProteinMapping -> peptideProteinMapping.getProteinAccession())
                              .collect(Collectors.toCollection(HashSet::new)), 
                      (a,b) -> {throw new IllegalStateException("Duplicate key in groupingBy.");}, 
                      HashMap::new));
    }

    /**
     * Aggregates the given mapping into a list of peptides.
     *
     * @param peptideProteinMappings a list of peptides to protein mappings
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a list of peptides
     */
    public static Collection<Peptide> getPeptides(ArrayList<PeptideProteinMapping> peptideProteinMappings, SequenceMatchingParameters sequenceMatchingPreferences) {

        HashMap<String, Peptide> peptidesMap = new HashMap<>(peptideProteinMappings.size());
        HashMap<String, HashMap<String, HashSet<Integer>>> proteinsMap = new HashMap<>(peptideProteinMappings.size());
        HashMap<String, HashMap<String, HashMap<Integer, PeptideVariantMatches>>> variantsMap = new HashMap<>(peptideProteinMappings.size());

        for (PeptideProteinMapping peptideProteinMapping : peptideProteinMappings) {

            Peptide tempPeptide = new Peptide(peptideProteinMapping.getPeptideSequence(),
                    peptideProteinMapping.getModificationMatches());
            String peptideKey = tempPeptide.getMatchingKey(sequenceMatchingPreferences);
            Peptide peptide = peptidesMap.get(peptideKey);

            String proteinAccession = peptideProteinMapping.getProteinAccession();
            int peptideIndex = peptideProteinMapping.getIndex();

            if (peptide == null) {

                peptidesMap.put(peptideKey, tempPeptide);

                HashSet<Integer> peptideIndexes = new HashSet<>(1);
                peptideIndexes.add(peptideIndex);
                HashMap<String, HashSet<Integer>> proteinMapping = new HashMap<>(1);
                proteinMapping.put(proteinAccession, peptideIndexes);
                proteinsMap.put(peptideKey, proteinMapping);

                HashMap<Integer, PeptideVariantMatches> variantIndex = new HashMap<>(1);
                variantIndex.put(peptideIndex, peptideProteinMapping.getPeptideVariantMatches());
                HashMap<String, HashMap<Integer, PeptideVariantMatches>> proteinVariantMap = new HashMap<>(1);
                proteinVariantMap.put(proteinAccession, variantIndex);
                variantsMap.put(peptideKey, proteinVariantMap);

            } else {

                HashMap<String, HashSet<Integer>> proteinMapping = proteinsMap.get(peptideKey);
                HashSet<Integer> peptideIndexes = proteinMapping.get(proteinAccession);

                if (peptideIndexes == null) {

                    peptideIndexes = new HashSet<>(1);
                    proteinMapping.put(proteinAccession, peptideIndexes);

                }

                peptideIndexes.add(peptideIndex);

                HashMap<String, HashMap<Integer, PeptideVariantMatches>> proteinVariantMap = variantsMap.get(peptideKey);
                HashMap<Integer, PeptideVariantMatches> variantIndex = proteinVariantMap.get(proteinAccession);

                if (variantIndex == null) {

                    variantIndex = new HashMap<>(1);
                    proteinVariantMap.put(proteinAccession, variantIndex);

                }

                variantIndex.put(peptideIndex, peptideProteinMapping.getPeptideVariantMatches());

            }
        }

        for (String peptideKey : peptidesMap.keySet()) {

            Peptide peptide = peptidesMap.get(peptideKey);

            HashMap<String, HashSet<Integer>> proteinMapping = proteinsMap.get(peptideKey);
            HashMap<String, int[]> proteinMappingArray = proteinMapping.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey,
                            entry -> entry.getValue().stream()
                                    .mapToInt(Integer::intValue)
                                    .sorted()
                                    .toArray(),
                            null,
                            HashMap::new));

            peptide.setProteinMapping(proteinMappingArray);

            HashMap<String, HashMap<Integer, PeptideVariantMatches>> variantMapping = variantsMap.get(peptideKey);
            peptide.setVariantMatches(variantMapping);

        }

        return peptidesMap.values();
    }
}
