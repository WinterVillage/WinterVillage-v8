package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class DeleteSubCommand {

    private final WinterVillage winterVillage;

    public DeleteSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("delete")
                .then(Commands.argument("uniqueId", ArgumentTypes.uuid())
                        .requires((source) -> source.getSender().hasPermission("wintervillage.plot.command.force_delete"))
                        .executes((source) -> {
                            UUID uniqueId = source.getArgument("uniqueId", UUID.class);
                            final Player player = (Player) source.getSource().getSender();

                            Plot plot = this.winterVillage.plotHandler.byUniqueId(uniqueId);
                            if (plot == null) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.not-found-by-uniqueId", Component.text(uniqueId.toString()))
                                ));
                                return 0;
                            }

                            this.winterVillage.plotDatabase.delete(plot.uniqueId())
                                    .thenAccept((v) -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.deleted")
                                        ));
                                        this.winterVillage.plotHandler.plotCache.remove(plot);
                                    })
                                    .exceptionally(throwable -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.failed-to-delete", throwable.getMessage())
                                        ));
                                        return null;
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

                    boolean notOwner = !plot.owner().equals(player.getUniqueId());
                    boolean canBypass = player.hasPermission("wintervillage.plot.ignore_owner");
                    if (notOwner && !canBypass) {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.commands.plot.not-owner")
                        ));
                        return 0;
                    }

                    this.winterVillage.plotDatabase.delete(plot.uniqueId())
                            .thenAccept((v) -> {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.deleted")
                                ));
                                this.winterVillage.plotHandler.plotCache.remove(plot);
                            })
                            .exceptionally(throwable -> {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.failed-to-delete", throwable.getMessage())
                                ));
                                return null;
                            });

                    return Command.SINGLE_SUCCESS;
                });
    }
}
