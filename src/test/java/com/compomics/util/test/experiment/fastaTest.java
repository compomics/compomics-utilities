package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
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
    public void testPfu() {
    try {
    File fastaFile = new File("src/test/resources/experiment/uniprot_pfu_2261_03.08.2011.fasta");
    sequenceFactory.loadFastaFile(fastaFile);
    String sequence;
    EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    File enzymeFile = new File("src/test/resources/experiment/enzymes.xml");
    enzymeFactory.importEnzymes(enzymeFile);
    Enzyme enzyme = enzymeFactory.getEnzyme("Trypsin");
    int nMissedCleavages = 0;
    int nMin = 8;
    int nMax = 20;
    HashMap<String, ArrayList<String>> sequences = new HashMap<String, ArrayList<String>>();
    for (String proteinKey : sequenceFactory.getAccessions()) {
    sequence = sequenceFactory.getProtein(proteinKey).getSequence();
    for (String peptide : enzyme.digest(sequence, nMissedCleavages, nMin, nMax)) {
    if (!sequences.containsKey(peptide)) {
    sequences.put(peptide, new ArrayList<String>());
    }
    sequences.get(peptide).add(proteinKey);
    }
    }
    
    fastaFile = new File("src/test/resources/experiment/uniprot_pyrococcus_2260_03.08.2011_no_pfu.fasta");
    sequenceFactory.loadFastaFile(fastaFile);
    HashMap<String, ArrayList<String>> commonSequences = new HashMap<String, ArrayList<String>>();
    int percent = sequenceFactory.getAccessions().size() / 100;
    int cpt = 0, progress = 0;            
    for (String proteinKey : sequenceFactory.getAccessions()) {
    sequence = sequenceFactory.getProtein(proteinKey).getSequence();
    for (String peptide : enzyme.digest(sequence, nMissedCleavages, nMin, nMax)) {
    if (sequences.containsKey(peptide)) {
    if (!commonSequences.containsKey(proteinKey)) {
    commonSequences.put(proteinKey, new ArrayList<String>());
    }
    commonSequences.get(proteinKey).add(peptide);
    }
    }
    cpt++;
    if (cpt > percent * progress) {                    
    System.out.println(progress + "%");
    progress++;
    }
    }
    File outputFile = new File("src/test/resources/experiment/pyrococcus.txt");
    FileWriter f = new FileWriter(outputFile);
    BufferedWriter b = new BufferedWriter(f);
    for (String proteinKey : commonSequences.keySet()) {
    for (String peptide : commonSequences.get(proteinKey)) {
    b.write(proteinKey + "\t" + peptide + "\n");
    }
    }
    b.close();
    f.close();
    
    } catch (Exception e) {
    int debug = 1;
    }
    }
     **/
}
