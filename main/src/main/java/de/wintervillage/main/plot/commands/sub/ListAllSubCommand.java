package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ListAllSubCommand {

    private final WinterVillage winterVillage;

    public ListAllSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("listAll")
                .requires((source) -> source.getSender().hasPermission("wintervillage.plot.command.listAll"))
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    if (this.winterVillage.plotHandler.getPlotCache().isEmpty()) {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.commands.plot.no-plots-found")
                        ));
                        return 1;
                    }

                    // TODO: message
                    return Command.SINGLE_SUCCESS;
                });
    }
}
