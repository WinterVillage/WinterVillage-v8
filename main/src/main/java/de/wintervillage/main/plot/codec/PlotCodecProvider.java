package de.wintervillage.main.plot.codec;

import de.wintervillage.main.plot.impl.PlotImpl;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class PlotCodecProvider implements CodecProvider {

    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (aClass == PlotImpl.class) return (Codec<T>) new PlotCodec();
        return null;
    }
}
