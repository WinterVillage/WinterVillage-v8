package de.wintervillage.common.core.database;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UUIDConverter {

    public static byte[] toBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer.wrap(bytes)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

    public static UUID fromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return new UUID(byteBuffer.getLong(), byteBuffer.getLong());
    }

    public static Binary toBinary(UUID uuid) {
        byte[] bytes = toBytes(uuid);
        return new Binary(BsonBinarySubType.UUID_STANDARD, bytes);
    }
}
