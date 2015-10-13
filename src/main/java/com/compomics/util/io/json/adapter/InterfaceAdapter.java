package com.compomics.util.io.json.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * A generic adapter to parse used interfaces in a class
 *
 * @author Kenneth Verheggen
 */
public class InterfaceAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    /**
     *
     * @param object the input object
     * @param interfaceType the class of the interface
     * @param context the serialization context for the json parser
     * @return a serialized version of the object as a JsonElement
     */
    @Override
    public JsonElement serialize(T object, Type interfaceType, JsonSerializationContext context) {
        final JsonObject wrapper = new JsonObject();
        wrapper.addProperty("type", object.getClass().getName());
        wrapper.add("data", context.serialize(object));
        return wrapper;
    }

    /**
     *
     * @param elem a serialized version of the object as a JsonElement
     * @param interfaceType the class of the interface
     * @param context the deserialization context for the json parser
     * @throws JsonParseException if the deserialisation failed due to a
     * corrupted json format
     * @return an object of the interface type
     */
    @Override
    public T deserialize(JsonElement elem, Type interfaceType, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject wrapper = (JsonObject) elem;
        final JsonElement typeName = get(wrapper, "type");
        final JsonElement data = get(wrapper, "data");
        final Type actualType = typeForName(typeName);
        return context.deserialize(data, actualType);
    }

    private Type typeForName(final JsonElement typeElem) {
        try {
            return Class.forName(typeElem.getAsString());
        } catch (ClassNotFoundException e) {
            throw new JsonParseException(e);
        }
    }

    private JsonElement get(final JsonObject wrapper, String memberName) {
        final JsonElement elem = wrapper.get(memberName);
        if (elem == null) {
            throw new JsonParseException("No '" + memberName + "' member found in what was expected to be an interface wrapper");
        }
        return elem;
    }

}
