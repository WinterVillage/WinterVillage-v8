package de.wintervillage.main.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import eu.cloudnetservice.driver.provider.CloudServiceProvider;
import eu.cloudnetservice.driver.registry.ServiceRegistry;
import eu.cloudnetservice.driver.service.ServiceId;
import eu.cloudnetservice.driver.service.ServiceInfoSnapshot;
import eu.cloudnetservice.modules.bridge.player.PlayerManager;
import eu.cloudnetservice.modules.bridge.player.executor.ServerSelectorType;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnCommand {

    private final WinterVillage winterVillage;

    private final PlayerManager playerManager;
    private final CloudServiceProvider cloudServiceProvider;

    public SpawnCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        ServiceRegistry serviceRegistry = InjectionLayer.ext().instance(ServiceRegistry.class);
        this.playerManager = serviceRegistry.firstProvider(PlayerManager.class);

        this.cloudServiceProvider = InjectionLayer.ext().instance(CloudServiceProvider.class);

        final LiteralArgumentBuilder builder = Commands.literal("spawn")
                .requires((source) -> source.getSender() instanceof Player)
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    String taskName = this.playerManager.onlinePlayer(player.getUniqueId()).connectedService().taskName();

                    /**
                     * not equal -> send via {@link eu.cloudnetservice.modules.bridge.player.executor.PlayerExecutor} to the service, and teleport him delayed
                     * equal -> teleport him
                     */
                    if (taskName.equals("bauwelt")) {
                        player.teleportAsync(
                                Bukkit.getWorld("world").getSpawnLocation(),
                                PlayerTeleportEvent.TeleportCause.PLUGIN
                        );
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.command.spawn.teleported")
                        ));
                        return Command.SINGLE_SUCCESS;
                    }

                    ServiceId randomService = this.findRandomServiceIdByTaskName("bauwelt");
                    if (randomService == null) {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.commands.server-not-available")
                        ));
                        return Command.SINGLE_SUCCESS;
                    }

                    DataBuf dataBuf = DataBuf.empty()
                            .writeUniqueId(player.getUniqueId());

                    ChannelMessage.builder()
                            .message("wintervillage:bauwelt")
                            .channel("teleport_player")
                            .targetService(randomService.name())
                            .buffer(dataBuf)
                            .build()
                            .sendSingleQuery();

                    this.playerManager.playerExecutor(player.getUniqueId()).connect(randomService.name());
                    return Command.SINGLE_SUCCESS;
                });
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Teleport you to the spawn", List.of());
    }

    private @Nullable ServiceId findRandomServiceIdByTaskName(String taskName) {
        return this.cloudServiceProvider.servicesByTask(taskName)
                .stream()
                .filter(ServiceInfoSnapshot::connected)
                .min(ServerSelectorType.RANDOM.comparator())
                .map(ServiceInfoSnapshot::serviceId)
                .orElse(null);
    }
}
