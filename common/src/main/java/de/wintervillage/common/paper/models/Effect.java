package de.wintervillage.common.paper.models;

public record Effect(String key, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {

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
