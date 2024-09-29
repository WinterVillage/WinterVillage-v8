package de.wintervillage.main.shop.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.shop.Shop;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

public class TpSubCommand {

    private final WinterVillage winterVillage;

    public TpSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("tp")
                .requires((source) -> source.getSender().hasPermission("wintervillage.shop.command.teleport"))
                .then(Commands.argument("uniqueId", ArgumentTypes.uuid())
                        .executes((source) -> {
                            final Player player = (Player) source.getSource().getSender();

                            final UUID uniqueId = source.getArgument("uniqueId", UUID.class);
                            Optional<Shop> optional = this.winterVillage.shopHandler.byUniqueId(uniqueId);
                            if (optional.isEmpty()) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.shop.not-found-by-uniqueId", Component.text(uniqueId.toString()))
                                ));
                                return 0;
                            }

                            player.teleportAsync(optional.get().location(), PlayerTeleportEvent.TeleportCause.PLUGIN)
                                    .thenAccept(_ -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.command.shop.teleported")
                                        ));
                                    })
                                    .exceptionally(throwable -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.command.shop.failed-to-teleport", Component.text(throwable.getMessage()))
                                        ));
                                        return null;
                                    });
                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
