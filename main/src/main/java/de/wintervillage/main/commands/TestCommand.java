package de.wintervillage.main.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TestCommand {

    private final WinterVillage winterVillage;

    public TestCommand(Commands commands) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder builder = Commands.literal("test")
                .requires((source) -> source.getSender() instanceof Player)
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.commands.test.usage", Component.text(player.getName(), NamedTextColor.GOLD))
                    ));

                    return 1;
                });
        commands.register(this.winterVillage.getPluginMeta(), builder.build(), "test command", List.of());
    }
}
