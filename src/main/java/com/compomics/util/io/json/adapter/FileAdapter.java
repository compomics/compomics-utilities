package com.compomics.util.io.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.IOException;

/**
 * This class is intended to avoid the default behavior of the GSON parser to
 * append the current path to the absolute path of a file. It should always be
 * registered in the GSON builder as this is not wanted behavior.
 *
 * @author Kenneth Verheggen
 */
public class FileAdapter extends TypeAdapter {

    /**
     * Empty default constructor
     */
    public FileAdapter() {
    }

    @Override
    public File read(final JsonReader in) throws IOException {
        in.beginObject();
        File file = null;
        while (in.hasNext()) {
            if (in.nextName().equalsIgnoreCase("path")) {
                file = new File(in.nextString());
            }
        }
        in.endObject();
        return file;
    }

    @Override
    public void write(JsonWriter writer, Object t) throws IOException {
        writer.beginObject();
        if (t instanceof File) {
            writer.name("path").value(((File) t).getAbsolutePath());
        }
        writer.endObject();
    }
}
