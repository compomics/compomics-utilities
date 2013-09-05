/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.identification.protein_inference.proteintree.ProteinTree;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This class is an example on how to go through the protein tree
 *
 * @author Marc
 */
public class TestKenneth {

    /**
     * The path to your database
     */
    private String dbPath = "src/test/resources/experiment/testMarc/uniprot_reviewed_no-isoforms_05.06.13_concatenated_target_decoy.fasta";

    /**
     * Creates a species to digested peptides map (peptides are not necessarily
     * unique to a species)
     */
    public void testDigestion() throws FileNotFoundException, ClassNotFoundException, XmlPullParserException, IOException, InterruptedException {

        long time1 = System.currentTimeMillis();
        // Load the protein sequences
        File sequencesFile = new File(dbPath);
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequencesFile);

        // Set the enzyme settings
        EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
        File enzymeFile = new File("src/test/resources/experiment/enzymes.xml");
        enzymeFactory.importEnzymes(enzymeFile);
        ArrayList<Enzyme> enzymes = enzymeFactory.getEnzymes();
        Enzyme testEnzyme = enzymes.get(0);
        int nMissedCleavages = 2;
        int minPeptideSize = 6;
        int maxPeptideSize = 50;

        // Create a species to peptide map
        HashMap<String, ArrayList<String>> speciesToPeptideMap = new HashMap<String, ArrayList<String>>();
        int cpt = 0;
        for (String accession : sequenceFactory.getAccessions()) {
            String species = sequenceFactory.getHeader(accession).getTaxonomy();
            ArrayList<String> peptides = speciesToPeptideMap.get(species);
            if (peptides == null) {
                peptides = new ArrayList<String>();
                speciesToPeptideMap.put(species, peptides);
            }
            Protein protein = sequenceFactory.getProtein(accession);
            for (String peptide : testEnzyme.digest(protein.getSequence(), nMissedCleavages, minPeptideSize, maxPeptideSize)) {
                peptides.add(peptide);
            }
            if (++cpt % 5000 == 0) {
                System.out.println(cpt + " sequences of " + sequenceFactory.getAccessions().size() + " processed.");
            }
        }

        // Here you go
        int nSpecies = speciesToPeptideMap.size();
        System.out.println(nSpecies + " species found:");
        for (String species : speciesToPeptideMap.keySet()) {
            System.out.println(species + ": " + speciesToPeptideMap.get(species).size() + " peptides.");
        }

        long time2 = System.currentTimeMillis();
        long time = (time2 - time1) / 1000;
        System.out.println("Digestion time: " + time + "s.");
    }

    /**
     * Creates a species to unique peptide map
     */
    public void testProteinTree() throws FileNotFoundException, IOException, ClassNotFoundException, SQLException, InterruptedException {

        long time1 = System.currentTimeMillis();

        File sequencesFile = new File(dbPath);

        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        sequenceFactory.loadFastaFile(sequencesFile);

        ProteinTree proteinTree = new ProteinTree(1000); // note: the memory settings are calibrated for a node size of 500
        int tagSize;
        if (sequenceFactory.getNTargetSequences() < 100000) {
            tagSize = 3;
        } else {
            tagSize = 4;
        }
        int nodeSize = 1; // The maximal number of proteins you want for a given peptide
        int maxPeptideSize = 40; // The maximal number of amino acids allowed per peptide
        proteinTree.initiateTree(tagSize, nodeSize, maxPeptideSize, null, true);

        // Species to peptide mapping
        HashMap<String, ArrayList<String>> speciesToPeptideMap = new HashMap<String, ArrayList<String>>();

        ProteinTree.PeptideIterator iterator = proteinTree.getPeptideIterator();
        while (iterator.hasNext()) {
            // the peptide sequence
            String peptideSequence = (String) iterator.next();
            // Its protein mapping as a map: accession -> positions in the sequence
            HashMap<String, ArrayList<Integer>> proteinMapping = iterator.getMapping();
            // The number of mapped proteins
            int nProteins = proteinMapping.size();
            // check that it is a unique peptide.
            if (nProteins == 1) {
                String accession = proteinMapping.keySet().iterator().next();
                String species = sequenceFactory.getHeader(accession).getTaxonomy();
                ArrayList<String> peptides = speciesToPeptideMap.get(species);
                if (peptides == null) {
                    peptides = new ArrayList<String>();
                    speciesToPeptideMap.put(species, peptides);
                }
                peptides.add(peptideSequence);
            }
        }
        // here you are
        int nSpecies = speciesToPeptideMap.size();
        System.out.println(nSpecies + " species found:");
        for (String species : speciesToPeptideMap.keySet()) {
            System.out.println(species + ": " + speciesToPeptideMap.get(species).size() + " peptides.");
        }

        long time2 = System.currentTimeMillis();
        long time = (time2 - time1) / 1000;
        System.out.println("Tree iteration time: " + time + "s.");
    }
}
