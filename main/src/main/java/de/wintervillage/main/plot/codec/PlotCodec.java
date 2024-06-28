package de.wintervillage.main.plot.codec;

import com.mongodb.MongoClientSettings;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.util.BoundingBox2D;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class PlotCodec implements Codec<Plot> {

    private final Codec<Document> documentCodec;

    public PlotCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public Plot decode(BsonReader bsonReader, DecoderContext decoderContext) {
        Document document = this.documentCodec.decode(bsonReader, decoderContext);

        String uniqueId = document.getString("_id");
        String name = document.getString("name");
        Date creation = document.getDate("creation");
        UUID owner = UUID.fromString(document.getString("owner"));

        Document boundingBoxDocument = document.get("boundingBox", Document.class);
        BoundingBox2D boundingBox = new BoundingBox2D(
                boundingBoxDocument.getDouble("minX"),
                boundingBoxDocument.getDouble("minZ"),
                boundingBoxDocument.getDouble("maxX"),
                boundingBoxDocument.getDouble("maxZ")
        );

        List<UUID> members = document.getList("members", String.class).stream()
                .map(UUID::fromString)
                .toList();

        return new Plot(name, uniqueId, creation, owner, boundingBox, members);
    }

    @Override
    public void encode(BsonWriter bsonWriter, Plot plot, EncoderContext encoderContext) {
        Document document = new Document();
        document.put("_id", plot.getUniqueId());
        document.put("name", plot.getName());
        document.put("creation", plot.getCreation());
        document.put("owner", plot.getOwner().toString());

        BoundingBox2D boundingBox = plot.getBoundingBox();
        Document boundingBoxDocument = new Document();
        boundingBoxDocument.put("minX", boundingBox.getMinX());
        boundingBoxDocument.put("minZ", boundingBox.getMinZ());
        boundingBoxDocument.put("maxX", boundingBox.getMaxX());
        boundingBoxDocument.put("maxZ", boundingBox.getMaxZ());
        document.put("boundingBox", boundingBoxDocument);

        List<String> members = plot.getMembers().stream()
                .map(UUID::toString)
                .toList();
        document.put("members", members);

        this.documentCodec.encode(bsonWriter, document, encoderContext);
    }

    @Override
    public Class<Plot> getEncoderClass() {
        return Plot.class;
    }
}
