package de.wintervillage.main.calendar.codec;

import de.wintervillage.main.calendar.CalendarDay;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class CalenderDayCodecProvider implements CodecProvider {

    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (aClass == CalendarDay.class) return (Codec<T>) new CalendarDayCodec();
        return null;
    }
}
