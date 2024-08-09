package de.wintervillage.common.paper.models;

import org.bson.Document;

public record Effect(String key, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {

    public Document document() {
        return new Document("key", this.key())
                .append("duration", this.duration())
                .append("amplifier", this.amplifier())
                .append("ambient", this.ambient())
                .append("particles", this.particles())
                .append("icon", this.icon());
    }

    @Override
    public String toString() {
        return "Effect{" +
                "key='" + this.key + '\'' +
                ", duration=" + this.duration +
                ", amplifier=" + this.amplifier +
                ", ambient=" + this.ambient +
                ", particles=" + this.particles +
                ", icon=" + this.icon +
                '}';
    }
}
