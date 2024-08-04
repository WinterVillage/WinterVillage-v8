package de.wintervillage.common.core.player.data;

import org.bson.Document;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.UUID;

public class BanInformation {

    private @Nullable UUID from;
    private String reason;
    private Date created, expiring;

    public BanInformation() { }

    public BanInformation(@Nullable UUID from, String reason, Date created, Date expiring) {
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

    public Document toDocument(BanInformation banInformation) {
        return new Document()
                .append("from", banInformation.from.toString())
                .append("reason", banInformation.reason)
                .append("created", banInformation.created)
                .append("expiring", banInformation.expiring);
    }

    public static BanInformation fromDocument(Document document) {
        return new BanInformation(
                UUID.fromString(document.getString("from")),
                document.getString("reason"),
                document.getDate("created"),
                document.getDate("expiring")
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
