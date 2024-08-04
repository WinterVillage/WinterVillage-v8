package de.wintervillage.main.calendar.codec;

import com.mongodb.MongoClientSettings;
import de.wintervillage.main.calendar.impl.CalendarDayImpl;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class CalendarDayCodec implements Codec<CalendarDayImpl> {

    private final Codec<Document> documentCodec;

    public CalendarDayCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public CalendarDayImpl decode(BsonReader bsonReader, DecoderContext decoderContext) {
        Document document = this.documentCodec.decode(bsonReader, decoderContext);
        return CalendarDayImpl.fromDocument(document);
    }

    @Override
    public void encode(BsonWriter bsonWriter, CalendarDayImpl calendarDay, EncoderContext encoderContext) {
        Document document = calendarDay.toDocument();
        this.documentCodec.encode(bsonWriter, document, encoderContext);
    }

    @Override
    public Class<CalendarDayImpl> getEncoderClass() {
        return CalendarDayImpl.class;
    }
}
