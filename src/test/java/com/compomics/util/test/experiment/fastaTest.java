package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceFactory;
import java.io.File;
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
}
