package de.wintervillage.common.core.player.data;

import org.bson.Document;
import org.bson.types.Binary;

import java.util.UUID;

import static de.wintervillage.common.core.database.UUIDConverter.fromBytes;
import static de.wintervillage.common.core.database.UUIDConverter.toBinary;

public class WhitelistInformation {

    private UUID from;
    private long whitelisted;

    public WhitelistInformation() { }

    public WhitelistInformation(UUID from, long whitelisted) {
        this.from = from;
        this.whitelisted = whitelisted;
    }

    public UUID from() {
        return this.from;
    }

    public void from(UUID from) {
        this.from = from;
    }

    public long whitelisted() {
        return this.whitelisted;
    }

    public void whitelisted(long whitelisted) {
        this.whitelisted = whitelisted;
    }

    public Document toDocument() {
        return new Document("from", toBinary(this.from))
                .append("whitelisted", this.whitelisted());
    }

    public static WhitelistInformation fromDocument(Document document) {
        return new WhitelistInformation(
                fromBytes(document.get("from", Binary.class).getData()),
                document.getLong("whitelisted")
        );
    }

    @Override
    public String toString() {
        return "WhitelistInformation{" +
                "from=" + this.from +
                ", whitelisted=" + this.whitelisted +
                '}';
    }
}
