package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
                                Component.translatable("wintervillage.commands.plot.no-plots-found")
                        ));
                        return 1;
                    }

                    player.sendMessage(Component.translatable("wintervillage.commands.plot.list-plots"));

                    this.winterVillage.plotHandler.getPlotCache().forEach(plot -> {
                        this.winterVillage.plotHandler.lookupUsers(plot).thenAccept(plotUsers -> {
                            Group highestGroup = this.winterVillage.playerHandler.highestGroup(plotUsers.owner());

                            player.sendMessage(this.buildFromContext(
                                    plot.name(),
                                    highestGroup.getCachedData().getMetaData().getMetaValue("color") + plotUsers.owner().getUsername(),
                                    plot.uniqueId().toString())
                            );
                        });
                    });
                    return Command.SINGLE_SUCCESS;
                });
    }

    private Component buildFromContext(String arg0, String arg1, String arg2) {
        return Component.text()
                .append(Component.text("-", NamedTextColor.DARK_GRAY)
                        .decorate(TextDecoration.BOLD))
                .append(Component.space())
                .append(Component.text(arg0, NamedTextColor.YELLOW)
                        .clickEvent(ClickEvent.runCommand("/gs info " + arg2)))
                .append(Component.text(" von ", NamedTextColor.WHITE))
                .append(MiniMessage.miniMessage().deserialize(arg1))
                .append(Component.space())
                .append(Component.text("[Delete]", NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/gs delete " + arg2)))
                .append(Component.space())
                .append(Component.text("[Teleport]", NamedTextColor.BLUE)
                        .clickEvent(ClickEvent.runCommand("/gs tp " + arg2)))
                .build();
    }
}
