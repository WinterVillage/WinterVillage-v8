package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.core.player.WinterVillagePlayer;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.impl.PlotImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CancelSubCommand {

    private final WinterVillage winterVillage;

    public CancelSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("cancel")
                .executes((source) -> {
                    final Player player = (Player) source.getSource().getSender();

                    player.sendMessage(Component.join(
                            this.winterVillage.prefix,
                            Component.translatable("wintervillage.command.plot.cancelled-procedure")
                    ));
                    this.winterVillage.plotHandler.stopTasks(player);
                    return Command.SINGLE_SUCCESS;
                });
    }
}
