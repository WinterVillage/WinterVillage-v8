package de.wintervillage.common.core.player.impl;

import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.data.BanInformation;
import de.wintervillage.common.core.player.data.MuteInformation;
import de.wintervillage.common.core.player.data.PlayerInformation;
import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.Decimal128;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

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

    public WinterVillagePlayerImpl(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.money = BigDecimal.ZERO;

        this.playerInformation = new PlayerInformation(
                new PlayerInformation.Inventory(new HashMap<>()),
                new PlayerInformation.EnderChest(new HashMap<>())
        );
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

    public Document toDocument() {
        Document document = new Document();

        document.put("_id", this.uniqueId.toString());
        document.put("money", this.money);

        if (this.banInformation != null)
            document.put("banInformation", this.banInformation.toDocument(this.banInformation));
        if (this.muteInformation != null)
            document.put("muteInformation", this.muteInformation.toDocument(this.muteInformation));

        document.put("playerInformation", this.playerInformation.toDocument(this.playerInformation));

        return document;
    }

    public static WinterVillagePlayerImpl fromDocument(Document document) {
        UUID uniqueId = UUID.fromString(document.getString("_id"));
        BigDecimal money = document.get("money", Decimal128.class).bigDecimalValue();

        WinterVillagePlayerImpl player = new WinterVillagePlayerImpl(uniqueId);
        player.money = money;

        if (document.containsKey("banInformation") && !document.get("banInformation", Document.class).isEmpty()) {
            Document banDocument = document.get("banInformation", Document.class);
            player.banInformation = BanInformation.fromDocument(banDocument);
        }

        if (document.containsKey("muteInformation") && !document.get("muteInformation", Document.class).isEmpty()) {
            Document muteDocument = document.get("muteInformation", Document.class);
            player.muteInformation = MuteInformation.fromDocument(muteDocument);
        }

        PlayerInformation playerInformation = PlayerInformation.fromDocument(document.get("playerInformation", Document.class));
        player.playerInformation = playerInformation;

        return player;
    }

    @Override
    public String toString() {
        return "WinterVillagePlayerImpl{" +
                "uniqueId=" + uniqueId +
                ", money=" + money +
                ", banInformation=" + banInformation +
                ", muteInformation=" + muteInformation +
                ", playerInformation=" + playerInformation +
                '}';
    }
}
