package de.wintervillage.common.core.player.data;

import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class MuteInformation {

    private @Nullable UUID from;
    private String reason;
    private Date created, expiring;

    public MuteInformation() { }

    public MuteInformation(@Nullable UUID from, String reason, Date created, Date expiring) {
        this.from = from;
        this.reason = reason;
        this.created = created;
        this.expiring = expiring;
    }

    public @Nullable UUID from() {
        return from;
    }

    public void from(@Nullable UUID from) {
        this.from = from;
    }

    public String reason() {
        return reason;
    }

    public void reason(String reason) {
        this.reason = reason;
    }

    public Date created() {
        return created;
    }

    public void created(Date created) {
        this.created = created;
    }

    public Date expiring() {
        return expiring;
    }

    public void expiring(Date expiring) {
        this.expiring = expiring;
    }

    public Document toDocument() {
        return new Document()
                .append("from", this.from().toString())
                .append("reason", this.reason())
                .append("created", this.created())
                .append("expiring", this.expiring());
    }

    public static MuteInformation fromDocument(Document document) {
        return new MuteInformation(
                UUID.fromString(document.getString("from")),
                document.getString("reason"),
                document.getDate("created"),
                document.getDate("expiring")
        );
    }

    @Override
    public String toString() {
        return "MuteInformation{" +
                "from=" + this.from +
                ", reason='" + this.reason + '\'' +
                ", created=" + this.created +
                ", expiring=" + this.expiring +
                '}';
    }
}
