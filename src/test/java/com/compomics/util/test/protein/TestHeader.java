/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 7-okt-02
 * Time: 16:46:16
 */
package com.compomics.util.test.protein;

import com.compomics.util.protein.Header;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

/*
 * CVS information:
 *
 * $Revision: 1.18 $
 * $Date: 2008/09/08 16:10:54 $
 */

/**
 * This class implements all test scenarios for the Header class.
 *
 * @author Lennart Martens
 * @see com.compomics.util.protein.Header
 */
public class TestHeader extends TestCase {

    // Class specific log4j logger for TestHeader instances.
    Logger logger = Logger.getLogger(TestHeader.class);

    public TestHeader() {
        this("This is the test scenario for the Header class.");
    }

    public TestHeader(String aName) {
        super(aName);
    }

    /**
     * This method test the creation of a Header instance for various
     * header types.
     */
    public void testHeaderCreation() {

        final String unknown = ">Unknown header type.";
        final String spStandard = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String spWrong = ">sw|O95229 ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String nrStandard = ">gi|20149565|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]";
        final String nrSPRef = ">gi|21542145|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)";
        final String nrSimple = ">gi|20545032|hypothetical protein XP_51234155 [Homo Sapiens]";
        final String nrWrong = ">gi|20149565 ref NP_004878.2 small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]";
        final String spFASTA = ">K1CI_HUMAN (P35527) Keratin, type I cytoskeletal 9";
        final String ipiStandard = ">IPI:IPI00232014.1|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976";
        final String halobacterium = ">OE1007R (OE1007R) [del] Predicted orf (overlaps another ORF)";
        final String chlamydia = ">C.tr_L2_353 [492222 - 493658] | Chlamydia trachomatis LGV2";
        final String chlamydia2 = ">C_trachomatis_L2_1 [1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]";
        final String mycobacterium = ">M. tub.H37Rv|Rv1963c|Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R";
        final String drosophila = ">CG11023-PA pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA";
        final String sgd = ">YHR159W YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"";
        final String generic = ">NP0002A (NP0002A) hypothetical protein";
        final String spFASTA90 = ">P19084|11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)";
        final String spFASTA90_RAT = ">A1L1J6|ZN652_RAT Zinc finger protein 652 - Rattus norvegicus (Rat).";
        final String dm = ">dm345_3L-sense [234353534-234353938]";
        final String tair = ">AT1G08520.1 | Symbol: PDE166 | magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum) | chr1:2696415-2700961 FORWARD | Aliases: T27G7.20, T27G7_20, PDE166, PIGMENT DEFECTIVE 166";
        final String hinv = ">HIT000000001.10|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.";
        final String nrAt = ">nrAt0.2_1 \t(TR:Q8HT11_ARATH) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).";
        final String listeria = ">L. monocytogenes EGD-e|LMO02333|'comK: 158 aa - competence transcription factor (C-terminal part)";
        final String spSep2008 = ">sp|A7GKH8|PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1";
        final String trSep2008 = ">tr|Q8KFF3|Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1";
        final String ensemblGenomes = ">en|CBW20588|Chromosome:4847047-4849455 fimbrial usher protein";
        final String flybase = ">FBpp0071678 type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;";
        final String nextProt = ">nxp|NX_P02768-1|ALB|Serum albumin|Iso 1";
        final String uniRef = ">UniRef100_U3PVA8 Protein IroK n=22 Tax=Escherichia coli RepID=IROK_ECOL";

        // @TODO: extend with the newly added header types!!!
        
        // First of all, two trivial cases: 'null' and empty String.
        Header h = Header.parseFromFASTA(null);
        Assert.assertTrue(h == null);

        h = Header.parseFromFASTA("");
        Assert.assertEquals("", h.getRest());
        Assert.assertEquals(">", h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">", h.toString());

        h = Header.parseFromFASTA("             ");
        Assert.assertEquals("", h.getRest());
        Assert.assertEquals(">", h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">", h.toString());


