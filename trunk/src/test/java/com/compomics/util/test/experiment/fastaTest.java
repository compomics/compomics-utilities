package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This test will try to load proteins from a fasta file
 *
 * @author Marc
 */
public class fastaTest extends TestCase {

    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();

    public void testImport() {
        try {
            File fastaFile = new File("src/test/resources/experiment/sgd.fasta");
            sequenceFactory.loadFastaFile(fastaFile);
            Assert.assertTrue(sequenceFactory.getNTargetSequences() == 0);
            Protein sgdProtein = sequenceFactory.getProtein("YPL008W_REVERSED");
            Assert.assertTrue(sgdProtein.isDecoy());
            Assert.assertTrue(sgdProtein.getSequence().equals("*RSNLSRMSFFKRTSSIVQHTTHESNISDQVWRSLKKRFNPRNYRVDLLYINAYDNAHRIARGVSQNVAKMCINEMFEKTARSAEEETGGSKMIKAALHKRKVILEGSFINPFPLGVMVVARCLDDQFNIGESLKGGVIALLLSGRGEAVSDSYGSLIDDGDKAEYFIKRVNNLTAFRDNQKWCQIVHALYQYSPFFAVIGGKKPVAKSLDVFFQFLHNNVLSPSMRKEFTFELEPQNTIYTQLNEKPIVHNCSLTTIDESPVEPLLNSLFESMPEMTGGALVVCKAQNLISEFPKSPELLMYKISYNKEFFFQGESTLNTLCYLFQSVKFLLPQSSVSKKHTEKIPNENKSSEEEKLAQNYTDIKYAIKSVKIYRLLKHINLTDINSGTFMDNPDIEQGIKKFNKVIFQILTMLLSNLKLLNVRNGPNLRSKFKNFYTVIGKHCNKLDELSIQSSYISNITEILNHAEDIIVISNELNIQLSSRTSESLLYQYPLTVVEAIPLAERSAYYPCIGLSKGLPVLDEIDQIESFIMDRLALTDPCHRWENTNQYFICGEKSHRLDACADNIAELTKWKMVKPNICLQKKSALPLYKVKEDPVKDRFSSPFSPLRLQSTFQGLQSYTRSAYYIKVPNQNTVDFRDGNNPDRSVKGDIKDLLTIIQSNLESLKYDKDSIRGGRTSKSTDNNESDSEYPRPIFDQEELSVDLHRAGKRKKRLPDVSKYRGHEKDLDCMTKLQKCSTTNIENLHKEYDNLLDVKEQLVSKRYTDIVWDPEDDSLNESDDENTKINTEMRTFIDAKNMRLWTMTACILSLTKGTGTPSELIAIKKGESLVRYVTEMLQVQIDYPKYPHYFTESYEKKDM"));

            fastaFile = new File("src/test/resources/experiment/uniprot.fasta");
            sequenceFactory.loadFastaFile(fastaFile);
            Assert.assertTrue(sequenceFactory.getNTargetSequences() == 1);
            Protein uniprotProtein = sequenceFactory.getProtein("P31946");
            Assert.assertTrue(!uniprotProtein.isDecoy());
            Assert.assertTrue(uniprotProtein.getSequence().equals("MTMDKSELVQKAKLAEQAERYDDMAAAMKAVTEQGHELSNEERNLLSVAYKNVVGARRSSWRVISSIEQKTERNEKKQQMGKEYREKIEAELQDICNDVLELLDKYLIPNATQPESKVFYLKMKGDYFRYLSEVASGDNKQTTVSNSQQAYQEAFEISKKEMQPTHPIRLGLALNFSVFYYEILNSPEKACSLAKTAFDEAIAELDTLNEESYKDSTLIMQLLRDNLTLWTSENQGDEGDAGEGEN"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
/**
    public void testJulia() {
        try {
            
            String fileName = "YPD_N-terminal_UP";
            
            ArrayList<String> peptides = new ArrayList<String>();
            HashMap<String, String> remarks = new HashMap<String, String>();
            File peptidesFile = new File("src/test/resources/experiment/" + fileName + ".txt");
            FileReader f = new FileReader(peptidesFile);
            BufferedReader b = new BufferedReader(f);
            String line;
            while ((line = b.readLine()) != null) {
                String[] input = line.split("\t");
                String peptide = input[0].trim();
                if (!peptide.equals("")) {
                    peptides.add(peptide);
                    remarks.put(peptide, input[1].trim());
                }
            }
            b.close();
            f.close();

            File fastaFile = new File("src/test/resources/experiment/merged_SGD20110203_UPS_12_adapted_20120201.fasta");
            sequenceFactory.loadFastaFile(fastaFile);
            String currentProteinSequence;
            char aa;
            HashMap<String, HashMap<String, HashMap<Integer, String>>> aaBefore = new HashMap<String, HashMap<String, HashMap<Integer, String>>>();
            HashMap<String, HashMap<String, HashMap<Integer, String>>> aaAfter = new HashMap<String, HashMap<String, HashMap<Integer, String>>>();
            for (String accession : sequenceFactory.getAccessions()) {
                currentProteinSequence = sequenceFactory.getProtein(accession).getSequence();
                for (String peptide : peptides) {
                    String tempSequence = currentProteinSequence;
                    while (tempSequence.lastIndexOf(peptide) >= 0) {
                    String subsequence = "";
                        int startIndex = tempSequence.lastIndexOf(peptide) - 5;
                        for (int index = startIndex; index < startIndex + 5; index++) {
                            if (index >= 0 && index < currentProteinSequence.length()) {
                                aa = currentProteinSequence.charAt(index);
                                if (aa != '*') {
                                    subsequence += aa;
                                }
                            }
                        }
                        if (!aaBefore.containsKey(accession)) {
                            aaBefore.put(accession, new HashMap<String, HashMap<Integer, String>>());
                        }
                        if (!aaBefore.get(accession).containsKey(peptide)) {
                            aaBefore.get(accession).put(peptide, new HashMap<Integer, String>());
                        }
                        aaBefore.get(accession).get(peptide).put(startIndex + 6, subsequence);
                        subsequence = "";
                        for (int index = startIndex + peptide.length() + 5; index < startIndex + peptide.length() + 10; index++) {
                            if (index >= 0 && index < currentProteinSequence.length()) {
                                aa = currentProteinSequence.charAt(index);
                                if (aa != '*') {
                                    subsequence += aa;
                                }
                            }
                        }
                        if (!aaAfter.containsKey(accession)) {
                            aaAfter.put(accession, new HashMap<String, HashMap<Integer, String>>());
                        }
                        if (!aaAfter.get(accession).containsKey(peptide)) {
                            aaAfter.get(accession).put(peptide, new HashMap<Integer, String>());
                        }
                        aaAfter.get(accession).get(peptide).put(startIndex + 6, subsequence);
                        tempSequence = currentProteinSequence.substring(0, startIndex + 6);
                    }
                }
            }

            System.out.println("writing results");
            File outputFile1 = new File("src/test/resources/experiment/" + fileName + " subsequences.txt");
            FileWriter fw1 = new FileWriter(outputFile1);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            bw1.write("Accession\tPeptide Start\tAA before\tPeptide\tAA after\tComment\n");
            ArrayList<Integer> indexes;
            for (String accession : aaBefore.keySet()) {
                for (String peptide : aaBefore.get(accession).keySet()) {
                    indexes = new ArrayList<Integer>(aaBefore.get(accession).get(peptide).keySet());
                    Collections.sort(indexes);
                    for (int index : indexes) {
                        bw1.write(accession + "\t" + index + "\t" + aaBefore.get(accession).get(peptide).get(index) + "\t" + peptide + "\t" + aaAfter.get(accession).get(peptide).get(index) + "\t" + remarks.get(peptide) + "\n");
                    }
                }
            }
            bw1.close();
            fw1.close();

        } catch (Exception e) {
            int debug = 1;
        }
    }
    /**
     * public void testComparison() { try { System.out.println("Importing
     * archaea peptides"); File fastaFile = new
     * File("src/test/resources/experiment/uniprot Archaea [2157]
     * 25.7.2011.fasta"); sequenceFactory.loadFastaFile(fastaFile); String
     * sequence; EnzymeFactory enzymeFactory = EnzymeFactory.getInstance(); File
     * enzymeFile = new File("src/test/resources/experiment/enzymes.xml");
     * enzymeFactory.importEnzymes(enzymeFile); Enzyme enzyme =
     * enzymeFactory.getEnzyme("Trypsin"); int nMissedCleavages = 0; int nMin =
     * 8; int nMax = 20; ArrayList<String> humanSequences = new
     * ArrayList<String>(); for (String proteinKey :
     * sequenceFactory.getAccessions()) { sequence =
     * sequenceFactory.getProtein(proteinKey).getSequence(); for (String peptide
     * : enzyme.digest(sequence, nMissedCleavages, nMin, nMax)) { sequence =
     * peptide.replace("L", "I"); if (!humanSequences.contains(sequence)) {
     * humanSequences.add(sequence); } } } System.out.println("archaea:" +
     * humanSequences.size());
     *
     * System.out.println("importing yeast peptides"); fastaFile = new
     * File("src/test/resources/experiment/ABRF_yeast.fasta");
     * sequenceFactory.loadFastaFile(fastaFile); ArrayList<String>
     * yeastSequences = new ArrayList<String>(); for (String proteinKey :
     * sequenceFactory.getAccessions()) { sequence =
     * sequenceFactory.getProtein(proteinKey).getSequence(); for (String peptide
     * : enzyme.digest(sequence, nMissedCleavages, nMin, nMax)) { sequence =
     * peptide.replace("L", "I"); if (!yeastSequences.contains(sequence)) {
     * yeastSequences.add(sequence); } } } System.out.println("ABRF_yeast:" +
     * yeastSequences.size());
     *
     * System.out.println("comparing peptides"); ArrayList<String>
     * commonSequences = new ArrayList<String>(); for (String peptide :
     * humanSequences) { if (yeastSequences.contains(peptide)) {
     * commonSequences.add(peptide); } }
     *
     * System.out.println("writing results"); File outputFile = new
     * File("src/test/resources/experiment/archaea-yeast common.txt");
     * FileWriter f = new FileWriter(outputFile); BufferedWriter b = new
     * BufferedWriter(f); for (String peptide : commonSequences) {
     * b.write(peptide + "\n"); } b.close(); f.close();
     *
     * } catch (Exception e) { e.printStackTrace(); int debug = 1; } } /**
     * public void testPfu() { try { File fastaFile = new
     * File("src/test/resources/experiment/uniprot_sprot_101104_human.fasta");
     * sequenceFactory.loadFastaFile(fastaFile); String sequence; EnzymeFactory
     * enzymeFactory = EnzymeFactory.getInstance(); File enzymeFile = new
     * File("src/test/resources/experiment/enzymes.xml");
     * enzymeFactory.importEnzymes(enzymeFile); Enzyme enzyme =
     * enzymeFactory.getEnzyme("Trypsin"); int nMissedCleavages = 0; int nMin =
     * 8; int nMax = 20; int nPep = 0; ArrayList<String> sequences = new
     * ArrayList<String>(); for (String proteinKey :
     * sequenceFactory.getAccessions()) { sequence =
     * sequenceFactory.getProtein(proteinKey).getSequence();
     * sequences.addAll(enzyme.digest(sequence, nMissedCleavages, nMin, nMax));
     * } Collections.sort(sequences); String tempString = ""; int nAA;
     * HashMap<Integer, Integer> sizes = new HashMap<Integer, Integer>();
     * HashMap<String, Integer> aaFrequencies = new HashMap<String, Integer>();
     * for (String tempSequence : sequences) { if
     * (!tempSequence.equals(tempString)) { nPep++; tempString = tempSequence;
     * nAA = tempSequence.length(); if (!sizes.containsKey(nAA)) {
     * sizes.put(nAA, 0); } sizes.put(nAA, sizes.get(nAA) + 1); for (String aa :
     * tempSequence.split("")) { if (!aaFrequencies.containsKey(aa)) {
     * aaFrequencies.put(aa, 0); } aaFrequencies.put(aa,
     * aaFrequencies.get(aa)+1); } } }
     *
     * File outputFile = new
     * File("src/test/resources/experiment/uniprot_sprot_101104_human
     * stats.txt");
     *
     * String toWrite = "uniprot_sprot_101104_human " + nPep + " peptides\n";
     * toWrite += "size\tn\n"; for (int n : sizes.keySet()) { toWrite += n +
     * "\t" + sizes.get(n) + "\n"; } toWrite += "AA\tn\n"; for (String aa :
     * aaFrequencies.keySet()) { toWrite += aa + "\t" + aaFrequencies.get(aa) +
     * "\n"; } Writer fileWriter = new BufferedWriter(new
     * FileWriter(outputFile)); fileWriter.write(toWrite); fileWriter.close(); }
     * catch (Exception e) { int debug = 1; } }*
     */
}
