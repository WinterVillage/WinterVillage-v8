package de.wintervillage.common.core.player.data;

import org.bson.Document;

public class WildcardInformation {

    private int amount;

    public WildcardInformation() { }

    public WildcardInformation(int amount) {
        this.amount = amount;
    }

    public int amount() {
        return this.amount;
    }

    public void amount(int amount) {
        this.amount = amount;
    }

    public Document toDocument(WildcardInformation wildcardInformation) {
        return new Document()
                .append("amount", wildcardInformation.amount);
    }

    public static WildcardInformation fromDocument(Document document) {
        return new WildcardInformation(
                document.getInteger("amount")
        );
    }

    @Override
    public String toString() {
        return "WildcardInformation{" +
                "amount=" + amount +
                '}';
    }
}
