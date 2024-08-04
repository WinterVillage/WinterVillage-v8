package de.wintervillage.common.core.player.data;

import org.bson.Document;

import java.util.Date;
import java.util.UUID;

public class WhitelistInformation {

    private UUID from;
    private Date whitelisted;

    public WhitelistInformation() { }

    public WhitelistInformation(UUID from, Date whitelisted) {
        this.from = from;
        this.whitelisted = whitelisted;
    }

    public UUID from() {
        return this.from;
    }

    public void from(UUID from) {
        this.from = from;
    }

    public Date whitelisted() {
        return this.whitelisted;
    }

    public void whitelisted(Date whitelisted) {
        this.whitelisted = whitelisted;
    }

    public Document toDocument(WhitelistInformation whitelistInformation) {
        return new Document()
                .append("from", whitelistInformation.from().toString())
                .append("whitelisted", whitelistInformation.whitelisted());
    }

    public static WhitelistInformation fromDocument(Document document) {
        return new WhitelistInformation(
                UUID.fromString(document.getString("from")),
                document.getDate("whitelisted")
        );
    }

    @Override
    public String toString() {
        return "WhitelistInformation{" +
                "from=" + from +
                ", whitelisted=" + whitelisted +
                '}';
    }
}
