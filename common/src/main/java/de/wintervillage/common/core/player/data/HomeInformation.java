package de.wintervillage.common.core.player.data;

import org.bson.Document;

public record HomeInformation(
        String taskName,
        String world,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
    public Document toDocument() {
        return new Document("taskName", this.taskName)
                .append("world", this.world)
                .append("x", this.x)
                .append("y", this.y)
                .append("z", this.z)
                .append("yaw", Float.valueOf(this.yaw))
                .append("pitch", Float.valueOf(this.pitch));
    }

    public static HomeInformation fromDocument(Document document) {
        return new HomeInformation(
                document.getString("taskName"),
                document.getString("world"),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"),
                document.getDouble("yaw").floatValue(),
                document.getDouble("pitch").floatValue());
    }
}
