package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.task.SetupTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class SetupSubCommand {

    private final WinterVillage winterVillage;

    public SetupSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("setup")
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    if (player.getPersistentDataContainer().has(this.winterVillage.plotHandler.setupBoundingsKey)
                            || player.getPersistentDataContainer().has(this.winterVillage.plotHandler.setupTaskId)) {
                        player.sendMessage(Component.join(
                                this.winterVillage.prefix,
                                Component.translatable("wintervillage.commands.plot.already-setting-up")
                        ));
                        return 0;
                    }

                    SetupTask rectangle = new SetupTask(player);
                    int taskId = rectangle.start();

                    player.getPersistentDataContainer().set(this.winterVillage.plotHandler.setupTaskId, PersistentDataType.INTEGER, taskId);
                    player.getPersistentDataContainer().set(this.winterVillage.plotHandler.setupBoundingsKey, new BoundingBoxDataType(), new BoundingBox2D());
                    player.getInventory().addItem(this.winterVillage.plotHandler.SETUP_ITEM);

                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.commands.plot.setting-up", Component.text(this.winterVillage.plotHandler.MAX_PLOT_WIDTH))
                    ));

                    return Command.SINGLE_SUCCESS;
                });
    }
}
