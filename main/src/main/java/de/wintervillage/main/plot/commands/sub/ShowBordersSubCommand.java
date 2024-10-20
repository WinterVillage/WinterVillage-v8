package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.task.BoundariesTask;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ShowBordersSubCommand {

    private final WinterVillage winterVillage;

    public ShowBordersSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("showBorders")
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    if (player.getPersistentDataContainer().has(this.winterVillage.plotHandler.showBoundingsKey)) {
                        int taskId = player.getPersistentDataContainer().get(this.winterVillage.plotHandler.showBoundingsKey, PersistentDataType.INTEGER);

                        BoundariesTask task = BoundariesTask.task(taskId);
                        if (task != null) task.stop();

                        player.getPersistentDataContainer().remove(this.winterVillage.plotHandler.showBoundingsKey);
                        return 1;
                    }

                    BoundariesTask task = new BoundariesTask(player);
                    int taskId = task.start();

                    player.getPersistentDataContainer().set(this.winterVillage.plotHandler.showBoundingsKey, PersistentDataType.INTEGER, taskId);

                    return Command.SINGLE_SUCCESS;
                });
    }
}
