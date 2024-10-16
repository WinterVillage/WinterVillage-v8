package de.wintervillage.main.shop.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RefreshSubCommand {

    private final WinterVillage winterVillage;

    public RefreshSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("refresh")
                .requires((source) -> source.getSender().hasPermission("wintervillage.shop.command.refresh"))
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    this.winterVillage.shopHandler.forceUpdate((success, message) -> {
                        if (success)
                            player.sendMessage(Component.join(
                                    this.winterVillage.prefix,
                                    Component.translatable("wintervillage.commands.shop.refreshed.success")
                            ));
                        else
                            player.sendMessage(Component.join(
                                    this.winterVillage.prefix,
                                    Component.translatable("wintervillage.commands.shop.refreshed.failed",
                                            Component.text(message)
                                    )
                            ));

                    });
                    return Command.SINGLE_SUCCESS;
                });
    }
}
