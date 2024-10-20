package de.wintervillage.main.plot.commands.sub;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.common.paper.persistent.BoundingBoxDataType;
import de.wintervillage.common.paper.util.BoundingBox2D;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.Plot;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;

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
                            if (tooLarge && this.winterVillage.plotHandler.byOwner(player.getUniqueId()).isEmpty()) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.commands.plot.too-large",
                                                Component.text(this.winterVillage.plotHandler.MAX_PLOT_WIDTH)
                                        )
                                ));
                                return 0;
                            }

                            BigDecimal cost = this.winterVillage.plotHandler.calculatePrice(player, boundingBox);
                            if (cost.compareTo(BigDecimal.ZERO) == 0) {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.plot.first-plot-is-free")
                                ));
                            } else {
                                player.sendMessage(Component.join(
                                        this.winterVillage.prefix,
                                        Component.translatable("wintervillage.command.plot.cost-confirmation",
                                                Component.text(this.winterVillage.formatBD(cost, true))
                                        )
                                ));
                            }

                            player.getPersistentDataContainer().set(this.winterVillage.plotHandler.confirmCreationKey, PersistentDataType.STRING, name);
                            if (this.winterVillage.plotHandler.byOwner(player.getUniqueId()).isEmpty())
                                player.performCommand("plot confirm");
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}
