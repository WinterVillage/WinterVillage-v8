package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import de.wintervillage.main.plot.impl.PlotImpl;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CreateSubCommand {

    private final WinterVillage winterVillage;

    public CreateSubCommand() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes((source) -> {
                            String name = StringArgumentType.getString(source, "name");
                            final Player player = (Player) source.getSource().getSender();

                            PersistentDataContainer container = player.getPersistentDataContainer();
                            if (!container.has(this.winterVillage.plotHandler.setupBoundingsKey)
                                    || !container.has(this.winterVillage.plotHandler.setupTaskId)) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.not-setting-up")
                                ));
                                return 0;
                            }

                            BoundingBox2D boundingBox = container.get(this.winterVillage.plotHandler.setupBoundingsKey, new BoundingBoxDataType());
                            if (!boundingBox.isDefined()) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.configuration-failed")
                                ));
                                return 0;
                            }

                            boolean intersects = this.winterVillage.plotHandler.getPlotCache().stream()
                                    .map(Plot::boundingBox)
                                    .anyMatch(plot -> plot.intersects(boundingBox));
                            if (intersects) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.plot.bounding-cannot-intersect")
                                ));
                                return 0;
                            }

                            boolean tooLarge = (!player.hasPermission("wintervillage.plot.width_bypass")
                                    && (boundingBox.getWidthX() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH
                                    || boundingBox.getWidthZ() > this.winterVillage.plotHandler.MAX_PLOT_WIDTH));
                            if (tooLarge) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.too-large",
                                                Component.text(this.winterVillage.plotHandler.MAX_PLOT_WIDTH)
                                        )
                                ));
                                return 0;
                            }

                            Plot plot = new PlotImpl(
                                    UUID.randomUUID(),
                                    name,
                                    new Date(),
                                    player.getUniqueId(),
                                    boundingBox,
                                    List.of()
                            );

                            this.winterVillage.plotDatabase.insert(plot)
                                    .thenAccept((v) -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.created")
                                        ));
                                        this.winterVillage.plotHandler.plotCache.add(plot);
                                    })
                                    .exceptionally(throwable -> {
                                        player.sendMessage(Component.join(
                                                this.winterVillage.prefix,
                                                Component.translatable("wintervillage.commands.plot.failed-to-create", throwable.getMessage())
                                        ));
                                        return null;
                                    });

                            this.winterVillage.plotHandler.stopTasks(player);
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}
