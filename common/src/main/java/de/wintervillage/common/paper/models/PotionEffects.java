package de.wintervillage.common.paper.models;

import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public record PotionEffects(Collection<Effect> effects) {

    public PotionEffects(Collection<Effect> effects) {
        this.effects = new ArrayList<>(effects);
    }

    public Document document() {
        return this.effects.stream()
                .collect(
                        Document::new,
                        (document, effect) -> document.append(effect.key(), effect.document()),
                        Document::putAll
                );
    }

    public static PotionEffects generate(Document document) {
        return new PotionEffects(document.values().stream().map(entry -> {
            Document effect = (Document) entry;
            return new Effect(
                    effect.getString("key"),
                    effect.getInteger("duration"),
                    effect.getInteger("amplifier"),
                    effect.getBoolean("ambient"),
                    effect.getBoolean("particles"),
                    effect.getBoolean("icon")
            );
        }).collect(Collectors.toList()));
    }

    public static PotionEffects generateDefault() {
        return new PotionEffects(new ArrayList<>());
    }

    @Override
    public String toString() {
        return "PotionEffects{" +
                "effects=" + this.effects +
                '}';
    }
}
