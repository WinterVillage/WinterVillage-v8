package de.wintervillage.proxy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.core.type.Pair;
import de.wintervillage.proxy.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;

import java.time.Duration;
import java.time.LocalDateTime;

public class WildcardsCommand {

    /**
     * /wildcards
     * /wildcards player <player>
     * /wildcards modify <player> <amount>
     */

    private final WinterVillage winterVillage;

    public WildcardsCommand(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
    }

    public BrigadierCommand create() {
        LiteralCommandNode<CommandSource> node = BrigadierCommand.literalArgumentBuilder("wildcards")
                .then(BrigadierCommand.literalArgumentBuilder("player")
                        .requires(context -> context.hasPermission("wintervillage.command.wildcards.player"))
                        .then(BrigadierCommand.requiredArgumentBuilder("name", StringArgumentType.word())
                                .executes(context -> {
                                    final String name = context.getArgument("name", String.class);

                                    this.winterVillage.playerHandler.lookupUniqueId(name)
                                            .thenCompose(uniqueId -> this.winterVillage.playerHandler.combinedPlayer(uniqueId, name))
                                            .thenAccept(pair -> {
                                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(pair.first());
                                                int groupWeight = highestGroup.getWeight().orElse(0);

                                                int currentAmount = pair.second().wildcardInformation().currentAmount();
                                                boolean obtainable = pair.second().wildcardInformation().obtainable();

                                                long remainingTime = this.remainingTimeUntilWildcard(pair.second(), groupWeight);

                                                context.getSource().sendMessage(Component.join(
                                                        this.winterVillage.prefix,
                                                        Component.translatable("wintervillage.command.wildcard.player-information",
                                                                MiniMessage.miniMessage().deserialize(pair.first().getCachedData().getMetaData().getMetaValue("color") + name),
                                                                currentAmount == 0 ? Component.text(currentAmount, NamedTextColor.RED) : Component.text(currentAmount, NamedTextColor.GREEN),
                                                                Component.text(pair.second().wildcardInformation().totalReceived()),
                                                                obtainable ? Component.text("✔", NamedTextColor.GREEN) : Component.text("✘", NamedTextColor.RED)
                                                        )
                                                ));
                                                if (obtainable)
                                                    context.getSource().sendMessage(Component.translatable("wintervillage.command.wildcard.time-remaining",
                                                            Component.text(this.formatMillis(remainingTime))
                                                    ));
                                            });
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(BrigadierCommand.literalArgumentBuilder("modify")
                        .requires(context -> context.hasPermission("wintervillage.command.wildcards.modify"))
                        .then(BrigadierCommand.requiredArgumentBuilder("name", StringArgumentType.word())
                                .then(BrigadierCommand.requiredArgumentBuilder("amount", IntegerArgumentType.integer(Integer.MIN_VALUE, Integer.MAX_VALUE))
                                        .executes(context -> {
                                            final String name = context.getArgument("name", String.class);
                                            final int amount = context.getArgument("amount", Integer.class);

                                            this.winterVillage.playerHandler.lookupUniqueId(name)
                                                    .thenCompose(uniqueId -> this.winterVillage.playerHandler.combinedPlayer(uniqueId, name))
                                                    .thenCompose(pair -> this.winterVillage.playerDatabase.modify(pair.second().uniqueId(), builder -> {
                                                                builder.wildcardInformation().currentAmount(builder.wildcardInformation().currentAmount() + amount);
                                                                builder.wildcardInformation().totalReceived(builder.wildcardInformation().totalReceived() + amount);
                                                            })
                                                            .thenApply(player -> Pair.of(pair.first(), player)))
                                                    .thenAccept(pair -> context.getSource().sendMessage(Component.join(
                                                            this.winterVillage.prefix,
                                                            Component.translatable("wintervillage.command.wildcard.modified",
                                                                    MiniMessage.miniMessage().deserialize(pair.first().getCachedData().getMetaData().getMetaValue("color") + name),
                                                                    amount > 0 ? Component.text("+" + amount, NamedTextColor.GREEN) : Component.text(amount, NamedTextColor.RED),
                                                                    Component.text(pair.second().wildcardInformation().currentAmount())
                                                            )
                                                    )));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .executes(context -> {
                    if (!(context.getSource() instanceof Player player)) {
                        context.getSource().sendMessage(Component.translatable("wintervillage.commands.not-a-player"));
                        return Command.SINGLE_SUCCESS;
                    }

                    this.winterVillage.playerHandler.combinedPlayer(player.getUniqueId(), player.getUsername())
                            .thenAccept(pair -> {
                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(player);
                                int groupWeight = highestGroup.getWeight().orElse(0);

                                int currentAmount = pair.second().wildcardInformation().currentAmount();
                                boolean obtainable = pair.second().wildcardInformation().obtainable();

                                long remainingTime = this.remainingTimeUntilWildcard(pair.second(), groupWeight);

                                context.getSource().sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.wildcard.self-information",
                                                MiniMessage.miniMessage().deserialize(pair.first().getCachedData().getMetaData().getMetaValue("color") + player.getUsername()),
                                                currentAmount == 0 ? Component.text(currentAmount, NamedTextColor.RED) : Component.text(currentAmount, NamedTextColor.GREEN),
                                                Component.text(pair.second().wildcardInformation().totalReceived()),
                                                obtainable ? Component.text("✔", NamedTextColor.GREEN) : Component.text("✘", NamedTextColor.RED)
                                        )
                                ));
                                if (obtainable)
                                    context.getSource().sendMessage(Component.translatable("wintervillage.command.wildcard.time-remaining",
                                            Component.text(this.formatMillis(remainingTime))
                                    ));
                            });
                    return Command.SINGLE_SUCCESS;
                })
                .build();
        return new BrigadierCommand(node);
    }

    private String formatMillis(long millis) {
        if (millis < 0) throw new IllegalArgumentException("Duration must be greater than 0");

        Duration duration = Duration.ofMillis(millis);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        StringBuilder result = new StringBuilder();
        if (hours > 0) result.append(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        else if (minutes > 0) result.append(String.format("%02d:%02d", minutes, seconds));
        else if (seconds > 0) result.append(String.format("%02d sec", seconds));
        else result.append("NaN");

        return result.toString();
    }

    private long remainingTimeUntilWildcard(WinterVillagePlayer winterVillagePlayer, int groupWeight) {
        // >= 100 every 8h, < 100 every 20h
        long obtainableAfter = (groupWeight >= 100 ? 8 : 20) * 60 * 60 * 1000;

        long totalPlaytime = winterVillagePlayer.playTime();

        LocalDateTime joined = this.winterVillage.playerHandler.playTime.get(winterVillagePlayer.uniqueId());
        if (joined != null) {
            long onlineTime = Duration.between(joined, LocalDateTime.now()).toMillis();
            totalPlaytime += onlineTime;
        }

        // calculation of the time passed since the last wildcard
        long currentMillis = System.currentTimeMillis();
        long elapsedSinceLastWildcard = currentMillis - winterVillagePlayer.wildcardInformation().lastWildcardReceived();

        // calculation of the remaining time
        long remainingTime = Math.max(obtainableAfter - elapsedSinceLastWildcard, 0);

        if (totalPlaytime < obtainableAfter)
            remainingTime = Math.max(obtainableAfter - totalPlaytime, 0);

        return remainingTime;
    }
}
