package de.wintervillage.common.paper.models;

import org.bson.Document;

public record Generic(
        double maxHealth,
        double health,
        int foodLevel,
        float exhaustion,
        float saturation,
        boolean allowFlight,
        boolean isFlying,
        String gameMode,
        int fireTicks,
        float experience,
        int level
) {

    public Document document() {
        return new Document()
                .append("maxHealth", this.maxHealth())
                .append("health", this.health())
                .append("foodLevel", this.foodLevel())
                .append("exhaustion", this.exhaustion())
                .append("saturation", this.saturation())
                .append("allowFlight", this.allowFlight())
                .append("isFlying", this.isFlying())
                .append("gameMode", this.gameMode())
                .append("fireTicks", this.fireTicks())
                .append("experience", this.experience())
                .append("level", this.level());
    }

    public static Generic generate(Document document) {
        return new Generic(
                document.getDouble("maxHealth"),
                document.getDouble("health"),
                document.getInteger("foodLevel"),
                document.getDouble("exhaustion").floatValue(),
                document.getDouble("saturation").floatValue(),
                document.getBoolean("allowFlight"),
                document.getBoolean("isFlying"),
                document.getString("gameMode"),
                document.getInteger("fireTicks"),
                document.getDouble("experience").floatValue(),
                document.getInteger("level")
        );
    }

    public static Generic generateDefault() {
        return new Generic(
                20.0d,
                20.0d,
                20,
                0.0f,
                5.0f,
                false,
                false,
                "SURVIVAL",
                -20,
                0.0f,
                0
        );
    }

    @Override
    public String toString() {
        return "Generic{" +
                "maxHealth=" + this.maxHealth +
                ", health=" + this.health +
                ", foodLevel=" + this.foodLevel +
                ", exhaustion=" + this.exhaustion +
                ", saturation=" + this.saturation +
                ", allowFlight=" + this.allowFlight +
                ", isFlying=" + this.isFlying +
                ", gameMode='" + this.gameMode + '\'' +
                ", fireTicks=" + this.fireTicks +
                ", experience=" + this.experience +
                ", level=" + this.level +
                '}';
    }
}
