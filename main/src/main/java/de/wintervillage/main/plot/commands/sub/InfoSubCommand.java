package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.combined.PlotUsers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class InfoSubCommand {

    private final WinterVillage winterVillage;

    public InfoSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                .then(Commands.argument("uniqueId", ArgumentTypes.uuid())
                        .requires((source) -> source.getSender().hasPermission("wintervillage.plot.command.info_by_id"))
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();
                            final UUID uniqueId = source.getArgument("uniqueId", UUID.class);

                            Plot plot = this.winterVillage.plotHandler.byUniqueId(uniqueId);
                            if (plot == null) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.not-found-by-uniqueId", Component.text(uniqueId.toString()))
                                ));
                                return 0;
                            }

                            CompletableFuture<PlotUsers> usersFuture = this.winterVillage.plotHandler.lookupUsers(plot);
                            usersFuture.thenAccept(plotUsers -> {
                                Group highestGroup = this.winterVillage.playerHandler.highestGroup(plotUsers.owner());

                                player.sendMessage(Component.translatable(
                                        "wintervillage.commands.plot.info",
                                        Component.text(plot.name()), // <arg:0>
                                        Component.text(plot.boundingBox().getWidthX() + "x" + plot.boundingBox().getWidthZ()), // <arg:1>
                                        MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + plotUsers.owner().getUsername()), // <arg:2>
                                        Component.text(plot.created().toString()), // <arg:3>
                                        this.winterVillage.plotHandler.formatMembers(plotUsers) // <arg:4>
                                ));
                            });

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    Plot plot = this.winterVillage.plotHandler.byBounds(player.getLocation());
                    if (plot == null) {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.commands.plot.not-found-by-location")
                        ));
                        return 0;
                    }

                    CompletableFuture<PlotUsers> usersFuture = this.winterVillage.plotHandler.lookupUsers(plot);
                    usersFuture.thenAccept(plotUsers -> {
                        Group highestGroup = this.winterVillage.playerHandler.highestGroup(plotUsers.owner());

                        player.sendMessage(Component.translatable(
                                "wintervillage.commands.plot.info",
                                Component.text(plot.name()), // <arg:0>
                                Component.text(plot.boundingBox().getWidthX() + "x" + plot.boundingBox().getWidthZ()), // <arg:1>
                                MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + plotUsers.owner().getUsername()), // <arg:2>
                                Component.text(plot.created().toString()), // <arg:3>
                                this.winterVillage.plotHandler.formatMembers(plotUsers) // <arg:4>
                        ));
                    });

                    return Command.SINGLE_SUCCESS;
                });
    }
}
