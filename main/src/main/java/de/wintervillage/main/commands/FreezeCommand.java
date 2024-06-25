package de.wintervillage.main.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class FreezeCommand {

    private final WinterVillage winterVillage;

    public FreezeCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        // TODO: check permission with LuckPerms too
        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("freeze")
                .then(
                        Commands.literal("everyone")
                                // TODO: add extra permission check
                                .executes((source) -> {
                                    this.winterVillage.PLAYERS_FROZEN = !this.winterVillage.PLAYERS_FROZEN;
                                    source.getSource().getExecutor().sendMessage(
                                            this.winterVillage.PREFIX.append(Component.text("Everyone has been ", NamedTextColor.WHITE)
                                                    .append(
                                                            this.winterVillage.PLAYERS_FROZEN
                                                                    ? Component.text("frozen", NamedTextColor.RED)
                                                                    : Component.text("unfrozen", NamedTextColor.GREEN)
                                                    ))
                                    );
                                    return 1;
                                })
                ).then(
                        Commands.argument("player", ArgumentTypes.player())
                                .executes((source) -> {
                                            Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).get(0);

                                            boolean isFrozen = player.getPersistentDataContainer().getOrDefault(this.winterVillage.frozenKey, PersistentDataType.BOOLEAN, false);
                                            if (isFrozen) {
                                                player.getPersistentDataContainer().remove(this.winterVillage.frozenKey);
                                                source.getSource().getExecutor().sendMessage(
                                                        this.winterVillage.PREFIX.append(Component.text(player.getName(), NamedTextColor.AQUA)
                                                                .append(Component.text(" has been unfrozen", NamedTextColor.GREEN))
                                                        ));
                                            } else {
                                                player.getPersistentDataContainer().set(this.winterVillage.frozenKey, PersistentDataType.BOOLEAN, true);
                                                source.getSource().getExecutor().sendMessage(
                                                        this.winterVillage.PREFIX.append(Component.text(player.getName(), NamedTextColor.AQUA)
                                                                .append(Component.text(" has been frozen", NamedTextColor.RED))
                                                        ));
                                            }
                                            return 1;
                                        }
                                )
                );
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "Freeze a specified player or everyone", List.of());
    }
}
