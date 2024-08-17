package de.wintervillage.main.plot.impl;

import com.google.common.collect.ImmutableList;
import de.wintervillage.common.core.database.UUIDConverter;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.plot.Plot;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.Binary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static de.wintervillage.common.core.database.UUIDConverter.*;

public class PlotImpl implements Plot {

    @BsonId
    private final @NotNull UUID uniqueId;

    @BsonProperty("name")
    private final @NotNull String name;

    @BsonProperty("date")
    private final @NotNull Date created;

    @BsonProperty("owner")
    private @NotNull UUID owner;

    @BsonProperty("boundingBox")
    private @NotNull BoundingBox2D boundingBox;

    @BsonProperty("members")
    private @NotNull List<UUID> members;

    public PlotImpl(
            @NotNull UUID uniqueId,
            @NotNull String name,
            @NotNull Date created,
            @NotNull UUID owner,
            @NotNull BoundingBox2D boundingBox,
            @NotNull List<UUID> members
    ) {
        this.uniqueId = uniqueId;
        this.name = name;
        this.created = created;
        this.owner = owner;
        this.boundingBox = boundingBox;
        this.members = new ArrayList<>(members);
    }

    @Override
    public @NotNull UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull Date created() {
        return this.created;
    }

    @Override
    public @NotNull UUID owner() {
        return this.owner;
    }

    @Override
    public void owner(@NotNull UUID owner) {
        this.owner = owner;
    }

    @Override
    public @NotNull BoundingBox2D boundingBox() {
        return this.boundingBox;
    }

    @Override
    public void boundingBox(@NotNull BoundingBox2D boundingBox) {
        this.boundingBox = boundingBox;
    }

    @Override
    public List<UUID> members() {
        return ImmutableList.copyOf(this.members);
    }

    @Override
    public void addMember(UUID uuid) {
        this.members.add(uuid);
    }

    public Document toDocument() {
        return new Document("_id", toBinary(this.uniqueId))
                .append("name", this.name)
                .append("created", this.created)
                .append("owner", toBinary(this.owner))
                .append("boundingBox", this.BBtoDocument())
                .append("members", this.members.stream()
                        .map(UUIDConverter::toBinary)
                        .toList());
    }

    public static PlotImpl fromDocument(Document document) {
        Document boundingBoxDocument = document.get("boundingBox", Document.class);
        BoundingBox2D boundingBox = new BoundingBox2D(
                boundingBoxDocument.getDouble("minX"),
                boundingBoxDocument.getDouble("minZ"),
                boundingBoxDocument.getDouble("maxX"),
                boundingBoxDocument.getDouble("maxZ")
        );

        return new PlotImpl(
                fromBytes(document.get("_id", Binary.class).getData()),
                document.getString("name"),
                document.getDate("created"),
                fromBytes(document.get("owner", Binary.class).getData()),
                boundingBox,
                new ArrayList<>(document.getList("members", Binary.class)
                        .stream()
                        .map(binary -> fromBytes(binary.getData()))
                        .toList())
        );
    }

    private Document BBtoDocument() {
        return new Document("minX", this.boundingBox.getMinX())
                .append("minZ", this.boundingBox.getMinZ())
                .append("maxX", this.boundingBox.getMaxX())
                .append("maxZ", this.boundingBox.getMaxZ());
    }

    @Override
    public String toString() {
        return "PlotImpl{" +
                "uniqueId=" + this.uniqueId +
                ", name='" + this.name + '\'' +
                ", created=" + this.created +
                ", owner=" + this.owner +
                ", boundingBox=" + this.boundingBox +
                ", members=" + this.members +
                '}';
    }
}
