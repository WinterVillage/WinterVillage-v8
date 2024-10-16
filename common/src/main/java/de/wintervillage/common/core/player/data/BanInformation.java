package de.wintervillage.common.core.player.data;

import org.bson.Document;
import org.bson.types.Binary;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static de.wintervillage.common.core.database.UUIDConverter.fromBytes;
import static de.wintervillage.common.core.database.UUIDConverter.toBinary;

public class BanInformation {

    private @NotNull UUID from;
    private String reason;
    private long created, expiring;

    public BanInformation(@NotNull UUID from, String reason, long created, long expiring) {
        this.from = from;
        this.reason = reason;
        this.created = created;
        this.expiring = expiring;
    }

    public @NotNull UUID from() {
        return this.from;
    }

    public void from(@NotNull UUID from) {
        this.from = from;
    }

    public String reason() {
        return this.reason;
    }

    public void reason(String reason) {
        this.reason = reason;
    }

    public long created() {
        return this.created;
    }

    public void created(long created) {
        this.created = created;
    }

    public long expiring() {
        return this.expiring;
    }

    public void expiring(long expiring) {
        this.expiring = expiring;
    }

    public Document toDocument() {
        return new Document()
                .append("from", toBinary(this.from))
                .append("reason", this.reason())
                .append("created", this.created())
                .append("expiring", this.expiring());
    }

    public static BanInformation fromDocument(Document document) {
        return new BanInformation(
                fromBytes(document.get("from", Binary.class).getData()),
                document.getString("reason"),
                document.getLong("created"),
                document.getLong("expiring")
        );
    }

    @Override
    public String toString() {
        return "BanInformation{" +
                "from=" + this.from +
                ", reason='" + this.reason + '\'' +
                ", created=" + this.created +
                ", expiring=" + this.expiring +
                '}';
    }
}
