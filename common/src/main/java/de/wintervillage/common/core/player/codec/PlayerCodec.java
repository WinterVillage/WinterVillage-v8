package de.wintervillage.common.core.player.codec;

import com.mongodb.MongoClientSettings;
import de.wintervillage.common.core.player.impl.WinterVillagePlayerImpl;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class PlayerCodec implements Codec<WinterVillagePlayerImpl> {

    private final Codec<Document> documentCodec;

    public PlayerCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public WinterVillagePlayerImpl decode(BsonReader bsonReader, DecoderContext decoderContext) {
        Document document = this.documentCodec.decode(bsonReader, decoderContext);
        return WinterVillagePlayerImpl.fromDocument(document);
    }

    @Override
    public void encode(BsonWriter bsonWriter, WinterVillagePlayerImpl winterVillagePlayer, EncoderContext encoderContext) {
        Document document = winterVillagePlayer.toDocument();
        this.documentCodec.encode(bsonWriter, document, encoderContext);
    }

    @Override
    public Class<WinterVillagePlayerImpl> getEncoderClass() {
        return WinterVillagePlayerImpl.class;
    }
}
