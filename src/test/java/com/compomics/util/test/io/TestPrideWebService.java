package com.compomics.util.test.io;

import com.compomics.util.pride.PrideWebService;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilter;
import com.compomics.util.pride.prideobjects.webservice.query.PrideFilterType;
import java.io.IOException;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetail;
import uk.ac.ebi.pride.archive.web.service.model.assay.AssayDetailList;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetail;
import uk.ac.ebi.pride.archive.web.service.model.file.FileDetailList;
import uk.ac.ebi.pride.archive.web.service.model.peptide.PsmDetailList;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectDetail;
import uk.ac.ebi.pride.archive.web.service.model.project.ProjectSummaryList;
import uk.ac.ebi.pride.archive.web.service.model.protein.ProteinDetailList;

/**
 * Test for the PRIDE web service Java object.
 *
 * @author Kenneth Verheggen
 */
public class TestPrideWebService {

    public TestPrideWebService() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getProjectDetails method, of class PrideWebService.
     */
    @Test
    public void testGetProjectSummaryList() throws Exception {
        String query = "PRD000001";
        ProjectSummaryList projectSummaryList = PrideWebService.getProjectSummaryList(query);
        assertTrue(projectSummaryList.getList().size() == 1);
    }

    /**
     * Test of getProjectDetails method, of class PrideWebService.
     */
    @Test
    public void testGetProjectDetails() throws Exception {
        String query = "PXD000001";
        ProjectDetail result = PrideWebService.getProjectDetail(query);
        assertTrue(result.getAccession().equals("PXD000001"));
    }

