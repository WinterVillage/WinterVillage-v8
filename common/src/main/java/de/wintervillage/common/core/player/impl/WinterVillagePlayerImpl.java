package de.wintervillage.common.core.player.impl;

import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.data.*;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.Binary;
import org.bson.types.Decimal128;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static de.wintervillage.common.core.database.UUIDConverter.fromBytes;
import static de.wintervillage.common.core.database.UUIDConverter.toBinary;

public class WinterVillagePlayerImpl implements WinterVillagePlayer {

    @BsonId
    private final @NotNull UUID uniqueId;

    @BsonProperty("money")
    private @NotNull BigDecimal money;

    @BsonProperty("banInformation")
    private @Nullable BanInformation banInformation;

    @BsonProperty("muteInformation")
    private @Nullable MuteInformation muteInformation;

    @BsonProperty("playerInformation")
    private @NotNull PlayerInformation playerInformation;

    @BsonProperty("wildcardInformation")
    private @Nullable WildcardInformation wildcardInformation;

    @BsonProperty("whitelistInformation")
    private @Nullable WhitelistInformation whitelistInformation;

    public WinterVillagePlayerImpl(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.money = BigDecimal.ZERO;

        this.playerInformation = new PlayerInformation();
    }

    @Override
    public @NotNull UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    public @NotNull BigDecimal money() {
        return this.money;
    }

    @Override
    public void money(@NotNull BigDecimal money) {
        this.money = money;
    }

    public @Nullable BanInformation banInformation() {
        return this.banInformation;
    }

    @Override
    public void banInformation(@Nullable BanInformation banInformation) {
        this.banInformation = banInformation;
    }

    @Override
    public @Nullable MuteInformation muteInformation() {
        return this.muteInformation;
    }

    @Override
    public void muteInformation(@Nullable MuteInformation muteInformation) {
        this.muteInformation = muteInformation;
    }

    @Override
    public @NotNull PlayerInformation playerInformation() {
        return this.playerInformation;
    }

    @Override
    public void playerInformation(@NotNull PlayerInformation playerInformation) {
        this.playerInformation = playerInformation;
    }

    @Override
    public @Nullable WildcardInformation wildcardInformation() {
        return this.wildcardInformation;
    }

    @Override
    public void wildcardInformation(@Nullable WildcardInformation wildcardInformation) {
        this.wildcardInformation = wildcardInformation;
    }

    @Override
    public @Nullable WhitelistInformation whitelistInformation() {
        return this.whitelistInformation;
    }

    @Override
    public void whitelistInformation(@Nullable WhitelistInformation whitelistInformation) {
        this.whitelistInformation = whitelistInformation;
    }

    public Document toDocument() {
        Document document = new Document();

        document.put("_id", toBinary(this.uniqueId));
        document.put("money", this.money);

        if (this.banInformation != null)
            document.put("banInformation", this.banInformation.toDocument());
        if (this.muteInformation != null)
            document.put("muteInformation", this.muteInformation.toDocument());

        document.put("playerInformation", this.playerInformation.toDocument());

        if (this.wildcardInformation != null)
            document.put("wildcardInformation", this.wildcardInformation.toDocument());

        if (this.whitelistInformation != null)
            document.put("whitelistInformation", this.whitelistInformation.toDocument());

        return document;
    }

    public static WinterVillagePlayerImpl fromDocument(Document document) {
        UUID uniqueId = fromBytes(document.get("_id", Binary.class).getData());

        WinterVillagePlayerImpl player = new WinterVillagePlayerImpl(uniqueId);
        player.money(Optional.ofNullable(document.get("money", Decimal128.class).bigDecimalValue()).orElse(BigDecimal.ZERO));
        player.banInformation(Optional.ofNullable(document.get("banInformation", Document.class))
                .filter(doc -> !doc.isEmpty())
                .map(BanInformation::fromDocument)
                .orElse(null));
        player.muteInformation(Optional.ofNullable(document.get("muteInformation", Document.class))
                .filter(doc -> !doc.isEmpty())
                .map(MuteInformation::fromDocument)
                .orElse(null));
        player.playerInformation(PlayerInformation.fromDocument(document.get("playerInformation", Document.class)));
        player.wildcardInformation(Optional.ofNullable(document.get("wildcardInformation", Document.class))
                .filter(doc -> !doc.isEmpty())
                .map(WildcardInformation::fromDocument)
                .orElse(null));
        player.whitelistInformation(Optional.ofNullable(document.get("whitelistInformation", Document.class))
                .filter(doc -> !doc.isEmpty())
                .map(WhitelistInformation::fromDocument)
                .orElse(null));

        return player;
    }

    @Override
    public String toString() {
        return "WinterVillagePlayerImpl{" +
                "uniqueId=" + this.uniqueId +
                ", money=" + this.money +
                ", banInformation=" + this.banInformation +
                ", muteInformation=" + this.muteInformation +
                ", playerInformation=" + this.playerInformation +
                ", wildcardInformation=" + this.wildcardInformation +
                ", whitelistInformation=" + this.whitelistInformation +
                '}';
    }
}
