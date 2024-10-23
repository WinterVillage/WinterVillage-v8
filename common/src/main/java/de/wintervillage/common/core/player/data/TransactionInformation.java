package de.wintervillage.common.core.player.data;

import org.bson.Document;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.UUID;

import static de.wintervillage.common.core.database.UUIDConverter.fromBytes;
import static de.wintervillage.common.core.database.UUIDConverter.toBytes;

public class TransactionInformation {

    private UUID who;
    private BigDecimal amount;
    private @Nullable String information;
    private long timestamp;

    public TransactionInformation() {}

    public TransactionInformation(UUID who, BigDecimal amount, @Nullable String information, long timestamp) {
        this.who = who;
        this.amount = amount;
        this.information = information;
        this.timestamp = timestamp;
    }

    public Document toDocument() {
        return new Document("who", toBytes(this.who))
                .append("amount", this.amount)
                .append("information", this.information)
                .append("timestamp", this.timestamp);
    }

    public static TransactionInformation fromDocument(Document document) {
        return new TransactionInformation(
                fromBytes(document.get("who", Binary.class).getData()),
                document.get("amount", Decimal128.class).bigDecimalValue(),
                document.getString("information"),
                document.getLong("timestamp")
        );
    }
}
