package com.compomics.util.test.io;

import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.io.json.marshallers.SearchParameterMarshaller;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for the SearchParameterMarshaller.
 *
 * @author Kenneth Verheggen
 */
public class TestSearchParameterMarshaller {

    public TestSearchParameterMarshaller() {
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
     * Test of the JSON (un)marshalling methods for SearchParameters (also
     * applies for other marshallers)
     */
    @Test
    public void testMarshallSearchParametersToJson() throws Exception {
        System.out.println("Testing JSON Marshalling...");
        Class objectType = SearchParameters.class;
        SearchParameters parameters = createMockUpParameters();
        SearchParameterMarshaller instance = new SearchParameterMarshaller();
        //1. Marshall to JSON
        String parametersAsJson = instance.toJson(parameters);
        System.out.println(parametersAsJson);
        //2. Unmarshall back to a parameters object
        SearchParameters jsonAsParameters = (SearchParameters) instance.fromJson(objectType, parametersAsJson);
        System.out.println(jsonAsParameters);
        //3. Compare both
        assertTrue(parameters.equals(jsonAsParameters));
    }

    /**
     * @return mock up SearchParameters
     */
    private SearchParameters createMockUpParameters() {
        SearchParameters parameters = new SearchParameters();
        parameters.setFragmentIonAccuracy(0.02);
        parameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setPrecursorAccuracy(0.5);
        parameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
        parameters.setnMissedCleavages(2);
        parameters.setMaxChargeSearched(new Charge(1, 5));
        parameters.setMinChargeSearched(new Charge(1, 1));
        parameters.setPtmSettings(createMockUpPTMSettings());
        parameters.setFastaFile(new File("T:/HIS/IS/A/FAKE/FASTA.FASTA"));
        parameters.setParametersFile(new File("T:/HIS/IS/JUST/TO/TEST/CAN/BE/COMMENTED/LATER.par"));
        return parameters;
    }

    /**
     * @return mock up PtmSettings
     */
    private PtmSettings createMockUpPTMSettings() {
        PtmSettings settings = new PtmSettings();
        PTMFactory instance = PTMFactory.getInstance();
        settings.addFixedModification(instance.getPTM("Carboxymethylation of C"));
        settings.addVariableModification(instance.getPTM("Oxidation of M"));
        return settings;
    }
}
