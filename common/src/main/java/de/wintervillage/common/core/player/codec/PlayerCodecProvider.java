package de.wintervillage.common.core.player.codec;

import de.wintervillage.common.core.player.impl.WinterVillagePlayerImpl;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class PlayerCodecProvider implements CodecProvider {

    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if (aClass == WinterVillagePlayerImpl.class) return (Codec<T>) new PlayerCodec();
        return null;
    }
}
