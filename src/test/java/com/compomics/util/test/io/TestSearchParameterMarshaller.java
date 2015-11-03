package com.compomics.util.test.io;

import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
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
        Class objectType = SearchParameters.class;
        SearchParameters parameters = createMockUpParameters();
        IdentificationParametersMarshaller instance = new IdentificationParametersMarshaller();
        //1. Marshall to JSON
        String parametersAsJson = instance.toJson(parameters);
        //System.out.println(parametersAsJson);
        //2. Unmarshall back to a parameters object
        SearchParameters jsonAsParameters = (SearchParameters) instance.fromJson(objectType, parametersAsJson);
        //System.out.println(jsonAsParameters);
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
