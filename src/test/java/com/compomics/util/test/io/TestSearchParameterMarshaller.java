package com.compomics.util.test.io;

import com.compomics.util.Util;
import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.io.json.marshallers.IdentificationParametersMarshaller;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import java.io.File;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zoodb.internal.util.DBTracer;

/**
 * Test for the SearchParameterMarshaller.
 *
 * @author Kenneth Verheggen
 * @author Marc Vaudel
 */
public class TestSearchParameterMarshaller {

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
    public void testMarshallSearchParametersToJson() {
        String path = this.getClass().getResource("TestSearchParameterMarshaller.class").getPath();
        path = path.substring(1, path.indexOf("/target/"));
        path += "/src/test/resources/experiment/identificationDB";
        File dbFolder = new File(path);
        if (!dbFolder.exists()) {
            dbFolder.mkdir();
        }
        ObjectsDB objectsDB = null;

        try {
            DBTracer.enable(true);
            //objectsDB = new ObjectsDB(path, "experimentTestDB2.zdb", true);
        
        
        
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
        } finally {
            if (objectsDB != null){
                objectsDB.close();
            }
            Util.deleteDir(dbFolder);
        }
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
        parameters.setDigestionParameters(DigestionParameters.getDefaultParameters());
        parameters.setMaxChargeSearched(5);
        parameters.setMinChargeSearched(1);
        parameters.setModificationParameters(createMockUpPTMSettings());
        parameters.setFastaFile(new File("T:/HIS/IS/A/FAKE/FASTA.FASTA"));
        return parameters;
    }

    /**
     * @return mock up PtmSettings
     */
    private ModificationParameters createMockUpPTMSettings() {
        ModificationParameters settings = new ModificationParameters();
        ModificationFactory instance = ModificationFactory.getInstance();
        settings.addFixedModification(instance.getModification("Carboxymethylation of C"));
        settings.addVariableModification(instance.getModification("Oxidation of M"));
        return settings;
    }
}