        // Now test the unidentified one.
        h = Header.parseFromFASTA(unknown);
        Assert.assertTrue(h.getID() == null);
        Assert.assertTrue(h.getAccession() == null);
        Assert.assertTrue(h.getDescription() == null);
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() != null);
        Assert.assertEquals(unknown.substring(1), h.getRest());

        Assert.assertEquals(unknown, h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(unknown, h.toString());


        // Next do the SwissProt standard.
        h = Header.parseFromFASTA(spStandard);
        Assert.assertEquals("sw", h.getID());
        Assert.assertEquals("O95229", h.getAccession());
        Assert.assertEquals("ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        Assert.assertEquals(spStandard, h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(spStandard, h.toString());

        // Next on the list is the SwissProt with the error in it.
        try {
            Header.parseFromFASTA(spWrong);
            fail("Invalid SwissProt header '" + spWrong + "' was not recognized as such by the Header class!");
        } catch(IllegalArgumentException iae) {
            // Perfect. Do nothing.
        }

        // Now do the Expasy SP FASTA header.
        h = Header.parseFromFASTA(spFASTA);
        Assert.assertEquals("sw", h.getID());
        Assert.assertEquals("P35527", h.getAccession());
        Assert.assertEquals("K1CI_HUMAN Keratin, type I cytoskeletal 9", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        Assert.assertEquals(">sw|P35527|K1CI_HUMAN Keratin, type I cytoskeletal 9", h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">sw|P35527|K1CI_HUMAN Keratin, type I cytoskeletal 9", h.toString());

        // IPI up next.
        // IPI:IPI00232014.1|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976
        h = Header.parseFromFASTA(ipiStandard);
        Assert.assertEquals("IPI", h.getID());
        Assert.assertEquals("IPI00232014.1", h.getAccession());
        Assert.assertEquals("REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        Assert.assertEquals(">IPI|IPI00232014.1|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976", h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">IPI|IPI00232014.1|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976", h.toString());

        // Halobacterium up next.
        h = Header.parseFromFASTA(halobacterium);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("OE1007R", h.getAccession());
        Assert.assertEquals("(OE1007R) [del] Predicted orf (overlaps another ORF)", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        Assert.assertEquals(">OE1007R (OE1007R) [del] Predicted orf (overlaps another ORF)", h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">OE1007R (OE1007R) [del] Predicted orf (overlaps another ORF)", h.toString());

        // Now the Chlamydia trachomatis headers.
        h = Header.parseFromFASTA(chlamydia);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("C.tr_L2_353", h.getAccession());
        Assert.assertEquals("[492222 - 493658] | Chlamydia trachomatis LGV2", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);
        h = Header.parseFromFASTA(chlamydia2);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("C_trachomatis_L2_1", h.getAccession());
        Assert.assertEquals("[1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);


        // The mycobacterium tuberculosis header.
        h = Header.parseFromFASTA(mycobacterium);
        Assert.assertEquals("M. tub.H37Rv", h.getID());
        Assert.assertEquals("Rv1963c", h.getAccession());
        Assert.assertEquals("Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // The drosophila header.
        h = Header.parseFromFASTA(drosophila);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("CG11023-PA", h.getAccession());
        Assert.assertEquals("pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // The SGD header.
        h = Header.parseFromFASTA(sgd);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("YHR159W", h.getAccession());
        Assert.assertEquals("YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // New SP FASTA header.
        h = Header.parseFromFASTA(spFASTA90);
        Assert.assertEquals("sw", h.getID());
        Assert.assertEquals("P19084", h.getAccession());
        Assert.assertEquals("11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // New SP FASTA header with atypical start 'A' from RAT species.
        h = Header.parseFromFASTA(spFASTA90_RAT);
        Assert.assertEquals("sw", h.getID());
        Assert.assertEquals("A1L1J6", h.getAccession());
        Assert.assertEquals("ZN652_RAT Zinc finger protein 652 - Rattus norvegicus (Rat).", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);


        // Move on to the generic one.
        h = Header.parseFromFASTA(generic);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("NP0002A", h.getAccession());
        Assert.assertEquals("(NP0002A) hypothetical protein", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Now test a personal dm protein header.
        h = Header.parseFromFASTA(dm);
        Assert.assertEquals("dm345_3L-sense", h.getID());
        Assert.assertEquals(234353534 , h.getStartLocation());
        Assert.assertEquals(234353938, h.getEndLocation());
        Assert.assertEquals("dm345_3L-sense", h.getAccession());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Now test a TAIR protein header.
        h = Header.parseFromFASTA(tair);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals(-1, h.getStartLocation());
        Assert.assertEquals(-1, h.getEndLocation());
        Assert.assertEquals("AT1G08520.1", h.getAccession());
        Assert.assertEquals("magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum)", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Now test a H-Inv protein header.
        h = Header.parseFromFASTA(hinv);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals(-1, h.getStartLocation());
        Assert.assertEquals(-1, h.getEndLocation());
        Assert.assertEquals("HIT000000001.10", h.getAccession());
        Assert.assertEquals("HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Now test a nrAt protein header.
        h = Header.parseFromFASTA(nrAt);
        Assert.assertEquals("nrAt0.2_1", h.getID());
        Assert.assertEquals(-1, h.getStartLocation());
        Assert.assertEquals(-1, h.getEndLocation());
        Assert.assertEquals("TR:Q8HT11_ARATH", h.getAccession());
        Assert.assertEquals("Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Now test a listeria protein header.
        h = Header.parseFromFASTA(listeria);
        Assert.assertEquals("L. monocytogenes EGD-e", h.getID());
        Assert.assertEquals(-1, h.getStartLocation());
        Assert.assertEquals(-1, h.getEndLocation());
        Assert.assertEquals("LMO02333", h.getAccession());
        Assert.assertEquals("'comK: 158 aa - competence transcription factor (C-terminal part)", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Now test a SP > Sep 2008 protein header.
        h = Header.parseFromFASTA(spSep2008);
        Assert.assertEquals("sp", h.getID());
        Assert.assertEquals(-1, h.getStartLocation());
        Assert.assertEquals(-1, h.getEndLocation());
        Assert.assertEquals("A7GKH8", h.getAccession());
        Assert.assertEquals("PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // New TrEMBL FASTA header.
        h = Header.parseFromFASTA(trSep2008);
        Assert.assertEquals("tr", h.getID());
        Assert.assertEquals("Q8KFF3", h.getAccession());
        Assert.assertEquals("Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Ensembl Genomes FASTA header.
        h = Header.parseFromFASTA(ensemblGenomes);
        Assert.assertEquals("en", h.getID());
        Assert.assertEquals("CBW20588", h.getAccession());
        Assert.assertEquals("Chromosome:4847047-4849455 fimbrial usher protein", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Flybase header.
        h = Header.parseFromFASTA(flybase);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("FBpp0071678", h.getAccession());
        Assert.assertEquals("type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // nextProt header
        h = Header.parseFromFASTA(nextProt);
        Assert.assertEquals("nxp", h.getID());
        Assert.assertEquals("NX_P02768-1", h.getAccession());
        Assert.assertEquals("Serum albumin|Iso 1", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);
        Assert.assertEquals(h.getGeneName(), "ALB");

        // UniRef header
        h = Header.parseFromFASTA(uniRef);
        Assert.assertEquals("", h.getID());
        Assert.assertEquals("UniRef100_U3PVA8", h.getAccession());
        Assert.assertEquals("Protein IroK n=22 Tax=Escherichia coli RepID=IROK_ECOL", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        // Now do the standard NCBI header.
        h = Header.parseFromFASTA(nrStandard);
        Assert.assertEquals("gi", h.getID());
        Assert.assertEquals("20149565", h.getAccession());
        Assert.assertEquals("small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]", h.getDescription());
        Assert.assertEquals("NP_004878.2", h.getForeignAccession());
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertEquals("ref", h.getForeignID());
        Assert.assertTrue(h.getRest() == null);

        Assert.assertEquals(nrStandard, h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(nrStandard, h.toString());

        // OK, try an NCBI header with a SP reference.
        h = Header.parseFromFASTA(nrSPRef);
        Assert.assertEquals("gi", h.getID());
        Assert.assertEquals("21542145", h.getAccession());
        Assert.assertEquals("Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)", h.getDescription());
        Assert.assertEquals("Q9ULX9", h.getForeignAccession());
        Assert.assertEquals("MAFF_HUMAN", h.getForeignDescription());
        Assert.assertEquals("sp", h.getForeignID());
        Assert.assertTrue(h.getRest() == null);

        Assert.assertEquals(nrSPRef, h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(nrSPRef, h.toString());

        // Now the simple NCBI header
        h = Header.parseFromFASTA(nrSimple);
        Assert.assertEquals("gi", h.getID());
        Assert.assertEquals("20545032", h.getAccession());
        Assert.assertEquals("hypothetical protein XP_51234155 [Homo Sapiens]", h.getDescription());
        Assert.assertTrue(h.getForeignAccession() == null);
        Assert.assertTrue(h.getForeignDescription() == null);
        Assert.assertTrue(h.getForeignID() == null);
        Assert.assertTrue(h.getRest() == null);

        Assert.assertEquals(">gi|20545032| hypothetical protein XP_51234155 [Homo Sapiens]", h.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">gi|20545032| hypothetical protein XP_51234155 [Homo Sapiens]", h.toString());

        // Finally, do the wrong NCBI header.
         try {
            Header.parseFromFASTA(nrWrong);
            fail("Invalid NCBI header '" + nrWrong + "' was not recognized as such by the Header class!");
        } catch(IllegalArgumentException iae) {
            // Perfect. Do nothing.
        }
    }

    /**
     * This method test the scoring behaviour of the Header class.
     */
    public void testScoring() {
        final String unknown = ">Unknown header type.";
        final String spStandard = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String ipiSP = ">IPI:IPI00029695.2|UniProt/Swiss-Prot:Q12824-1|REFSEQ_NP:NP_003064|ENSEMBL:ENSP00000263121 Tax_Id=9606 Splice isoform A of Q12824 SWI/SNF related, matrix associated, actin dependent regulator of chromatin subfamily B member 1";
        final String ipiStandard1 = ">IPI:IPI00008879.1|UniProt/TrEMBL:Q9HD05|ENSEMBL:ENSP00000324580 Tax_Id=9606 DJ831C21.3";
        final String ipiStandard2 = ">IPI:IPI00014903.1|REFSEQ_NP:NP_056448|ENSEMBL:ENSP00000229395 Tax_Id=9606 Hypothetical protein FLJ10672";
        final String ipiNone = ">IPI:IPI00373784.3|REFSEQ_XP:XP_208993|ENSEMBL:ENSP00000293443 Tax_Id=9606 PREDICTED: Similar to hypothetical gene supported by AL050367";
        final String nrStandard = ">gi|20149565|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]";
        final String nrSPRef = ">gi|21542145|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)";
        final String nrSimple = ">gi|20545032 (95-98)|hypothetical protein XP_51234155 [Homo Sapiens]";
        final String spExpasy = ">K1CI_HUMAN (P35527) Keratin, type I cytoskeletal 9";
        final String halobacterium = ">OE1007R (OE1007R) [del] Predicted orf (overlaps another ORF)";
        final String chlamydia = ">C.tr_L2_353 [492222 - 493658] | Chlamydia trachomatis LGV2";
        final String chlamydia2 = ">C_trachomatis_L2_1 [1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]";
        final String mycobacterium = "> M. tub.H37Rv|Rv1963c|Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R";
        final String drosophila = ">CG11023-PA pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA";
        final String sgd = ">YHR159W YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"";
        final String spFASTA90 = ">P19084|11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)";
        final String generic = ">NP0002A (NP0002A) hypothetical protein";
        final String tair = ">AT1G08520.1 | Symbol: PDE166 | magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum) | chr1:2696415-2700961 FORWARD | Aliases: T27G7.20, T27G7_20, PDE166, PIGMENT DEFECTIVE 166";
        final String hinv = ">HIT000000001.10|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.";
        final String nrAt = ">nrAt0.2_1\t (TR:Q8HT11_ARATH) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).";
        final String listeria = ">L. monocytogenes EGD-e|LMO02333|'comK: 158 aa - competence transcription factor (C-terminal part)";
        final String spSep2008 = ">sp|A7GKH8|PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1";
        final String trSep2008 = ">tr|Q8KFF3|Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1";
        final String ensemblGenomes = ">en|CBW20588|Chromosome:4847047-4849455 fimbrial usher protein";
        final String flybase = ">FBpp0071678 type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;";

        Assert.assertEquals(0, Header.parseFromFASTA(unknown).getScore());
        Assert.assertEquals(4, Header.parseFromFASTA(spStandard).getScore());
        Assert.assertEquals(3, Header.parseFromFASTA(ipiSP).getScore());
        Assert.assertEquals(2, Header.parseFromFASTA(ipiStandard1).getScore());
        Assert.assertEquals(2, Header.parseFromFASTA(ipiStandard2).getScore());
        Assert.assertEquals(1, Header.parseFromFASTA(ipiNone).getScore());
        Assert.assertEquals(1, Header.parseFromFASTA(nrStandard).getScore());
        Assert.assertEquals(1, Header.parseFromFASTA(nrSimple).getScore());
        Assert.assertEquals(2, Header.parseFromFASTA(nrSPRef).getScore());
        Assert.assertEquals(4, Header.parseFromFASTA(spExpasy).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(halobacterium).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(chlamydia).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(chlamydia2).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(mycobacterium).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(drosophila).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(sgd).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(tair).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(hinv).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(nrAt).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(listeria).getScore());
        Assert.assertEquals(4, Header.parseFromFASTA(spFASTA90).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(generic).getScore());
        Assert.assertEquals(4, Header.parseFromFASTA(spSep2008).getScore());
        Assert.assertEquals(2, Header.parseFromFASTA(trSep2008).getScore());
        Assert.assertEquals(3, Header.parseFromFASTA(ensemblGenomes).getScore());
        Assert.assertEquals(0, Header.parseFromFASTA(flybase).getScore());
    }

    /**
     * This method test the reporting on the core part of the headers.
     */
    public void testCoreHeader() {
        final String unknown = ">Unknown header type.";
        final String spStandard = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String ipiStandard = ">IPI:IPI00232014.1|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976";
        final String nrStandard = ">gi|20149565|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]";
        final String nrSPRef = ">gi|21542145|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)";
        final String nrSimple = ">gi|20545032|hypothetical protein XP_51234155 [Homo Sapiens]";
        final String spExpasy = ">K1CI_HUMAN (P35527) Keratin, type I cytoskeletal 9";
        final String halobacterium = ">OE1007R (OE1007R) [del] Predicted orf (overlaps another ORF)";
        final String chlamydia = ">C.tr_L2_353 [492222 - 493658] | Chlamydia trachomatis LGV2";
        final String chlamydia2 = ">C_trachomatis_L2_1 [1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]";
        final String mycobacterium = ">M. tub.H37Rv|Rv1963c|Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R";
        final String drosophila = ">CG11023-PA pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA";
        final String sgd = ">YHR159W YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"";
        final String spFASTA90 = ">P19084|11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)";
        final String generic = ">NP0002A (NP0002A) hypothetical protein";
        final String tair = ">AT1G08520.1 | Symbol: PDE166 | magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum) | chr1:2696415-2700961 FORWARD | Aliases: T27G7.20, T27G7_20, PDE166, PIGMENT DEFECTIVE 166";
        final String hinv = ">HIT000000001.10|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.";
        final String nrAt = ">nrAt0.2_1\t (TR:Q8HT11_ARATH) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).";
        final String listeria = ">L. monocytogenes EGD-e|LMO02333|'comK: 158 aa - competence transcription factor (C-terminal part)";
        final String spSep2008 = ">sp|A7GKH8|PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1";
        final String trSep2008 = ">tr|Q8KFF3|Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1";
        final String ensemblGenomes = ">en|CBW20588|Chromosome:4847047-4849455 fimbrial usher protein";
        final String flybase = ">FBpp0071678 type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;";

        Assert.assertEquals(unknown.substring(1), Header.parseFromFASTA(unknown).getCoreHeader());
        Assert.assertEquals("sw|O95229", Header.parseFromFASTA(spStandard).getCoreHeader());
        Assert.assertEquals("IPI|IPI00232014.1", Header.parseFromFASTA(ipiStandard).getCoreHeader());
        Assert.assertEquals("gi|20149565", Header.parseFromFASTA(nrStandard).getCoreHeader());
        Assert.assertEquals("gi|21542145", Header.parseFromFASTA(nrSPRef).getCoreHeader());
        Assert.assertEquals("gi|20545032", Header.parseFromFASTA(nrSimple).getCoreHeader());
        Assert.assertEquals("sw|P35527", Header.parseFromFASTA(spExpasy).getCoreHeader());
        Assert.assertEquals("OE1007R", Header.parseFromFASTA(halobacterium).getCoreHeader());
        Assert.assertEquals("C.tr_L2_353", Header.parseFromFASTA(chlamydia).getCoreHeader());
        Assert.assertEquals("C_trachomatis_L2_1", Header.parseFromFASTA(chlamydia2).getCoreHeader());
        Assert.assertEquals("M. tub.H37Rv|Rv1963c", Header.parseFromFASTA(mycobacterium).getCoreHeader());
        Assert.assertEquals("CG11023-PA", Header.parseFromFASTA(drosophila).getCoreHeader());
        Assert.assertEquals("YHR159W", Header.parseFromFASTA(sgd).getCoreHeader());
        Assert.assertEquals("AT1G08520.1", Header.parseFromFASTA(tair).getCoreHeader());
        Assert.assertEquals("HIT000000001.10", Header.parseFromFASTA(hinv).getCoreHeader());
        Assert.assertEquals("sw|P19084", Header.parseFromFASTA(spFASTA90).getCoreHeader());
        Assert.assertEquals("NP0002A", Header.parseFromFASTA(generic).getCoreHeader());
        Assert.assertEquals("nrAt0.2_1 \t(TR:Q8HT11_ARATH)", Header.parseFromFASTA(nrAt).getCoreHeader());
        Assert.assertEquals("L. monocytogenes EGD-e|LMO02333", Header.parseFromFASTA(listeria).getCoreHeader());
        Assert.assertEquals("sp|A7GKH8", Header.parseFromFASTA(spSep2008).getCoreHeader());
        Assert.assertEquals("tr|Q8KFF3", Header.parseFromFASTA(trSep2008).getCoreHeader());
        Assert.assertEquals("en|CBW20588", Header.parseFromFASTA(ensemblGenomes).getCoreHeader());
        Assert.assertEquals("FBpp0071678", Header.parseFromFASTA(flybase).getCoreHeader());
    }

    /**
     * This method test all functionality related to addenda.
     */
    public void testAddenda() {
        final String unknown = ">Unknown header type.";
        final String spStandard = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String nrStandard = ">gi|20149565|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]";
        final String nrSPRef = ">gi|21542145|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)";
        final String nrSimple = ">gi|20545032|hypothetical protein XP_51234155 [Homo Sapiens]";
        final String ipiStandard = ">IPI:IPI00232014.1|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976";
        final String halobacterium = ">OE1007R (OE1007R) [del] Predicted orf (overlaps another ORF)";
        final String chlamydia = ">C.tr_L2_353 [492222 - 493658] | Chlamydia trachomatis LGV2";
        final String chlamydia2 = ">C_trachomatis_L2_1 [1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]";
        final String mycobacterium = "> M. tub.H37Rv|Rv1963c|Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R";
        final String drosophila = ">CG11023-PA pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA";
        final String sgd = ">YHR159W YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"";
        final String spFASTA90 = ">P19084|11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)";
        final String generic = ">NP0002A (NP0002A) hypothetical protein";
        final String tair = ">AT1G08520.1 | Symbol: PDE166 | magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum) | chr1:2696415-2700961 FORWARD | Aliases: T27G7.20, T27G7_20, PDE166, PIGMENT DEFECTIVE 166";
        final String hinv = ">HIT000000001.10|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.";
        final String nrAt = ">nrAt0.2_1\t (TR:Q8HT11_ARATH) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).";
        final String listeria = ">L. monocytogenes EGD-e|LMO02333|'comK: 158 aa - competence transcription factor (C-terminal part)";
        final String spSep2008 = ">sp|A7GKH8|PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1";
        final String trSep2008 = ">tr|Q8KFF3|Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1";
        final String ensemblGenomes = ">en|CBW20588|Chromosome:4847047-4849455 fimbrial usher protein";
        final String flybase = ">FBpp0071678 type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;";

        // First the absence of addenda.
        Header h1 = Header.parseFromFASTA(unknown);
        Header h2 = Header.parseFromFASTA(spStandard);
        Header h3 = Header.parseFromFASTA(nrStandard);
        Header h4 = Header.parseFromFASTA(nrSPRef);
        Header h5 = Header.parseFromFASTA(nrSimple);
        Header h6 = Header.parseFromFASTA(ipiStandard);
        Header h7 = Header.parseFromFASTA(halobacterium);
        Header h8 = Header.parseFromFASTA(generic);
        Header h9 = Header.parseFromFASTA(chlamydia);
        Header h9bis = Header.parseFromFASTA(chlamydia2);
        Header h10 = Header.parseFromFASTA(mycobacterium);
        Header h11 = Header.parseFromFASTA(drosophila);
        Header h12 = Header.parseFromFASTA(sgd);
        Header h13 = Header.parseFromFASTA(spFASTA90);
        Header h14 = Header.parseFromFASTA(tair);
        Header h15 = Header.parseFromFASTA(hinv);
        Header h16 = Header.parseFromFASTA(nrAt);
        Header h17 = Header.parseFromFASTA(listeria);
        Header h18 = Header.parseFromFASTA(spSep2008);
        Header h19 = Header.parseFromFASTA(trSep2008);
        Header h20 = Header.parseFromFASTA(ensemblGenomes);
        Header h21 = Header.parseFromFASTA(flybase);

        Assert.assertFalse(h1.hasAddenda());
        Assert.assertFalse(h2.hasAddenda());
        Assert.assertFalse(h3.hasAddenda());
        Assert.assertFalse(h4.hasAddenda());
        Assert.assertFalse(h5.hasAddenda());
        Assert.assertFalse(h6.hasAddenda());
        Assert.assertFalse(h7.hasAddenda());
        Assert.assertFalse(h8.hasAddenda());
        Assert.assertFalse(h9.hasAddenda());
        Assert.assertFalse(h9bis.hasAddenda());
        Assert.assertFalse(h10.hasAddenda());
        Assert.assertFalse(h11.hasAddenda());
        Assert.assertFalse(h12.hasAddenda());
        Assert.assertFalse(h13.hasAddenda());
        Assert.assertFalse(h14.hasAddenda());
        Assert.assertFalse(h15.hasAddenda());
        Assert.assertFalse(h16.hasAddenda());
        Assert.assertFalse(h17.hasAddenda());
        Assert.assertFalse(h18.hasAddenda());
        Assert.assertFalse(h19.hasAddenda());
        Assert.assertFalse(h20.hasAddenda());
        Assert.assertFalse(h21.hasAddenda());

        Assert.assertTrue(h1.getAddenda() == null);
        Assert.assertTrue(h2.getAddenda() == null);
        Assert.assertTrue(h3.getAddenda() == null);
        Assert.assertTrue(h4.getAddenda() == null);
        Assert.assertTrue(h5.getAddenda() == null);
        Assert.assertTrue(h5.getAddenda() == null);
        Assert.assertTrue(h6.getAddenda() == null);
        Assert.assertTrue(h7.getAddenda() == null);
        Assert.assertTrue(h8.getAddenda() == null);
        Assert.assertTrue(h9.getAddenda() == null);
        Assert.assertTrue(h9bis.getAddenda() == null);
        Assert.assertTrue(h10.getAddenda() == null);
        Assert.assertTrue(h11.getAddenda() == null);
        Assert.assertTrue(h12.getAddenda() == null);
        Assert.assertTrue(h13.getAddenda() == null);
        Assert.assertTrue(h14.getAddenda() == null);
        Assert.assertTrue(h15.getAddenda() == null);
        Assert.assertTrue(h16.getAddenda() == null);
        Assert.assertTrue(h17.getAddenda() == null);
        Assert.assertTrue(h18.getAddenda() == null);
        Assert.assertTrue(h19.getAddenda() == null);
        Assert.assertTrue(h20.getAddenda() == null);
        Assert.assertTrue(h21.getAddenda() == null);

        Assert.assertEquals(h1.toString(), h1.getFullHeaderWithAddenda());
        Assert.assertEquals(h2.toString(), h2.getFullHeaderWithAddenda());
        Assert.assertEquals(h3.toString(), h3.getFullHeaderWithAddenda());
        Assert.assertEquals(h4.toString(), h4.getFullHeaderWithAddenda());
        Assert.assertEquals(h5.toString(), h5.getFullHeaderWithAddenda());
        Assert.assertEquals(h6.toString(), h6.getFullHeaderWithAddenda());
        Assert.assertEquals(h7.toString(), h7.getFullHeaderWithAddenda());
        Assert.assertEquals(h8.toString(), h8.getFullHeaderWithAddenda());
        Assert.assertEquals(h9.toString(), h9.getFullHeaderWithAddenda());
        Assert.assertEquals(h9bis.toString(), h9bis.getFullHeaderWithAddenda());
        Assert.assertEquals(h10.toString(), h10.getFullHeaderWithAddenda());
        Assert.assertEquals(h11.toString(), h11.getFullHeaderWithAddenda());
        Assert.assertEquals(h12.toString(), h12.getFullHeaderWithAddenda());
        Assert.assertEquals(h13.toString(), h13.getFullHeaderWithAddenda());
        Assert.assertEquals(h14.toString(), h14.getFullHeaderWithAddenda());
        Assert.assertEquals(h15.toString(), h15.getFullHeaderWithAddenda());
        Assert.assertEquals(h16.toString(), h16.getFullHeaderWithAddenda());
        Assert.assertEquals(h17.toString(), h17.getFullHeaderWithAddenda());
        Assert.assertEquals(h18.toString(), h18.getFullHeaderWithAddenda());
        Assert.assertEquals(h19.toString(), h19.getFullHeaderWithAddenda());
        Assert.assertEquals(h20.toString(), h20.getFullHeaderWithAddenda());
        Assert.assertEquals(h21.toString(), h21.getFullHeaderWithAddenda());

        Assert.assertEquals(h1.getAbbreviatedFASTAHeader(), h1.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h2.getAbbreviatedFASTAHeader(), h2.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h3.getAbbreviatedFASTAHeader(), h3.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h4.getAbbreviatedFASTAHeader(), h4.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h5.getAbbreviatedFASTAHeader(), h5.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h6.getAbbreviatedFASTAHeader(), h6.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h7.getAbbreviatedFASTAHeader(), h7.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h8.getAbbreviatedFASTAHeader(), h8.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h9.getAbbreviatedFASTAHeader(), h9.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h9bis.getAbbreviatedFASTAHeader(), h9bis.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h10.getAbbreviatedFASTAHeader(), h10.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h11.getAbbreviatedFASTAHeader(), h11.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h12.getAbbreviatedFASTAHeader(), h12.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h13.getAbbreviatedFASTAHeader(), h13.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h14.getAbbreviatedFASTAHeader(), h14.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h15.getAbbreviatedFASTAHeader(), h15.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h16.getAbbreviatedFASTAHeader(), h16.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h17.getAbbreviatedFASTAHeader(), h17.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h18.getAbbreviatedFASTAHeader(), h18.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h19.getAbbreviatedFASTAHeader(), h19.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h20.getAbbreviatedFASTAHeader(), h20.getAbbreviatedFASTAHeaderWithAddenda());
        Assert.assertEquals(h21.getAbbreviatedFASTAHeader(), h21.getAbbreviatedFASTAHeaderWithAddenda());

        // Next add some addenda.
        h2.addAddendum(h3.getCoreHeader());
        Assert.assertTrue(h2.hasAddenda());
        Assert.assertEquals("^A" + h3.getCoreHeader(), h2.getAddenda());
        h2.addAddendum("^A" + h4.getCoreHeader());
        Assert.assertTrue(h2.hasAddenda());
        Assert.assertEquals("^A" + h3.getCoreHeader() + "^A" + h4.getCoreHeader(), h2.getAddenda());
        h2.addAddendum(h1.getCoreHeader());
        Assert.assertTrue(h2.hasAddenda());
        Assert.assertEquals("^A" + h3.getCoreHeader() + "^A" + h4.getCoreHeader() + "^A" + h1.getCoreHeader(), h2.getAddenda());
        Assert.assertEquals(h2.toString() + "^A" + h3.getCoreHeader() + "^A" + h4.getCoreHeader() + "^A" + h1.getCoreHeader(), h2.getFullHeaderWithAddenda());
        Assert.assertEquals(h2.getAbbreviatedFASTAHeader() + "^A" + h3.getCoreHeader() + "^A" + h4.getCoreHeader() + "^A" + h1.getCoreHeader(), h2.getAbbreviatedFASTAHeaderWithAddenda());


        h3.addAddendum("^A" + h2.getCoreHeader());
        Assert.assertTrue(h3.hasAddenda());
        Assert.assertEquals("^A" + h2.getCoreHeader(), h3.getAddenda());

        h1.addAddendum(h3.getCoreHeader());
        h1.addAddendum(h2.getCoreHeader());

        h5.addAddendum(h3.getCoreHeader());
        h5.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h5.hasAddenda());
        Assert.assertEquals(h5.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h5.getFullHeaderWithAddenda());

        h6.addAddendum(h3.getCoreHeader());
        h6.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h6.hasAddenda());
        Assert.assertEquals(h6.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h6.getFullHeaderWithAddenda());

        h7.addAddendum(h3.getCoreHeader());
        h7.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h7.hasAddenda());
        Assert.assertEquals(h7.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h7.getFullHeaderWithAddenda());

        h8.addAddendum(h3.getCoreHeader());
        h8.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h8.hasAddenda());
        Assert.assertEquals(h8.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h8.getFullHeaderWithAddenda());

        h9.addAddendum(h3.getCoreHeader());
        h9.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h9.hasAddenda());
        Assert.assertEquals(h9.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h9.getFullHeaderWithAddenda());

        h9bis.addAddendum(h3.getCoreHeader());
        h9bis.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h9bis.hasAddenda());
        Assert.assertEquals(h9bis.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h9bis.getFullHeaderWithAddenda());


        h10.addAddendum(h3.getCoreHeader());
        h10.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h10.hasAddenda());
        Assert.assertEquals(h10.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h10.getFullHeaderWithAddenda());

        h11.addAddendum(h3.getCoreHeader());
        h11.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h11.hasAddenda());
        Assert.assertEquals(h11.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h11.getFullHeaderWithAddenda());

        h12.addAddendum(h3.getCoreHeader());
        h12.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h12.hasAddenda());
        Assert.assertEquals(h12.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h12.getFullHeaderWithAddenda());

        h14.addAddendum(h3.getCoreHeader());
        h14.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h14.hasAddenda());
        Assert.assertEquals(h14.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h14.getFullHeaderWithAddenda());

        h15.addAddendum(h3.getCoreHeader());
        h15.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h15.hasAddenda());
        Assert.assertEquals(h15.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h15.getFullHeaderWithAddenda());

        h16.addAddendum(h3.getCoreHeader());
        h16.addAddendum(h2.getCoreHeader());
        Assert.assertTrue(h16.hasAddenda());
        Assert.assertEquals(h16.toString() + "^A" + h3.getCoreHeader() + "^A" + h2.getCoreHeader(), h16.getFullHeaderWithAddenda());

        h17.addAddendum(h3.getCoreHeader());
        h17.addAddendum(h17.getCoreHeader());
        Assert.assertTrue(h17.hasAddenda());
        Assert.assertEquals(h17.toString() + "^A" + h3.getCoreHeader() + "^A" + h17.getCoreHeader(), h17.getFullHeaderWithAddenda());

        h18.addAddendum(h3.getCoreHeader());
        h18.addAddendum(h18.getCoreHeader());
        Assert.assertTrue(h18.hasAddenda());
        Assert.assertEquals(h18.toString() + "^A" + h3.getCoreHeader() + "^A" + h18.getCoreHeader(), h18.getFullHeaderWithAddenda());

        h19.addAddendum(h3.getCoreHeader());
        h19.addAddendum(h19.getCoreHeader());
        Assert.assertTrue(h19.hasAddenda());
        Assert.assertEquals(h19.toString() + "^A" + h3.getCoreHeader() + "^A" + h19.getCoreHeader(), h19.getFullHeaderWithAddenda());

        h20.addAddendum(h3.getCoreHeader());
        h20.addAddendum(h20.getCoreHeader());
        Assert.assertTrue(h20.hasAddenda());
        Assert.assertEquals(h20.toString() + "^A" + h3.getCoreHeader() + "^A" + h20.getCoreHeader(), h20.getFullHeaderWithAddenda());

        h21.addAddendum(h3.getCoreHeader());
        h21.addAddendum(h21.getCoreHeader());
        Assert.assertTrue(h21.hasAddenda());
        Assert.assertEquals(h21.toString() + "^A" + h3.getCoreHeader() + "^A" + h21.getCoreHeader(), h21.getFullHeaderWithAddenda());


        // Now see if we parse addenda from a FASTA header with addenda correctly.
        Header parsed = Header.parseFromFASTA(h1.getFullHeaderWithAddenda());
        Assert.assertEquals(h1.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h1.getDescription(), parsed.getDescription());
        Assert.assertEquals(h1.getAccession(), parsed.getAccession());
        Assert.assertEquals(h1.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h1.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h1.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h1.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h1.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h1.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h1.getID(), parsed.getID());
        Assert.assertEquals(h1.getRest(), parsed.getRest());
        Assert.assertEquals(h1.getScore(), parsed.getScore());
        Assert.assertEquals(h1.getStartLocation(), parsed.getStartLocation());


        parsed = Header.parseFromFASTA(h2.getFullHeaderWithAddenda());
        Assert.assertEquals(h2.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h2.getDescription(), parsed.getDescription());
        Assert.assertEquals(h2.getAccession(), parsed.getAccession());
        Assert.assertEquals(h2.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h2.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h2.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h2.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h2.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h2.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h2.getID(), parsed.getID());
        Assert.assertEquals(h2.getRest(), parsed.getRest());
        Assert.assertEquals(h2.getScore(), parsed.getScore());
        Assert.assertEquals(h2.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h3.getFullHeaderWithAddenda());
        Assert.assertEquals(h3.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h3.getDescription(), parsed.getDescription());
        Assert.assertEquals(h3.getAccession(), parsed.getAccession());
        Assert.assertEquals(h3.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h3.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h3.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h3.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h3.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h3.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h3.getID(), parsed.getID());
        Assert.assertEquals(h3.getRest(), parsed.getRest());
        Assert.assertEquals(h3.getScore(), parsed.getScore());
        Assert.assertEquals(h3.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h5.getFullHeaderWithAddenda());
        Assert.assertEquals(h5.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h5.getDescription(), parsed.getDescription());
        Assert.assertEquals(h5.getAccession(), parsed.getAccession());
        Assert.assertEquals(h5.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h5.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h5.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h5.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h5.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h5.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h5.getID(), parsed.getID());
        Assert.assertEquals(h5.getRest(), parsed.getRest());
        Assert.assertEquals(h5.getScore(), parsed.getScore());
        Assert.assertEquals(h5.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h6.getFullHeaderWithAddenda());
        Assert.assertEquals(h6.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h6.getDescription(), parsed.getDescription());
        Assert.assertEquals(h6.getAccession(), parsed.getAccession());
        Assert.assertEquals(h6.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h6.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h6.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h6.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h6.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h6.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h6.getID(), parsed.getID());
        Assert.assertEquals(h6.getRest(), parsed.getRest());
        Assert.assertEquals(h6.getScore(), parsed.getScore());
        Assert.assertEquals(h6.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h7.getFullHeaderWithAddenda());
        Assert.assertEquals(h7.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h7.getDescription(), parsed.getDescription());
        Assert.assertEquals(h7.getAccession(), parsed.getAccession());
        Assert.assertEquals(h7.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h7.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h7.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h7.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h7.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h7.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h7.getID(), parsed.getID());
        Assert.assertEquals(h7.getRest(), parsed.getRest());
        Assert.assertEquals(h7.getScore(), parsed.getScore());
        Assert.assertEquals(h7.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h8.getFullHeaderWithAddenda());
        Assert.assertEquals(h8.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h8.getDescription(), parsed.getDescription());
        Assert.assertEquals(h8.getAccession(), parsed.getAccession());
        Assert.assertEquals(h8.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h8.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h8.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h8.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h8.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h8.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h8.getID(), parsed.getID());
        Assert.assertEquals(h8.getRest(), parsed.getRest());
        Assert.assertEquals(h8.getScore(), parsed.getScore());
        Assert.assertEquals(h8.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h9.getFullHeaderWithAddenda());
        Assert.assertEquals(h9.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h9.getDescription(), parsed.getDescription());
        Assert.assertEquals(h9.getAccession(), parsed.getAccession());
        Assert.assertEquals(h9.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h9.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h9.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h9.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h9.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h9.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h9.getID(), parsed.getID());
        Assert.assertEquals(h9.getRest(), parsed.getRest());
        Assert.assertEquals(h9.getScore(), parsed.getScore());
        Assert.assertEquals(h9.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h9bis.getFullHeaderWithAddenda());
        Assert.assertEquals(h9bis.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h9bis.getDescription(), parsed.getDescription());
        Assert.assertEquals(h9bis.getAccession(), parsed.getAccession());
        Assert.assertEquals(h9bis.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h9bis.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h9bis.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h9bis.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h9bis.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h9bis.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h9bis.getID(), parsed.getID());
        Assert.assertEquals(h9bis.getRest(), parsed.getRest());
        Assert.assertEquals(h9bis.getScore(), parsed.getScore());
        Assert.assertEquals(h9bis.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h10.getFullHeaderWithAddenda());
        Assert.assertEquals(h10.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h10.getDescription(), parsed.getDescription());
        Assert.assertEquals(h10.getAccession(), parsed.getAccession());
        Assert.assertEquals(h10.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h10.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h10.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h10.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h10.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h10.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h10.getID(), parsed.getID());
        Assert.assertEquals(h10.getRest(), parsed.getRest());
        Assert.assertEquals(h10.getScore(), parsed.getScore());
        Assert.assertEquals(h10.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h11.getFullHeaderWithAddenda());
        Assert.assertEquals(h11.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h11.getDescription(), parsed.getDescription());
        Assert.assertEquals(h11.getAccession(), parsed.getAccession());
        Assert.assertEquals(h11.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h11.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h11.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h11.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h11.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h11.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h11.getID(), parsed.getID());
        Assert.assertEquals(h11.getRest(), parsed.getRest());
        Assert.assertEquals(h11.getScore(), parsed.getScore());
        Assert.assertEquals(h11.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h12.getFullHeaderWithAddenda());
        Assert.assertEquals(h12.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h12.getDescription(), parsed.getDescription());
        Assert.assertEquals(h12.getAccession(), parsed.getAccession());
        Assert.assertEquals(h12.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h12.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h12.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h12.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h12.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h12.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h12.getID(), parsed.getID());
        Assert.assertEquals(h12.getRest(), parsed.getRest());
        Assert.assertEquals(h12.getScore(), parsed.getScore());
        Assert.assertEquals(h12.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h13.getFullHeaderWithAddenda());
        Assert.assertEquals(h13.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h13.getDescription(), parsed.getDescription());
        Assert.assertEquals(h13.getAccession(), parsed.getAccession());
        Assert.assertEquals(h13.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h13.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h13.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h13.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h13.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h13.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h13.getID(), parsed.getID());
        Assert.assertEquals(h13.getRest(), parsed.getRest());
        Assert.assertEquals(h13.getScore(), parsed.getScore());
        Assert.assertEquals(h13.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h14.getFullHeaderWithAddenda());
        Assert.assertEquals(h14.getAddenda(), parsed.getAddenda());

        parsed = Header.parseFromFASTA(h15.getFullHeaderWithAddenda());
        Assert.assertEquals(h15.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h15.getDescription(), parsed.getDescription());
        Assert.assertEquals(h15.getAccession(), parsed.getAccession());
        Assert.assertEquals(h15.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h15.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h15.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h15.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h15.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h15.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h15.getID(), parsed.getID());
        Assert.assertEquals(h15.getRest(), parsed.getRest());
        Assert.assertEquals(h15.getScore(), parsed.getScore());
        Assert.assertEquals(h15.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h16.getFullHeaderWithAddenda());
        Assert.assertEquals(h16.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h16.getDescription(), parsed.getDescription());
        Assert.assertEquals(h16.getAccession(), parsed.getAccession());
        Assert.assertEquals(h16.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h16.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h16.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h16.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h16.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h16.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h16.getID(), parsed.getID());
        Assert.assertEquals(h16.getRest(), parsed.getRest());
        Assert.assertEquals(h16.getScore(), parsed.getScore());
        Assert.assertEquals(h16.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h17.getFullHeaderWithAddenda());
        Assert.assertEquals(h17.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h17.getDescription(), parsed.getDescription());
        Assert.assertEquals(h17.getAccession(), parsed.getAccession());
        Assert.assertEquals(h17.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h17.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h17.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h17.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h17.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h17.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h17.getID(), parsed.getID());
        Assert.assertEquals(h17.getRest(), parsed.getRest());
        Assert.assertEquals(h17.getScore(), parsed.getScore());
        Assert.assertEquals(h17.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h18.getFullHeaderWithAddenda());
        Assert.assertEquals(h18.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h18.getDescription(), parsed.getDescription());
        Assert.assertEquals(h18.getAccession(), parsed.getAccession());
        Assert.assertEquals(h18.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h18.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h18.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h18.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h18.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h18.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h18.getID(), parsed.getID());
        Assert.assertEquals(h18.getRest(), parsed.getRest());
        Assert.assertEquals(h18.getScore(), parsed.getScore());
        Assert.assertEquals(h18.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h19.getFullHeaderWithAddenda());
        Assert.assertEquals(h19.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h19.getDescription(), parsed.getDescription());
        Assert.assertEquals(h19.getAccession(), parsed.getAccession());
        Assert.assertEquals(h19.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h19.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h19.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h19.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h19.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h19.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h19.getID(), parsed.getID());
        Assert.assertEquals(h19.getRest(), parsed.getRest());
        Assert.assertEquals(h19.getScore(), parsed.getScore());
        Assert.assertEquals(h19.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h20.getFullHeaderWithAddenda());
        Assert.assertEquals(h20.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h20.getDescription(), parsed.getDescription());
        Assert.assertEquals(h20.getAccession(), parsed.getAccession());
        Assert.assertEquals(h20.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h20.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h20.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h20.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h20.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h20.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h20.getID(), parsed.getID());
        Assert.assertEquals(h20.getRest(), parsed.getRest());
        Assert.assertEquals(h20.getScore(), parsed.getScore());
        Assert.assertEquals(h20.getStartLocation(), parsed.getStartLocation());

        parsed = Header.parseFromFASTA(h21.getFullHeaderWithAddenda());
        Assert.assertEquals(h21.getAddenda(), parsed.getAddenda());
        Assert.assertEquals(h21.getAccession(), parsed.getAccession());
        Assert.assertEquals(h21.getCoreHeader(), parsed.getCoreHeader());
        Assert.assertEquals(h21.getEndLocation(), parsed.getEndLocation());
        Assert.assertEquals(h21.getStartLocation(), parsed.getStartLocation());
        Assert.assertEquals(h21.getForeignAccession(), parsed.getForeignAccession());
        Assert.assertEquals(h21.getForeignDescription(), parsed.getForeignDescription());
        Assert.assertEquals(h21.getForeignID(), parsed.getForeignID());
        Assert.assertEquals(h21.getID(), parsed.getID());
        Assert.assertEquals(h21.getRest(), parsed.getRest());
        Assert.assertEquals(h21.getDescription(), parsed.getDescription());
        Assert.assertEquals(h21.getScore(), parsed.getScore());
        Assert.assertEquals(h21.getStartLocation(), parsed.getStartLocation());
    }

    /**
     * This method test the setting of a location on the Header.
     */
    public void testLocation() {
        final String unknown = ">Unknown header type.";
        final String spStandard = ">sw|O95229|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).";
        final String nrStandard = ">gi|20149565|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]";
        final String nrSPRef = ">gi|21542145|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)";
        final String nrSimple = ">gi|20545032|hypothetical protein XP_51234155 [Homo Sapiens]";
        final String ipiStandard = ">IPI:IPI00232014.1|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976";
        final String halobacterium = ">OE1007R (OE1007R) [del] Predicted orf (overlaps another ORF)";
        final String chlamydia = ">C.tr_L2_353 [492222 - 493658] | Chlamydia trachomatis LGV2";
        final String chlamydia2 = ">C_trachomatis_L2_1 [1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]";
        final String mycobacterium = ">M. tub.H37Rv|Rv1963c|Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R";
        final String drosophila = ">CG11023-PA pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA";
        final String sgd = ">YHR159W YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"";
        final String spFASTA90 = ">P19084|11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)";
        final String generic = ">NP0002A (NP0002A) hypothetical protein";
        final String tair = ">AT1G08520.1 | Symbol: PDE166 | magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum) | chr1:2696415-2700961 FORWARD | Aliases: T27G7.20, T27G7_20, PDE166, PIGMENT DEFECTIVE 166";
        final String hinv = ">HIT000000001.10|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.";
        final String nrAt = ">nrAt0.2_1\t (TR:Q8HT11_ARATH) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).";
        final String listeria = ">L. monocytogenes EGD-e|LMO02333|'comK: 158 aa - competence transcription factor (C-terminal part)";
        final String spSep2008 = ">sp|A7GKH8|PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1";
        final String trSep2008 = ">tr|Q8KFF3|Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1";
        final String ensemblGenomes = ">en|CBW20588|Chromosome:4847047-4849455 fimbrial usher protein";
        final String flybase = ">FBpp0071678 type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;";


        // Parse the headers.
        Header h1 = Header.parseFromFASTA(unknown);
        Header h2 = Header.parseFromFASTA(spStandard);
        Header h3 = Header.parseFromFASTA(nrStandard);
        Header h4 = Header.parseFromFASTA(nrSPRef);
        Header hs = Header.parseFromFASTA(nrSimple);
        Header h6 = Header.parseFromFASTA(ipiStandard);
        Header h7 = Header.parseFromFASTA(halobacterium);
        Header h8 = Header.parseFromFASTA(generic);
        Header h9 = Header.parseFromFASTA(chlamydia);
        Header h9bis = Header.parseFromFASTA(chlamydia2);
        Header h10 = Header.parseFromFASTA(mycobacterium);
        Header h11 = Header.parseFromFASTA(drosophila);
        Header h12 = Header.parseFromFASTA(sgd);
        Header h13 = Header.parseFromFASTA(spFASTA90);
        Header h14 = Header.parseFromFASTA(tair);
        Header h15 = Header.parseFromFASTA(hinv);
        Header h16 = Header.parseFromFASTA(nrAt);
        Header h17 = Header.parseFromFASTA(listeria);
        Header h18 = Header.parseFromFASTA(spSep2008);
        Header h19 = Header.parseFromFASTA(trSep2008);
        Header h20 = Header.parseFromFASTA(ensemblGenomes);
        Header h21 = Header.parseFromFASTA(flybase);

        h1.setLocation(10, 15);
        Assert.assertEquals("Unknown header type. (10-15)", h1.getCoreHeader());
        Assert.assertEquals(">Unknown header type. (10-15)", h1.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">Unknown header type. (10-15)", h1.toString());
        Assert.assertEquals(10, h1.getStartLocation());
        Assert.assertEquals(15, h1.getEndLocation());


        h2.setLocation(411, 423);
        Assert.assertEquals("sw|O95229 (411-423)", h2.getCoreHeader());
        Assert.assertEquals(">sw|O95229 (411-423)|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).", h2.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">sw|O95229 (411-423)|ZWIN_HUMAN ZW10 interactor (ZW10 interacting protein-1) (Zwint-1).", h2.toString());
        Assert.assertEquals(411, h2.getStartLocation());
        Assert.assertEquals(423, h2.getEndLocation());

        h3.setLocation(0, 9);
        Assert.assertEquals("gi|20149565 (0-9)", h3.getCoreHeader());
        Assert.assertEquals(">gi|20149565 (0-9)|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]", h3.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">gi|20149565 (0-9)|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]", h3.toString());
        Assert.assertEquals(0, h3.getStartLocation());
        Assert.assertEquals(9, h3.getEndLocation());

        h4.setLocation(6, 13);
        Assert.assertEquals("gi|21542145 (6-13)", h4.getCoreHeader());
        Assert.assertEquals(">gi|21542145 (6-13)|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)", h4.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">gi|21542145 (6-13)|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)", h4.toString());
        Assert.assertEquals(6, h4.getStartLocation());
        Assert.assertEquals(13, h4.getEndLocation());

        h4.setLocation(33, 45);
        Assert.assertEquals("gi|21542145 (33-45)", h4.getCoreHeader());
        Assert.assertEquals(">gi|21542145 (33-45)|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)", h4.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">gi|21542145 (33-45)|sp|Q9ULX9|MAFF_HUMAN Transcription factor MafF (V-maf musculoaponeurotic fibrosarcoma oncogene homolog F) (U-Maf)", h4.toString());
        Assert.assertEquals(33, h4.getStartLocation());
        Assert.assertEquals(45, h4.getEndLocation());

        hs.setLocation(66, 73);
        Assert.assertEquals("gi|20545032 (66-73)", hs.getCoreHeader());
        Assert.assertEquals(">gi|20545032 (66-73)| hypothetical protein XP_51234155 [Homo Sapiens]", hs.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">gi|20545032 (66-73)| hypothetical protein XP_51234155 [Homo Sapiens]", hs.toString());
        Assert.assertEquals(66, hs.getStartLocation());
        Assert.assertEquals(73, hs.getEndLocation());

        h6.setLocation(13, 23);
        Assert.assertEquals("IPI|IPI00232014.1 (13-23)", h6.getCoreHeader());
        Assert.assertEquals(">IPI|IPI00232014.1 (13-23)|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976", h6.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">IPI|IPI00232014.1 (13-23)|REFSEQ_XP:XP_303976 Tax_Id=9606 hypothetical protein XP_303976", h6.toString());
        Assert.assertEquals(13, h6.getStartLocation());
        Assert.assertEquals(23, h6.getEndLocation());

        h7.setLocation(42, 49);
        Assert.assertEquals("OE1007R (42-49)", h7.getCoreHeader());
        Assert.assertEquals(">OE1007R (42-49) (OE1007R) [del] Predicted orf (overlaps another ORF)", h7.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">OE1007R (42-49) (OE1007R) [del] Predicted orf (overlaps another ORF)", h7.toString());
        Assert.assertEquals(42, h7.getStartLocation());
        Assert.assertEquals(49, h7.getEndLocation());

        h8.setLocation(42, 49);
        Assert.assertEquals("NP0002A (42-49)", h8.getCoreHeader());
        Assert.assertEquals(">NP0002A (42-49) (NP0002A) hypothetical protein", h8.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">NP0002A (42-49) (NP0002A) hypothetical protein", h8.toString());
        Assert.assertEquals(42, h8.getStartLocation());
        Assert.assertEquals(49, h8.getEndLocation());

        h9.setLocation(42, 49);
        Assert.assertEquals("C.tr_L2_353 (42-49)", h9.getCoreHeader());
        Assert.assertEquals(">C.tr_L2_353 (42-49) [492222 - 493658] | Chlamydia trachomatis LGV2", h9.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">C.tr_L2_353 (42-49) [492222 - 493658] | Chlamydia trachomatis LGV2", h9.toString());
        Assert.assertEquals(42, h9.getStartLocation());
        Assert.assertEquals(49, h9.getEndLocation());

        h9bis.setLocation(42, 49);
        Assert.assertEquals("C_trachomatis_L2_1 (42-49)", h9bis.getCoreHeader());
        Assert.assertEquals(">C_trachomatis_L2_1 (42-49) [1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]", h9bis.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">C_trachomatis_L2_1 (42-49) [1 - 1014]| Porphobilinogen Synthase [Chlamydia trachomatis D/UW-3/CX]", h9bis.toString());
        Assert.assertEquals(42, h9bis.getStartLocation());
        Assert.assertEquals(49, h9bis.getEndLocation());

        h10.setLocation(42, 49);
        Assert.assertEquals("M. tub.H37Rv|Rv1963c (42-49)", h10.getCoreHeader());
        Assert.assertEquals(">M. tub.H37Rv|Rv1963c (42-49)|Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R", h10.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">M. tub.H37Rv|Rv1963c (42-49)|Mce3R: 406 aa - PROBABLE TRANSCRIPTIONAL REPRESSOR (PROBABLY TETR-FAMILY) MCE3R", h10.toString());
        Assert.assertEquals(42, h10.getStartLocation());
        Assert.assertEquals(49, h10.getEndLocation());

        h11.setLocation(42, 49);
        Assert.assertEquals("CG11023-PA (42-49)", h11.getCoreHeader());
        Assert.assertEquals(">CG11023-PA (42-49) pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA", h11.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">CG11023-PA (42-49) pep:known chromosome:DROM3B:2L:7529:9491:1 gene:CG11023 transcript:CG11023-RA", h11.toString());
        Assert.assertEquals(42, h11.getStartLocation());
        Assert.assertEquals(49, h11.getEndLocation());

        h12.setLocation(42, 49);
        Assert.assertEquals("YHR159W (42-49)", h12.getCoreHeader());
        Assert.assertEquals(">YHR159W (42-49) YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"", h12.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">YHR159W (42-49) YHR159W SGDID:S000001202, Chr VIII from 417552-419066, Uncharacterized ORF, \"Putative protein of unknown function; green fluorescent protein (GFP)-fusion protein localizes to the cytoplasm; potential Cdc28p substrate\"", h12.toString());
        Assert.assertEquals(42, h12.getStartLocation());
        Assert.assertEquals(49, h12.getEndLocation());

        h13.setLocation(42, 49);
        Assert.assertEquals("sw|P19084 (42-49)", h13.getCoreHeader());
        Assert.assertEquals(">sw|P19084 (42-49)|11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)", h13.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">sw|P19084 (42-49)|11S3_HELAN 11S globulin seed storage protein G3 precursor (Helianthinin G3) [Contains: 11S globulin seed storage protein G3 acidic chain; 11S globulin seed storage protein G3 basic chain] - Helianthus annuus (Common sunflower)", h13.toString());
        Assert.assertEquals(42, h13.getStartLocation());
        Assert.assertEquals(49, h13.getEndLocation());

        h14.setLocation(42, 49);
        Assert.assertEquals("AT1G08520.1 (42-49)", h14.getCoreHeader());
        Assert.assertEquals(">AT1G08520.1 (42-49) magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum)", h14.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">AT1G08520.1 (42-49) magnesium-chelatase subunit chlD, chloroplast, putative / Mg-protoporphyrin IX chelatase, putative (CHLD), similar to Mg-chelatase SP:O24133 from Nicotiana tabacum, GB:AF014399 GI:2318116 from (Pisum sativum)", h14.toString());
        Assert.assertEquals(42, h14.getStartLocation());
        Assert.assertEquals(49, h14.getEndLocation());

        h15.setLocation(42, 49);
        Assert.assertEquals("HIT000000001.10 (42-49)", h15.getCoreHeader());
        Assert.assertEquals(">HIT000000001.10 (42-49)|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.", h15.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">HIT000000001.10 (42-49)|HIX0021591.10|AB002292.2|NO|NO|HC|cds 185..4219|DH domain containing protein.", h15.toString());
        Assert.assertEquals(42, h15.getStartLocation());
        Assert.assertEquals(49, h15.getEndLocation());

        h16.setLocation(42, 49);
        Assert.assertEquals("nrAt0.2_1 \t(TR:Q8HT11_ARATH (42-49))", h16.getCoreHeader());
        Assert.assertEquals(">nrAt0.2_1 \t(TR:Q8HT11_ARATH (42-49)) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).", h16.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">nrAt0.2_1 \t(TR:Q8HT11_ARATH (42-49)) Photosystem II CP43 protein (Fragment).- Arabidopsis thaliana (Mouse-ear cress).", h16.toString());
        Assert.assertEquals(42, h16.getStartLocation());
        Assert.assertEquals(49, h16.getEndLocation());

        h17.setLocation(42, 49);
        Assert.assertEquals("L. monocytogenes EGD-e|LMO02333 (42-49)", h17.getCoreHeader());
        Assert.assertEquals(">L. monocytogenes EGD-e|LMO02333 (42-49)|'comK: 158 aa - competence transcription factor (C-terminal part)", h17.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">L. monocytogenes EGD-e|LMO02333 (42-49)|'comK: 158 aa - competence transcription factor (C-terminal part)", h17.toString());
        Assert.assertEquals(42, h17.getStartLocation());
        Assert.assertEquals(49, h17.getEndLocation());

        h18.setLocation(42, 49);
        Assert.assertEquals("sp|A7GKH8 (42-49)", h18.getCoreHeader());
        Assert.assertEquals(">sp|A7GKH8 (42-49)|PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1", h18.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">sp|A7GKH8 (42-49)|PURL_BACCN Phosphoribosylformylglycinamidine synthase 2 OS=Bacillus cereus subsp. cytotoxis (strain NVH 391-98) GN=purL PE=3 SV=1", h18.toString());
        Assert.assertEquals(42, h18.getStartLocation());
        Assert.assertEquals(49, h18.getEndLocation());

        h19.setLocation(42, 49);
        Assert.assertEquals("tr|Q8KFF3 (42-49)", h19.getCoreHeader());
        Assert.assertEquals(">tr|Q8KFF3 (42-49)|Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1", h19.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">tr|Q8KFF3 (42-49)|Q8KFF3_CHLTE SugE protein OS=Chlorobium tepidum GN=sugE PE=3 SV=1", h19.toString());
        Assert.assertEquals(42, h19.getStartLocation());
        Assert.assertEquals(49, h19.getEndLocation());

        h20.setLocation(42, 49);
        Assert.assertEquals("en|CBW20588 (42-49)", h20.getCoreHeader());
        Assert.assertEquals(">en|CBW20588 (42-49)|Chromosome:4847047-4849455 fimbrial usher protein", h20.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">en|CBW20588 (42-49)|Chromosome:4847047-4849455 fimbrial usher protein", h20.toString());
        Assert.assertEquals(42, h20.getStartLocation());
        Assert.assertEquals(49, h20.getEndLocation());

        h21.setLocation(42, 49);
        Assert.assertEquals("FBpp0071678 (42-49)", h21.getCoreHeader());
        Assert.assertEquals(">FBpp0071678 (42-49) type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;", h21.getAbbreviatedFASTAHeader());
        Assert.assertEquals(">FBpp0071678 (42-49) type=protein; loc=2R:join(18050425..18051199,18052282..18052494,18056749..18058222,18058283..18059490,18059587..18059757,18059821..18059938,18060002..18060032); ID=FBpp0071678; name=a-PB; parent=FBgn0000008,FBtr0071764; dbxref=FlyBase_Annotation_IDs:CG6741-PB,FlyBase:FBpp0071678,GB_protein:AAF46809.2,GB_protein:AAF46809,REFSEQ:NP_524641; MD5=9eb6e9e4c12ec62fdeb31cca5b0683b6; length=1329; release=r5.13; species=Dmel;", h21.toString());
        Assert.assertEquals(42, h21.getStartLocation());
        Assert.assertEquals(49, h21.getEndLocation());


        // Finally, test the creation of a header with location information present.
        // First SP.
        Header h5 = Header.parseFromFASTA(h2.toString());
        Assert.assertEquals(h2.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h2.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h2.toString(), h5.toString());

        // Next plain NCBI.
        h5 = Header.parseFromFASTA(h3.toString());
        Assert.assertEquals(h3.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h3.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h3.toString(), h5.toString());

        // Next simple NCBI.
        h5 = Header.parseFromFASTA(hs.toString());
        Assert.assertEquals(hs.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(hs.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(hs.toString(), h5.toString());

        // Next NCBI with SP ref.
        h5 = Header.parseFromFASTA(h4.toString());
        Assert.assertEquals(h4.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h4.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h4.toString(), h5.toString());

        // Next plain NCBI.
        h5 = Header.parseFromFASTA(h1.toString());
        Assert.assertEquals(h1.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h1.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h1.toString(), h5.toString());

        // Next IPI.
        h5 = Header.parseFromFASTA(h6.toString());
        Assert.assertEquals(h6.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h6.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h6.toString(), h5.toString());

        // Next halobacterium.
        h5 = Header.parseFromFASTA(h7.toString());
        Assert.assertEquals(h7.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h7.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h7.toString(), h5.toString());

        // Next generic.
        h5 = Header.parseFromFASTA(h8.toString());
        Assert.assertEquals(h8.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h8.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h8.toString(), h5.toString());

        // Next Chlamydia.
        h5 = Header.parseFromFASTA(h9.toString());
        Assert.assertEquals(h9.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h9.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h9.toString(), h5.toString());
        h5 = Header.parseFromFASTA(h9bis.toString());
        Assert.assertEquals(h9bis.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h9bis.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h9bis.toString(), h5.toString());

        // Next Mycobacterium.
        h5 = Header.parseFromFASTA(h10.toString());
        Assert.assertEquals(h10.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h10.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h10.toString(), h5.toString());

        // Next Drosophila.
        h5 = Header.parseFromFASTA(h11.toString());
        Assert.assertEquals(h11.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h11.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h11.toString(), h5.toString());

        // Next SGD.
        h5 = Header.parseFromFASTA(h12.toString());
        Assert.assertEquals(h12.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h12.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h12.toString(), h5.toString());

        // Next new SP FASTA 9.0.
        h5 = Header.parseFromFASTA(h13.toString());
        Assert.assertEquals(h13.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h13.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h13.toString(), h5.toString());

        // Next TAIR.
        h5 = Header.parseFromFASTA(h14.toString());

        // Next H-Inv
        h5 = Header.parseFromFASTA(h15.toString());
        Assert.assertEquals(h15.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h15.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h15.toString(), h5.toString());

        // Next nrAt
        h5 = Header.parseFromFASTA(h16.toString());
        Assert.assertEquals(h16.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h16.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h16.toString(), h5.toString());

        // Next listeria
        h5 = Header.parseFromFASTA(h17.toString());
        Assert.assertEquals(h17.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h17.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h17.toString(), h5.toString());

        // Next SP > Sep 2008
        h5 = Header.parseFromFASTA(h18.toString());
        Assert.assertEquals(h18.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h18.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h18.toString(), h5.toString());


        // Next TrEMBL > Sep 2008
        h5 = Header.parseFromFASTA(h19.toString());
        Assert.assertEquals(h19.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h19.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h19.toString(), h5.toString());

        // Next Ensembl Genomes
        h5 = Header.parseFromFASTA(h20.toString());
        Assert.assertEquals(h20.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h20.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h20.toString(), h5.toString());

        // Next Flybase
        h5 = Header.parseFromFASTA(h21.toString());
        Assert.assertEquals(h21.getStartLocation(), h5.getStartLocation());
        Assert.assertEquals(h21.getEndLocation(), h5.getEndLocation());
        Assert.assertEquals(h21.toString(), h5.toString());


        // Now make it harder: insert '()' somewhere.
        h5 = Header.parseFromFASTA(">This is a hard (test header)");
        Assert.assertEquals(-1, h5.getStartLocation());
        Assert.assertEquals(-1, h5.getEndLocation());

        h5 = Header.parseFromFASTA(">This is a hard-er (test header)");
        Assert.assertEquals(-1, h5.getStartLocation());
        Assert.assertEquals(-1, h5.getEndLocation());

        h5 = Header.parseFromFASTA(">This is a hard-er (test header) (0-9)");
        Assert.assertEquals(0, h5.getStartLocation());
        Assert.assertEquals(9, h5.getEndLocation());
    }

    /**
     * This method test the 'clone' method on the Header class.
     */
    public void testClone() {
        final String unknown = ">Unknown header type.";
        final String nrStandard = ">gi|20149565|ref|NP_004878.2| small inducible cytokine B14 precursor; CXC chemokine in breast and kidney; small inducible cytokine subfamily B (Cys-X-Cys), member 14 (BRAK) [Homo sapiens]";

        Header h = Header.parseFromFASTA(unknown);
        Header clone = (Header)h.clone();
        Assert.assertEquals(h.getAccession(), clone.getAccession());
        Assert.assertEquals(h.getAddenda(), clone.getAddenda());
        Assert.assertEquals(h.getCoreHeader(), clone.getCoreHeader());
        Assert.assertEquals(h.getDescription(), clone.getDescription());
        Assert.assertEquals(h.getEndLocation(), clone.getEndLocation());
        Assert.assertEquals(h.getForeignAccession(), clone.getForeignAccession());
        Assert.assertEquals(h.getForeignDescription(), clone.getForeignDescription());
        Assert.assertEquals(h.getForeignID(), clone.getForeignID());
        Assert.assertEquals(h.getID(), clone.getID());
        Assert.assertEquals(h.getRest(), clone.getRest());
        Assert.assertEquals(h.getScore(), clone.getScore());
        Assert.assertEquals(h.getStartLocation(), clone.getStartLocation());
        Assert.assertEquals(h.getFullHeaderWithAddenda(), clone.getFullHeaderWithAddenda());

        h = Header.parseFromFASTA(nrStandard);
        h.setLocation(3, 8);
        clone = (Header)h.clone();
        Assert.assertEquals(h.getAccession(), clone.getAccession());
        Assert.assertEquals(h.getAddenda(), clone.getAddenda());
        Assert.assertEquals(h.getCoreHeader(), clone.getCoreHeader());
        Assert.assertEquals(h.getDescription(), clone.getDescription());
        Assert.assertEquals(h.getEndLocation(), clone.getEndLocation());
        Assert.assertEquals(h.getForeignAccession(), clone.getForeignAccession());
        Assert.assertEquals(h.getForeignDescription(), clone.getForeignDescription());
        Assert.assertEquals(h.getForeignID(), clone.getForeignID());
        Assert.assertEquals(h.getID(), clone.getID());
        Assert.assertEquals(h.getRest(), clone.getRest());
        Assert.assertEquals(h.getScore(), clone.getScore());
        Assert.assertEquals(h.getStartLocation(), clone.getStartLocation());
        Assert.assertEquals(h.getFullHeaderWithAddenda(), clone.getFullHeaderWithAddenda());

        h = Header.parseFromFASTA(nrStandard);
        h.setLocation(3, 8);
        h.addAddendum(h.getCoreHeader());
        h.addAddendum(h.getCoreHeader());
        clone = (Header)h.clone();
        Assert.assertEquals(h.getAccession(), clone.getAccession());
        Assert.assertEquals(h.getAddenda(), clone.getAddenda());
        Assert.assertEquals(h.getCoreHeader(), clone.getCoreHeader());
        Assert.assertEquals(h.getDescription(), clone.getDescription());
        Assert.assertEquals(h.getEndLocation(), clone.getEndLocation());
        Assert.assertEquals(h.getForeignAccession(), clone.getForeignAccession());
        Assert.assertEquals(h.getForeignDescription(), clone.getForeignDescription());
        Assert.assertEquals(h.getForeignID(), clone.getForeignID());
        Assert.assertEquals(h.getID(), clone.getID());
        Assert.assertEquals(h.getRest(), clone.getRest());
        Assert.assertEquals(h.getScore(), clone.getScore());
        Assert.assertEquals(h.getStartLocation(), clone.getStartLocation());
        Assert.assertEquals(h.getFullHeaderWithAddenda(), clone.getFullHeaderWithAddenda());


        // Finally, see that changing 'clone' does not change 'h'.
        clone.setLocation(100, 101);
        Assert.assertEquals(100, clone.getStartLocation());
        Assert.assertEquals(101, clone.getEndLocation());
        Assert.assertEquals(3, h.getStartLocation());
        Assert.assertEquals(8, h.getEndLocation());
    }
}
