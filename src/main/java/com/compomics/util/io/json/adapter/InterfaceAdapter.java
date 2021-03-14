package com.compomics.util.io.json.adapter;

import com.compomics.util.io.export.features.peptideshaker.PsAnnotationFeature;
import com.compomics.util.io.export.features.peptideshaker.PsFragmentFeature;
import com.compomics.util.io.export.features.peptideshaker.PsIdentificationAlgorithmMatchesFeature;
import com.compomics.util.io.export.features.peptideshaker.PsInputFilterFeature;
import com.compomics.util.io.export.features.peptideshaker.PsPeptideFeature;
import com.compomics.util.io.export.features.peptideshaker.PsProjectFeature;
import com.compomics.util.io.export.features.peptideshaker.PsProteinFeature;
import com.compomics.util.io.export.features.peptideshaker.PsPsmFeature;
import com.compomics.util.io.export.features.peptideshaker.PsPtmScoringFeature;
import com.compomics.util.io.export.features.peptideshaker.PsSearchFeature;
import com.compomics.util.io.export.features.peptideshaker.PsSpectrumCountingFeature;
import com.compomics.util.io.export.features.peptideshaker.PsValidationFeature;
import com.compomics.util.io.export.features.reporter.ReporterPeptideFeature;
import com.compomics.util.io.export.features.reporter.ReporterProteinFeatures;
import com.compomics.util.io.export.features.reporter.ReporterPsmFeatures;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * A generic adapter to parse used interfaces in a class.
 *
 * @author Kenneth Verheggen
 * @author Harald Barsnes
 */
public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    /**
     * Empty default constructor.
     */
    public InterfaceAdapter() {
    }

    @Override
    public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
        final JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", object.getClass().getName());
        wrapper.add("data", context.serialize(object));
        return wrapper;
    }

    @Override
    public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {

        if (elem.isJsonPrimitive()
                && interfaceType.getTypeName().equalsIgnoreCase("com.compomics.util.io.export.ExportFeature")) {

            final JsonPrimitive jsonPrimitive = elem.getAsJsonPrimitive();
            final String featureAsString = jsonPrimitive.getAsString();

            String[] featureDetails = featureAsString.split("\\.");
            String featureType = featureDetails[0];
            String feature = featureDetails[1];

            switch (featureType) {

                case "PsAnnotationFeature":
                    return (T) PsAnnotationFeature.valueOf(feature);

                case "PsFragmentFeature":
                    return (T) PsFragmentFeature.valueOf(feature);

                case "PsIdentificationAlgorithmMatchesFeature":
                    return (T) PsIdentificationAlgorithmMatchesFeature.valueOf(feature);

                case "PsInputFilterFeature":
                    return (T) PsInputFilterFeature.valueOf(feature);

                case "PsPeptideFeature":
                    return (T) PsPeptideFeature.valueOf(feature);

                case "PsProjectFeature":
                    return (T) PsProjectFeature.valueOf(feature);

                case "PsProteinFeature":
                    return (T) PsProteinFeature.valueOf(feature);

                case "PsPsmFeature":
                    return (T) PsPsmFeature.valueOf(feature);

                case "PsPtmScoringFeature":
                    return (T) PsPtmScoringFeature.valueOf(feature);

                case "PsSearchFeature":
                    return (T) PsSearchFeature.valueOf(feature);

                case "PsSpectrumCountingFeature":
                    return (T) PsSpectrumCountingFeature.valueOf(feature);

                case "PsValidationFeature":
                    return (T) PsValidationFeature.valueOf(feature);

                case "ReporterPeptideFeature":
                    return (T) ReporterPeptideFeature.valueOf(feature);

                case "ReporterProteinFeatures":
                    return (T) ReporterProteinFeatures.valueOf(feature);

                case "ReporterPsmFeatures":
                    return (T) ReporterPsmFeatures.valueOf(feature);

                default:
                    throw new JsonParseException(
                            "Unknown export feature '" + featureAsString + "'!"
                    );

            }

        } else {
            final JsonObject jsonObject = elem.getAsJsonObject();
            final JsonElement typeName = get(jsonObject, "type");
            final JsonElement data = get(jsonObject, "data");
            final Type actualType = typeForName(typeName);
            return context.deserialize(data, actualType);
        }
    }

    /**
     * Returns the type.
     *
     * @param typeElem the JSON type element
     * @return the type
     */
    private Type typeForName(final JsonElement typeElem) {
        try {
            return Class.forName(typeElem.getAsString());
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    /**
     * Returns the JSON element.
     *
     * @param wrapper the JSON wrapper
     * @param memberName the member name
     * @return the JSON element
     */
    private JsonElement get(final JsonObject wrapper, String memberName) {

        final JsonElement elem = wrapper.get(memberName);

        if (elem == null) {
            throw new JsonParseException(
                    "No '" + memberName
                    + "' member found in what was expected to be an interface wrapper."
            );
        }

        return elem;
    }
}
