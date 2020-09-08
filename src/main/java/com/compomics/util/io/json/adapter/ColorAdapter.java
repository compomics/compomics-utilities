package com.compomics.util.io.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.awt.Color;
import java.io.IOException;

/**
 * ColorAdapter required to avoid illegal reflective access warnings.
 *
 * @author Harald Barsnes
 */
public class ColorAdapter extends TypeAdapter<Color> {

    @Override
    public void write(JsonWriter writer, Color t) throws IOException {

        writer.beginObject();
        writer.name("value").value(t.getRGB());
        writer.name("falpha").value(t.getAlpha());
        writer.endObject();

    }

    @Override
    public Color read(JsonReader reader) throws IOException {

        reader.beginObject();
        reader.nextName();
        int rgb = reader.nextInt();
        reader.nextName();
        int alpha = reader.nextInt();
        reader.endObject();

        Color tempColor = new Color(rgb);
        return new Color(tempColor.getRed(), tempColor.getGreen(), tempColor.getBlue(), alpha);
    }

}
