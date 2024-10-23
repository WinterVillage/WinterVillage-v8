package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.player.data.TransactionInformation;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.impl.PlotImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ConfirmSubCommand {

    private final WinterVillage winterVillage;

    public ConfirmSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("confirm")
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    PersistentDataContainer container = player.getPersistentDataContainer();
                    if (!container.has(this.winterVillage.plotHandler.confirmCreationKey)) {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.commands.plot.not-setting-up")
                        ));
                        return 0;
                    }

                    String name = container.get(this.winterVillage.plotHandler.confirmCreationKey, PersistentDataType.STRING);
                    BoundingBox2D boundingBox = container.get(this.winterVillage.plotHandler.setupBoundingsKey, new BoundingBoxDataType());

                    BigDecimal cost = this.winterVillage.plotHandler.calculatePrice(player, boundingBox);

                    Plot plot = new PlotImpl(
                            UUID.randomUUID(),
                            name,
                            new Date(),
                            player.getUniqueId(),
                            boundingBox,
                            List.of()
                    );

                    this.winterVillage.playerDatabase.player(player.getUniqueId())
                            .thenCompose(winterVillagePlayer -> {
                                if (winterVillagePlayer.money().compareTo(cost) < 0) {
                                    player.sendMessage(Component.join(
                                            this.winterVillage.prefix,
                                            Component.translatable("wintervillage.command.plot.too-expensive")
                                    ));
                                    return CompletableFuture.completedFuture(null);
                                }

                                CompletableFuture<Void> plotFuture = this.winterVillage.plotDatabase.insert(plot);
                                CompletableFuture<WinterVillagePlayer> playerFuture = this.winterVillage.playerDatabase.modify(winterVillagePlayer.uniqueId(), builder -> {
                                    builder.money(winterVillagePlayer.money().subtract(cost));
                                    builder.addTransaction(new TransactionInformation(
                                            new UUID(0L, 0L),
                                            cost,
                                            "Purchasing plot " + plot.name(),
                                            System.currentTimeMillis()
                                    ));
                                });
                                return plotFuture.thenCombine(playerFuture, (_, modifiedPlayer) -> modifiedPlayer);
                            })
                            .thenAccept(winterVillagePlayer -> {
                                if (winterVillagePlayer == null) return;

                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.created")
                                ));
                                this.winterVillage.plotHandler.plotCache.add(plot);
                            })
                            .exceptionally(throwable -> {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.failed-to-create", Component.text(throwable.getMessage()))
                                ));
                                return null;
                            });

                    this.winterVillage.plotHandler.stopTasks(player);
                    return Command.SINGLE_SUCCESS;
                });
    }
}
