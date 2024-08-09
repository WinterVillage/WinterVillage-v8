package de.wintervillage.common.paper.models;

import org.bson.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record Statistics(
        HashMap<String, Integer> generic,
        HashMap<String, Map<String, Integer>> blocks,
        HashMap<String, Map<String, Integer>> items,
        HashMap<String, Map<String, Integer>> entities
) {

    public Document document() {
        return new Document("generic", this.generic().entrySet().stream()
                        .collect(
                                Document::new,
                                (document, entry) -> document.put(entry.getKey(), entry.getValue()),
                                Document::putAll
                        ))
                .append("blocks", this.blocks().entrySet().stream()
                        .collect(
                                Document::new,
                                (document, entry) -> document.put(entry.getKey(), new Document(entry.getValue())),
                                Document::putAll
                        ))
                .append("items", this.items().entrySet().stream()
                        .collect(
                                Document::new,
                                (document, entry) -> document.put(entry.getKey(), new Document(entry.getValue())),
                                Document::putAll
                        ))
                .append("entities", this.entities().entrySet().stream()
                        .collect(
                                Document::new,
                                (document, entry) -> document.put(entry.getKey(), new Document(entry.getValue())),
                                Document::putAll
                        ));
    }

    public static Statistics generate(Document document) {
        HashMap<String, Integer> generic = new HashMap<>();
        for (var entry : document.get("generic", Document.class).entrySet()) {
            generic.put(entry.getKey(), (Integer) entry.getValue());
        }

        HashMap<String, Map<String, Integer>> blocks = new HashMap<>();
        for (var entry : document.get("blocks", Document.class).entrySet()) {
            Document value = (Document) entry.getValue();
            blocks.put(entry.getKey(), value.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> (Integer) e.getValue()
                    )));
        }

        HashMap<String, Map<String, Integer>> items = new HashMap<>();
        for (var entry : document.get("items", Document.class).entrySet()) {
            Document value = (Document) entry.getValue();
            items.put(entry.getKey(), value.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> (Integer) e.getValue()
                    )));
        }

        HashMap<String, Map<String, Integer>> entities = new HashMap<>();
        for (var entry : document.get("entities", Document.class).entrySet()) {
            Document value = (Document) entry.getValue();
            entities.put(entry.getKey(), value.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> (Integer) e.getValue()
                    )));
        }

        return new Statistics(
                generic,
                blocks,
                items,
                entities
        );
    }

    public static Statistics generateDefault() {
        return new Statistics(
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>()
        );
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "generic=" + this.generic +
                ", blocks=" + this.blocks +
                ", items=" + this.items +
                ", entities=" + this.entities +
                '}';
    }
}
