package de.wintervillage.main.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FreezeCommand {

    private final WinterVillage winterVillage;

    public FreezeCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("freeze")
                .requires((source) -> source.getSender().hasPermission("wintervillage.command.freeze"))
                .then(Commands.literal("everyone")
                        .requires((source) -> source.getSender().hasPermission("wintervillage.command.freeze.everyone"))
                        .executes((source) -> {
                            this.winterVillage.PLAYERS_FROZEN = !this.winterVillage.PLAYERS_FROZEN;

                            String key = this.winterVillage.PLAYERS_FROZEN ? "wintervillage.command.freeze.everyone-frozen" : "wintervillage.command.freeze.everyone-unfrozen";
                            source.getSource().getSender().sendMessage(Component.join(
                                    this.winterVillage.prefix,
                                    Component.translatable(key)
                            ));
                            return 1;
                        })
                )
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes((source) -> {
                            Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).getFirst();
                            Group highestGroup = this.winterVillage.playerHandler.highestGroup(player);

                            boolean isFrozen = player.getPersistentDataContainer().getOrDefault(this.winterVillage.frozenKey, PersistentDataType.BOOLEAN, false);
                            if (isFrozen) {
                                player.getPersistentDataContainer().remove(this.winterVillage.frozenKey);
                                source.getSource().getSender().sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.freeze.player-unfrozen", MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + player.getName())))
                                );
                            } else {
                                player.getPersistentDataContainer().set(this.winterVillage.frozenKey, PersistentDataType.BOOLEAN, true);
                                source.getSource().getSender().sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.freeze.player-frozen", MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + player.getName())))
                                );
                            }
                            return 1;
                        })
                );
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Freeze a specified player or everyone", List.of());
    }
}
