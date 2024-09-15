package de.wintervillage.main.shop.codec;

import com.mongodb.MongoClientSettings;
import de.wintervillage.main.shop.impl.ShopImpl;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class ShopCodec implements Codec<ShopImpl> {

    private final Codec<Document> documentCodec;

    public ShopCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public ShopImpl decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = this.documentCodec.decode(reader, decoderContext);
        return ShopImpl.fromDocument(document);
    }

    @Override
    public void encode(BsonWriter writer, ShopImpl value, EncoderContext encoderContext) {
        Document document = value.toDocument();
        this.documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<ShopImpl> getEncoderClass() {
        return ShopImpl.class;
    }
}
