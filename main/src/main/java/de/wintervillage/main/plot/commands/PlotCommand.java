package de.wintervillage.main.plot.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.plot.commands.sub.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class PlotCommand {

    /**
     * /gs setup                    | Starts the setup and gives the player an axe to select the corners
     * /gs info (uuid)              | Gives information about the plot the player is standing on
     * /gs showBorders              | Shows the border of every plot
     * /gs listAll                  | Lists all plots
     * /gs create <name>            | Creates a plot with the name
     * /gs delete (uuid)            | Deletes the plot with the name
     * /gs tp <uuid>                | Teleports to the plot with the uniqueId
     * /gs owner <player>           | Sets the owner of the plot
     * /gs member <player> add      | Adds a player to the plot
     * /gs member <player> remove   | Removes a player from the plot
     */

    public PlotCommand(Commands commands) {
        WinterVillage winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("gs")
                .requires((source) -> source.getSender() instanceof Player)
                .then(new SetupSubCommand().build())
                .then(new InfoSubCommand().build())
                .then(new ShowBordersSubCommand().build())
                .then(new ListAllSubCommand().build())
                .then(new CreateSubCommand().build())
                .then(new DeleteSubCommand().build())
                .then(new TpSubCommand().build())
                .then(new OwnerSubCommand().build())
                .then(new MemberSubCommand().build());
        commands.register(winterVillage.getPluginMeta(), builder.build(), "Manage your plots", List.of("plot"));
    }
}
