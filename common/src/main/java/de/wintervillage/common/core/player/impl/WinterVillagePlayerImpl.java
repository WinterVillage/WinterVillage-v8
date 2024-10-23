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
import org.jetbrains.annotations.Range;

import java.math.BigDecimal;
import java.util.*;

import static de.wintervillage.common.core.database.UUIDConverter.fromBytes;
import static de.wintervillage.common.core.database.UUIDConverter.toBinary;

public class WinterVillagePlayerImpl implements WinterVillagePlayer {

    @BsonId
    private final @NotNull UUID uniqueId;

    @BsonProperty("vanished")
    private boolean vanished;

    @BsonProperty("money")
    private @NotNull BigDecimal money;

    @BsonProperty("deaths")
    private int deaths;

    @BsonProperty("playTime")
    private long playTime;

    @BsonProperty("transactions")
    private Collection<TransactionInformation> transactions;

    @BsonProperty("banInformation")
    private @Nullable BanInformation banInformation;

    @BsonProperty("muteInformation")
    private @Nullable MuteInformation muteInformation;

    @BsonProperty("playerInformation")
    private @NotNull PlayerInformation playerInformation;

    @BsonProperty("wildcardInformation")
    private WildcardInformation wildcardInformation;

    @BsonProperty("whitelistInformation")
    private @Nullable WhitelistInformation whitelistInformation;

    @BsonProperty("homeInformation")
    private @Nullable HomeInformation homeInformation;

    public WinterVillagePlayerImpl(@NotNull UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.vanished = false;
        this.money = BigDecimal.ZERO;
        this.deaths = 0;
        this.playTime = 0L;

        this.transactions = new ArrayList<>();
        this.playerInformation = new PlayerInformation();
        this.wildcardInformation = new WildcardInformation(0L, 0, 0, true);
    }

    @Override
    public @NotNull UUID uniqueId() {
        return this.uniqueId;
    }

    @Override
    public boolean vanished() {
        return this.vanished;
    }

    @Override
    public void vanished(boolean vanished) {
        this.vanished = vanished;
    }

    @Override
    public @NotNull BigDecimal money() {
        return this.money;
    }

    @Override
    public void money(@NotNull BigDecimal money) {
        this.money = money;
    }

    @Override
    public int deaths() {
        return this.deaths;
    }

    @Override
    public void deaths(@Range(from = 0, to = Integer.MAX_VALUE) int deaths) {
        this.deaths = deaths;
    }

    @Override
    public long playTime() {
        return this.playTime;
    }

    @Override
    public void playTime(long playTime) {
        this.playTime = playTime;
    }

    @Override
    public Collection<TransactionInformation> transactions() {
        return List.copyOf(this.transactions);
    }

    @Override
    public void addTransaction(@NotNull TransactionInformation transactionInformation) {
        this.transactions.add(transactionInformation);
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
    public void wildcardInformation(WildcardInformation wildcardInformation) {
        this.wildcardInformation = wildcardInformation;
    }

    @Override
    public WhitelistInformation whitelistInformation() {
        return this.whitelistInformation;
    }

    @Override
    public void whitelistInformation(@Nullable WhitelistInformation whitelistInformation) {
        this.whitelistInformation = whitelistInformation;
    }

    @Override
    public @Nullable HomeInformation homeInformation() {
        return this.homeInformation;
    }

    @Override
    public void homeInformation(@Nullable HomeInformation homeInformation) {
        this.homeInformation = homeInformation;
    }

    public Document toDocument() {
        Document document = new Document();

        document.put("_id", toBinary(this.uniqueId));
        document.put("vanished", this.vanished);
        document.put("money", this.money);
        document.put("deaths", this.deaths);
        document.put("playTime", this.playTime);

        if (!this.transactions.isEmpty())
            document.put("transactions", this.transactions.stream()
                    .map(TransactionInformation::toDocument)
                    .toList());
        if (this.banInformation != null) document.put("banInformation", this.banInformation.toDocument());
        if (this.muteInformation != null) document.put("muteInformation", this.muteInformation.toDocument());
        document.put("playerInformation", this.playerInformation.toDocument());
        if (this.wildcardInformation != null)
            document.put("wildcardInformation", this.wildcardInformation.toDocument());
        if (this.whitelistInformation != null)
            document.put("whitelistInformation", this.whitelistInformation.toDocument());
        if (this.homeInformation != null) document.put("homeInformation", this.homeInformation.toDocument());

        return document;
    }

    public static WinterVillagePlayerImpl fromDocument(Document document) {
        UUID uniqueId = fromBytes(document.get("_id", Binary.class).getData());

        WinterVillagePlayerImpl player = new WinterVillagePlayerImpl(uniqueId);
        player.vanished(document.getBoolean("vanished", false));
        player.money(Optional.ofNullable(document.get("money", Decimal128.class).bigDecimalValue()).orElse(BigDecimal.ZERO));
        player.deaths(Optional.ofNullable(document.get("deaths", Integer.class)).orElse(0));
        player.playTime(Optional.ofNullable(document.getLong("playTime")).orElse(0L));
        Optional.ofNullable(document.getList("transactions", Document.class))
                .stream()
                .flatMap(List::stream)
                .map(TransactionInformation::fromDocument)
                .forEach(player::addTransaction);
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
        player.homeInformation(Optional.ofNullable(document.get("homeInformation", Document.class))
                .filter(doc -> !doc.isEmpty())
                .map(HomeInformation::fromDocument)
                .orElse(null));

        return player;
    }

    @Override
    public String toString() {
        return String.format("WinterVillagePlayerImpl{uniqueId=%s}", this.uniqueId);
    }
}
