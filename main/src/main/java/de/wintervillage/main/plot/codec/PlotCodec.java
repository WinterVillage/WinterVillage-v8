package de.wintervillage.main.plot.codec;

import com.mongodb.MongoClientSettings;
import de.wintervillage.main.plot.impl.PlotImpl;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class PlotCodec implements Codec<PlotImpl> {

    private final Codec<Document> documentCodec;

    public PlotCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public PlotImpl decode(BsonReader bsonReader, DecoderContext decoderContext) {
        Document document = this.documentCodec.decode(bsonReader, decoderContext);
        return PlotImpl.fromDocument(document);
    }

    @Override
    public void encode(BsonWriter bsonWriter, PlotImpl plot, EncoderContext encoderContext) {
        Document document = plot.toDocument();
        this.documentCodec.encode(bsonWriter, document, encoderContext);
    }

    @Override
    public Class<PlotImpl> getEncoderClass() {
        return PlotImpl.class;
    }
}
