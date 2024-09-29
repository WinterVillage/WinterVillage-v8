package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
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
                                Component.translatable("wintervillage.commands.plot.listAll.no-plots-found")
                        ));
                        return 1;
                    }

                    player.sendMessage(Component.translatable("wintervillage.commands.plot.listAll.plots-found"));

                    this.winterVillage.plotHandler.getPlotCache().forEach(plot -> {
                        this.winterVillage.plotHandler.lookupUsers(plot).thenAccept(plotUsers -> {
                            Group highestGroup = this.winterVillage.playerHandler.highestGroup(plotUsers.owner());

                            player.sendMessage(Component.translatable("wintervillage.commands.plot.listAll.plot-info",
                                    Component.text(plot.name(), NamedTextColor.BLUE)
                                            .clickEvent(ClickEvent.runCommand("/gs info " + plot.uniqueId().toString())),
                                    MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + plotUsers.owner().getUsername()),
                                    Component.text("[Delete]", NamedTextColor.RED)
                                            .clickEvent(ClickEvent.runCommand("/gs delete " + plot.uniqueId().toString())),
                                    Component.text("[Teleport]", NamedTextColor.GREEN)
                                            .clickEvent(ClickEvent.runCommand("/gs tp " + plot.uniqueId().toString()))
                            ));
                        });
                    });
                    return Command.SINGLE_SUCCESS;
                });
    }
}
