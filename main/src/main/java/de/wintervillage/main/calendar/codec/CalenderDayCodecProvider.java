package de.wintervillage.main.calendar.codec;

import de.wintervillage.main.calendar.impl.CalendarDayImpl;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class CalenderDayCodecProvider implements CodecProvider {

    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (aClass == CalendarDayImpl.class) return (Codec<T>) new CalendarDayCodec();
        return null;
    }
}
