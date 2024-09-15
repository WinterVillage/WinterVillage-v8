package de.wintervillage.main.shop.codec;

import de.wintervillage.main.shop.impl.ShopImpl;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class ShopCodecProvider implements CodecProvider {

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == ShopImpl.class) return (Codec<T>) new ShopCodec();
        return null;
    }
}
