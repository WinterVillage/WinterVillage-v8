package de.wintervillage.main.config.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

public class LocationTypeAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter jsonWriter, Location location) throws IOException {
        jsonWriter.beginObject();

        jsonWriter.name("world").value(location.getWorld().getName());
        jsonWriter.name("x").value(location.getX());
        jsonWriter.name("y").value(location.getY());
        jsonWriter.name("z").value(location.getZ());
        jsonWriter.name("yaw").value(location.getYaw());
        jsonWriter.name("pitch").value(location.getPitch());

        jsonWriter.endObject();
    }

    @Override
    public Location read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();

        String world = null;
        double x = 0, y = 0, z = 0;
        float yaw = 0, pitch = 0;

        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "world":
                    world = jsonReader.nextString();
                    break;
                case "x":
                    x = jsonReader.nextDouble();
                    break;
                case "y":
                    y = jsonReader.nextDouble();
                    break;
                case "z":
                    z = jsonReader.nextDouble();
                    break;
                case "yaw":
                    yaw = (float) jsonReader.nextDouble();
                    break;
                case "pitch":
                    pitch = (float) jsonReader.nextDouble();
                    break;
            }
        }

        jsonReader.endObject();
        if (world == null) throw new IOException("Missing 'world' value for Location");

        return new Location(
                Bukkit.getWorld(world),
                x,
                y,
                z,
                yaw,
                pitch
        );
    }
}
