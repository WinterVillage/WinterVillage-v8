package de.wintervillage.main.calendar.codec;

import com.mongodb.MongoClientSettings;
import de.wintervillage.main.calendar.CalendarDay;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Binary;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class CalendarDayCodec implements Codec<CalendarDay> {

    private final Codec<Document> documentCodec;

    public CalendarDayCodec() {
        this.documentCodec = MongoClientSettings.getDefaultCodecRegistry().get(Document.class);
    }

    @Override
    public CalendarDay decode(BsonReader bsonReader, DecoderContext decoderContext) {
        Document document = this.documentCodec.decode(bsonReader, decoderContext);

        int day = document.getInteger("day");

        byte[] bytes = document.get("itemStack", Binary.class).getData();
        ItemStack itemStack = ItemStack.deserializeBytes(bytes);

        List<UUID> opened = document.getList("opened", String.class).stream()
                .map(UUID::fromString)
                .toList();

        return new CalendarDay(day, itemStack, opened);
    }

    @Override
    public void encode(BsonWriter bsonWriter, CalendarDay calendarDay, EncoderContext encoderContext) {
        Document document = new Document();
        document.put("day", calendarDay.getDay());

        document.put("itemStack", new Binary(calendarDay.getItemStack().serializeAsBytes()));
        document.put("opened", calendarDay.getOpened().stream()
                .map(UUID::toString)
                .toList());

        this.documentCodec.encode(bsonWriter, document, encoderContext);
    }

    @Override
    public Class<CalendarDay> getEncoderClass() {
        return CalendarDay.class;
    }
}
