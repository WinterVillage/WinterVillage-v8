package de.wintervillage.common.paper.models;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public record Advancements(Collection<Advancement> advancements) {

    public static Advancements generate(Document document) {
        return new Advancements(document.values().stream().map(entry -> {
            Document advancement = (Document) entry;
            return new Advancement(
                    advancement.getString("key"),
                    advancement.get("completedCriteria", Document.class).entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, criteraValue -> (Long) criteraValue.getValue()))
            );
        }).collect(Collectors.toList()));
    }

    public static Advancements generateDefault() {
        return new Advancements(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "Advancements{" +
                "advancements=" + this.advancements +
                '}';
    }
}
