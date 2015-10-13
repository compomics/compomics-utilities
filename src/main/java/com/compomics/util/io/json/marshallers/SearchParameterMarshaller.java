package com.compomics.util.io.json.marshallers;

/**
 *
 * @author Kenneth Verheggen
 */
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.identification.identification_parameters.IdentificationAlgorithmParameter;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.io.json.JsonMarshaller;
import java.io.File;
import java.io.IOException;

/**
 * This class is a convenience class to have a DefaultJsonConverter with the
 * search parameter interfaces
 *
 * @author Kenneth Verheggen
 */
public class SearchParameterMarshaller extends JsonMarshaller {

    /**
     * Default constructor
     */
    public SearchParameterMarshaller() {
        super(IdentificationAlgorithmParameter.class, Atom.class);
    }

    /**
     *
     * @param objectType The class the object belongs to (SearchParameters in
     * this case)
     * @param jsonFile a json file
     * @return an instance of the objectType containing the json information
     * @throws IOException
     */
    @Override
    public Object fromJson(Class objectType, File jsonFile) throws IOException {
        String jsonString = super.getJsonStringFromFile(jsonFile);
        SearchParameters param = (SearchParameters) gson.fromJson(jsonString, objectType);       
        return param;
    }
}
