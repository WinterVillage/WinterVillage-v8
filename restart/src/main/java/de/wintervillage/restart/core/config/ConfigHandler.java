package de.wintervillage.restart.core.config;

import com.google.gson.JsonArray;
import de.wintervillage.common.core.config.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {

    private final Path configPath;
    private final Document config;

    public ConfigHandler(Path configPath) {
        this.configPath = configPath;
        this.config = this.resolve();
    }

    public Document getConfig() {
        return this.config;
    }

    private Document resolve() {
        try {
            Files.createDirectories(this.configPath.getParent());
            if (!Files.exists(this.configPath)) this.createDefaultConfig().save(this.configPath);

            return Document.load(this.configPath);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to resolve configuration", exception);
        }
    }

    private Document createDefaultConfig() {
        Document defaultConfig = new Document();
        defaultConfig.append("scheduled", new JsonArray());
        return defaultConfig;
    }
}
