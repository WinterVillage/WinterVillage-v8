package de.wintervillage.main.shop.commands.sub;

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
                .requires((source) -> source.getSender().hasPermission("wintervillage.shop.command.listAll"))
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    if (this.winterVillage.shopHandler.shops().isEmpty()) {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.commands.shop.listAll.no-shops-found")
                        ));
                        return 1;
                    }

                    player.sendMessage(Component.translatable("wintervillage.commands.shop.listAll.shops-found"));

                    this.winterVillage.shopHandler.shops().forEach(shop -> {
                        this.winterVillage.luckPerms.getUserManager().loadUser(shop.owner())
                                .thenAccept(user -> {
                                    Group highestGroup = this.winterVillage.playerHandler.highestGroup(user);

                                    player.sendMessage(Component.translatable("wintervillage.commands.shop.listAll.shop-info",
                                            Component.text(shop.name(), NamedTextColor.BLUE),
                                            MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getMetaValue("color") + user.getUsername()),
                                            Component.text("[Delete]", NamedTextColor.RED)
                                                    .clickEvent(ClickEvent.runCommand("/shop delete " + shop.uniqueId().toString())),
                                            Component.text("[Info]", NamedTextColor.GRAY)
                                                    .clickEvent(ClickEvent.runCommand("/shop info " + shop.uniqueId().toString()))
                                    ));
                                });
                    });
                    return Command.SINGLE_SUCCESS;
                });
    }
}