    /**
     * Test of getProjectDetailsCount method, of class PrideWebService.
     */
    @Test
    public void testGetProjectCount() throws Exception {
        String query = "";
        PrideFilter filter = new PrideFilter(PrideFilterType.speciesFilter, "tyrannosaurus");
        int expResult = 1;
        int result = PrideWebService.getProjectCount(query, filter);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAssayDetails method, of class PrideWebService.
     */
    @Test
    public void testGetAssayDetails() throws Exception {
        String projectAccession = "PRD000001";
        AssayDetailList result = PrideWebService.getAssayDetails(projectAccession);
        assertTrue(result.getList().size() == 5);
    }

    /**
     * Test of getAssayDetailsCount method, of class PrideWebService.
     */
    @Test
    public void testGetAssayDetailsCount() throws Exception {
        String projectAccession = "PRD000001";
        int expResult = 5;
        int result = PrideWebService.getAssayCount(projectAccession);
        assertTrue(result == expResult);
    }

    /**
     * Test of getSingleAssayDetail method, of class PrideWebService.
     */
    @Test
    public void testGetAssayDetail() throws Exception {
        String assayAccession = "3";
        AssayDetail result = PrideWebService.getAssayDetail(assayAccession);
        assertEquals(result.getAssayAccession(), assayAccession);
        assertEquals(result.getPeptideCount(), 1958);
    }

    /**
     * Test of getProjectFileDetails method, of class PrideWebService.
     */
    @Test
    public void testGetProjectFileDetails() throws Exception {
        String projectAccession = "PRD000001";
        int expResult = 5;
        FileDetailList result = PrideWebService.getProjectFileDetails(projectAccession);
        assertEquals(expResult, result.getList().size());
    }

    /**
     * Test of getProjectFileDetailsCount method, of class PrideWebService.
     */
    @Test
    public void testGetProjectFileDetailsCount() throws Exception {
        String projectAccession = "PRD000001";
        int expResult = 5;
        int result = PrideWebService.getProjectFileCount(projectAccession);
        assertEquals(expResult, result);
    }

    /**
     * Test of getAssayFileDetails method, of class PrideWebService.
     */
    @Test
    public void testGetAssayFileDetails() throws Exception {
        String assayAccession = "3";
        FileDetailList result = PrideWebService.getAssayFileDetails(assayAccession);
        FileDetail detail = result.getList().get(0);
        assertEquals("3", detail.getAssayAccession());
        assertEquals("PRD000001", detail.getProjectAccession());
    }

    /**
     * Test of getAssayFileDetailsCount method, of class PrideWebService.
     */
    @Test
    public void testGetAssayFileDetailsCount() throws Exception {
        String assayAccession = "3";
        int expResult = 1;
        int result = PrideWebService.getAssayFileCount(assayAccession);
        assertEquals(expResult, result);
    }

    /**
     * Test of getProteinsByProject method, of class PrideWebService.
     */
    @Test
    public void testGetProteinsByProject() throws IOException {
        String projectAccession = "PXD000001";
        ProteinDetailList result = PrideWebService.getProteinIdentificationByProject(projectAccession);
        assertEquals(496, result.getList().size());
    }

    /**
     * Test of getProteinsCountByProject method, of class PrideWebService.
     */
    @Test
    public void testGetProteinsCountByProject() throws Exception {
        String projectAccession = "PXD000001";
        int expResult = 496;
        int result = PrideWebService.getProteinIdentificationCountByProject(projectAccession);
        assertEquals(expResult, result);
    }

    /**
     * Test of getProteinsByProject method, of class PrideWebService.
     */
    @Test
    public void testgetProteinIdentificationsByProjectAndProtein() throws IOException {
        String projectAccession = "PXD000001";
        String proteinAccesion = "DECOY_ECA2118";
        ProteinDetailList result = PrideWebService.getProteinIdentificationsByProjectAndProtein(projectAccession, proteinAccesion);
        assertEquals(1, result.getList().size());
    }
   
    /**
     * Test of getProteinsCountByProject method, of class PrideWebService.
     */
    @Test
    public void testGetProteinIdentificationsCountByProjectAndProtein() throws Exception {
        String projectAccession = "PXD000001";
        String proteinAccesion = "DECOY_ECA2118";
        int result = PrideWebService.getProteinIdentificationsCountByProjectAndProtein(projectAccession, proteinAccesion);
        assertEquals(1, result);
    }

    /**
     * Test of getProteinCountByAssay method, of class PrideWebService.
     */
    @Test
    public void testGetProteinCountByAssay() throws IOException {
        String assayAccession = "3";
        int expResult = 345;
        int result = PrideWebService.getProteinIdentificationCountByAssay(assayAccession);
        assertEquals(expResult, result);
    }

    /**
     * Test of getPSMsByProject method, of class PrideWebService.
     */
    @Test
    public void testGetPSMsByProject() throws IOException {
        String projectAccession = "PRD000001";
        PsmDetailList result = PrideWebService.getPSMsByProject(projectAccession);
        assertEquals(6758, result.getList().size());
    }

    /**
     * Test of getPSMCountByProject method, of class PrideWebService.
     */
    @Test
    public void testGetPSMCountByProject() throws Exception {
        String projectAccession = "PRD000001";
        int result = PrideWebService.getPSMCountByProject(projectAccession);
        assertEquals(6758, result);
    }

    /**
     * Test of getPSMsByProject method, of class PrideWebService.
     */
    @Test
    public void testGetPSMCountByProjectAndSequence() throws IOException {
        String projectAccession = "PXD000001";
        String sequence = "SVEELNTELLGLLR";
        int result = PrideWebService.getPSMCountByProjectAndSequence(projectAccession, sequence);
        assertEquals(2, result);
    }

    /**
     * Test of getPSMCountByProject method, of class PrideWebService.
     */
    @Test
    public void testGetPSMsProjectAndSequence() throws Exception {
        String projectAccession = "PXD000001";
        String sequence = "SVEELNTELLGLLR";
        PsmDetailList result = PrideWebService.getPSMsByProjectAndSequence(projectAccession, sequence);
        assertEquals(result.getList().size(), 2);
        assertTrue(result.getList().get(0).getSequence().equalsIgnoreCase(sequence));
    }

    /**
     * Test of getPSMsByAssay method, of class PrideWebService.
     */
    @Test
    public void testGetPSMsByAssay() throws IOException {
        String assayAccession = "3";
        PsmDetailList result = PrideWebService.getPSMsByAssay(assayAccession);
        assertEquals(result.getList().size(), 1958);
    }

    /**
     * Test of getPSMCountByAssay method, of class PrideWebService.
     */
    @Test
    public void testGetPSMCountByAssay() throws Exception {
        String assayAccession = "3";
        int result = PrideWebService.getPSMCountByAssay(assayAccession);
        assertEquals(1958, result);
    }

    /**
     * Test of getPSMsByAssay method, of class PrideWebService.
     */
    @Test
    public void testGetPSMsByAssayAndSequence() throws IOException {
        String assayAccession = "3";
        String sequence = "AAAAAAAAAAAAR";
        PsmDetailList result = PrideWebService.getPSMsByAssayAndSequence(assayAccession, sequence);
        assertEquals(1, result.getList().size());
    }

    /**
     * Test of getPSMCountByAssay method, of class PrideWebService.
     */
    @Test
    public void testGetPSMCountByAssayAndSequence() throws Exception {
        String assayAccession = "3";
        String sequence = "AAAAAAAAAAAAR";
        int result = PrideWebService.getPSMCountByAssayAndSequence(assayAccession, sequence);
        assertEquals(1, result);
    }
}
