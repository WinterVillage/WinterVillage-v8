package de.wintervillage.main.scoreboard;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardHandler {

    private final WinterVillage winterVillage;

    private final Map<UUID, Scoreboard> scoreboards;

    @Inject
    public ScoreboardHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.scoreboards = new ConcurrentHashMap<>();
    }

    public void playerList(Player player) {
        Scoreboard scoreboard = this.getScoreboard(player.getUniqueId());

        this.winterVillage.luckPerms.getGroupManager().getLoadedGroups().forEach(group -> {
            String teamName = this.teamName(group);

            Team team = scoreboard.getTeam(teamName);
            if (team == null) team = scoreboard.registerNewTeam(teamName);

            team.prefix(MiniMessage.miniMessage().deserialize(group.getCachedData().getMetaData().getPrefix() + " <dark_gray>|</dark_gray> "));
            team.suffix(Component.empty());
        });

        Bukkit.getOnlinePlayers().stream()
                //.filter(onlinePlayer -> !onlinePlayer.getUniqueId().equals(player.getUniqueId()))
                .forEach(onlinePlayer -> {
                    Group highestGroup = this.winterVillage.playerHandler.highestGroup(onlinePlayer);
                    String teamName = this.teamName(highestGroup);

                    Team team = scoreboard.getTeam(teamName);
                    if (team == null) return; // could not apply

                    team.addPlayer(onlinePlayer);
                });

        player.setScoreboard(scoreboard);
    }

    public void playerList() {
        Bukkit.getOnlinePlayers().forEach(this::playerList);
    }

    public Scoreboard getScoreboard(UUID uniqueId) {
        return this.scoreboards.computeIfAbsent(uniqueId, _ -> Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public boolean removeScoreboard(UUID uniqueId) {
        return this.scoreboards.remove(uniqueId) != null;
    }

    private String teamName(@NotNull Group group) {
        if (group == null) return "0_default";
        return group.getWeight().getAsInt() + "_" + group.getName().toLowerCase();
    }
}
