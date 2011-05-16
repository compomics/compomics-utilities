package com.compomics.util.test.experiment;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.SequenceDataBase;
import java.io.File;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This test will try to load proteins from a fasta file 
 *
 * @author Marc
 */
public class fastaTest extends TestCase {

    public void testImport() {
        try {
            File fastaFile = new File("src/test/resources/experiment/sgd.fasta");
            SequenceDataBase db = new SequenceDataBase("SGD", "test");
            db.importDataBase(fastaFile);
            Assert.assertTrue(db.getNumberOfTargetSequences() == 0);
            Protein sgdProtein = db.getProtein("YPL008W_REVERSED");
            Assert.assertTrue(sgdProtein.isDecoy());
            Assert.assertTrue(sgdProtein.getSequence().equals("*RSNLSRMSFFKRTSSIVQHTTHESNISDQVWRSLKKRFNPRNYRVDLLYINAYDNAHRIARGVSQNVAKMCINEMFEKTARSAEEETGGSKMIKAALHKRKVILEGSFINPFPLGVMVVARCLDDQFNIGESLKGGVIALLLSGRGEAVSDSYGSLIDDGDKAEYFIKRVNNLTAFRDNQKWCQIVHALYQYSPFFAVIGGKKPVAKSLDVFFQFLHNNVLSPSMRKEFTFELEPQNTIYTQLNEKPIVHNCSLTTIDESPVEPLLNSLFESMPEMTGGALVVCKAQNLISEFPKSPELLMYKISYNKEFFFQGESTLNTLCYLFQSVKFLLPQSSVSKKHTEKIPNENKSSEEEKLAQNYTDIKYAIKSVKIYRLLKHINLTDINSGTFMDNPDIEQGIKKFNKVIFQILTMLLSNLKLLNVRNGPNLRSKFKNFYTVIGKHCNKLDELSIQSSYISNITEILNHAEDIIVISNELNIQLSSRTSESLLYQYPLTVVEAIPLAERSAYYPCIGLSKGLPVLDEIDQIESFIMDRLALTDPCHRWENTNQYFICGEKSHRLDACADNIAELTKWKMVKPNICLQKKSALPLYKVKEDPVKDRFSSPFSPLRLQSTFQGLQSYTRSAYYIKVPNQNTVDFRDGNNPDRSVKGDIKDLLTIIQSNLESLKYDKDSIRGGRTSKSTDNNESDSEYPRPIFDQEELSVDLHRAGKRKKRLPDVSKYRGHEKDLDCMTKLQKCSTTNIENLHKEYDNLLDVKEQLVSKRYTDIVWDPEDDSLNESDDENTKINTEMRTFIDAKNMRLWTMTACILSLTKGTGTPSELIAIKKGESLVRYVTEMLQVQIDYPKYPHYFTESYEKKDM"));

            fastaFile = new File("src/test/resources/experiment/uniprot.fasta");
            db = new SequenceDataBase("uniprot", "test");
            db.importDataBase(fastaFile);
            Assert.assertTrue(db.getNumberOfTargetSequences() == 1);
            Protein uniprotProtein = db.getProtein("P31946");
            Assert.assertTrue(!uniprotProtein.isDecoy());
            Assert.assertTrue(uniprotProtein.getSequence().equals("MTMDKSELVQKAKLAEQAERYDDMAAAMKAVTEQGHELSNEERNLLSVAYKNVVGARRSSWRVISSIEQKTERNEKKQQMGKEYREKIEAELQDICNDVLELLDKYLIPNATQPESKVFYLKMKGDYFRYLSEVASGDNKQTTVSNSQQAYQEAFEISKKEMQPTHPIRLGLALNFSVFYYEILNSPEKACSLAKTAFDEAIAELDTLNEESYKDSTLIMQLLRDNLTLWTSENQGDEGDAGEGEN"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
