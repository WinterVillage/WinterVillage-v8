package de.wintervillage.common.core.player.data;

import org.bson.Document;

public class WildcardInformation {

    private long lastWildcardReceived;

    private int currentAmount, totalReceived;
    private boolean obtainable;

    public WildcardInformation() {
    }

    public WildcardInformation(long lastWildcardReceived, int currentAmount, int totalReceived, boolean obtainable) {
        this.lastWildcardReceived = lastWildcardReceived;
        this.currentAmount = currentAmount;
        this.totalReceived = totalReceived;
        this.obtainable = obtainable;
    }

    public long lastWildcardReceived() {
        return this.lastWildcardReceived;
    }

    public void lastWildcardReceived(long lastWildcardReceived) {
        this.lastWildcardReceived = lastWildcardReceived;
    }

    public int currentAmount() {
        return this.currentAmount;
    }

    public void currentAmount(int currentAmount) {
        this.currentAmount = currentAmount;
    }

    public int totalReceived() {
        return this.totalReceived;
    }

    public void totalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }

    public boolean obtainable() {
        return this.obtainable;
    }

    public void obtainable(boolean obtainable) {
        this.obtainable = obtainable;
    }

    public Document toDocument() {
        return new Document("lastWildcardReceived", this.lastWildcardReceived)
                .append("currentAmount", this.currentAmount)
                .append("totalReceived", this.totalReceived)
                .append("obtainable", this.obtainable);
    }

    public static WildcardInformation fromDocument(Document document) {
        return new WildcardInformation(
                document.getLong("lastWildcardReceived"),
                document.getInteger("currentAmount"),
                document.getInteger("totalReceived"),
                document.getBoolean("obtainable")
        );
    }

    @Override
    public String toString() {
        return "WildcardInformation{" +
                "lastWildcardReceived=" + this.lastWildcardReceived +
                ", currentAmount=" + this.currentAmount +
                ", totalReceived=" + this.totalReceived +
                ", obtainable=" + this.obtainable +
                '}';
    }
}
