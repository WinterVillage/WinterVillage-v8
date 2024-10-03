package de.wintervillage.main.commands.home;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.core.player.data.HomeInformation;
import de.wintervillage.main.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SetHomeCommand {

    private final WinterVillage winterVillage;

    public SetHomeCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        ServiceRegistry serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry.class);
        PlayerManager playerManager = serviceRegistry.firstProvider(PlayerManager.class);

        final LiteralArgumentBuilder builder = Commands.literal("sethome")
                .requires((source) -> source.getSender() instanceof Player)
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    HomeInformation homeInformation = new HomeInformation(
                            playerManager.onlinePlayer(player.getUniqueId()).connectedService().taskName(),
                            player.getLocation().getWorld().getName(),
                            player.getLocation().getX(),
                            player.getLocation().getY(),
                            player.getLocation().getZ(),
                            player.getLocation().getYaw(),
                            player.getLocation().getPitch()
                    );

                    this.winterVillage.playerDatabase.modify(player.getUniqueId(), winterVillagePlayer -> winterVillagePlayer.homeInformation(homeInformation))
                            .thenAccept(_ -> {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.set-home.success")
                                ));
                            })
                            .exceptionally(throwable -> {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.set-home.failed",
                                                Component.text(throwable.getMessage())
                                        )
                                ));
                                throwable.printStackTrace();
                                return null;
                            });
                    return 1;
                });
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Set a home with your current location", List.of());
    }
}
