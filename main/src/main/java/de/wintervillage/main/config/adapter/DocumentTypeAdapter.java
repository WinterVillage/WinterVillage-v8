package de.wintervillage.main.config.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.wintervillage.main.config.Document;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DocumentTypeAdapter extends TypeAdapter<Document> {

    private final Gson GSON = new Gson();

    @Override
    public void write(JsonWriter jsonWriter, Document document) throws IOException {
        jsonWriter.beginObject();

        document.jsonObject.entrySet().forEach(entry -> {
            try {
                jsonWriter.name(entry.getKey());
                this.GSON.toJson(entry.getValue(), JsonElement.class, jsonWriter);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        });

        jsonWriter.endObject();
    }

    @Override
    public Document read(JsonReader in) throws IOException {
        Document document = new Document();

        in.beginObject();

        while (in.hasNext()) {
            String key = in.nextName();
            JsonElement jsonElement = this.GSON.fromJson(in, JsonElement.class);
            document.jsonObject.add(key, jsonElement);
        }

        in.endObject();
        return document;
    }
}
