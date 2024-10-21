package de.wintervillage.main.scoreboard;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardHandler {

    private final WinterVillage winterVillage;

    private final Map<UUID, Scoreboard> scoreboards;

    private final List<Group> sortedGroups;
    private final Map<String, Integer> groupOrder;

    private static final Component LOGO = Component.empty()
            .append(Component.text("A").font(Key.key("wintervillage", "logo")))
            .append(Component.text("\uF008").font(Key.key("wintervillage", "space")))
            .append(Component.text("B").font(Key.key("wintervillage", "logo")))
            .append(Component.text("\uF008").font(Key.key("wintervillage", "space")))
            .append(Component.text("C").font(Key.key("wintervillage", "logo")));

    @Inject
    public ScoreboardHandler() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.scoreboards = new ConcurrentHashMap<>();

        this.sortedGroups = this.winterVillage.luckPerms.getGroupManager().getLoadedGroups()
                .stream()
                .sorted((group1, group2) -> Integer.compare(group2.getWeight().getAsInt(), group1.getWeight().getAsInt()))
                .toList();

        this.groupOrder = new ConcurrentHashMap<>();
        for (int i = 0; i < this.sortedGroups.size(); i++) {
            this.groupOrder.put(this.sortedGroups.get(i).getName().toLowerCase(), i);
        }
    }

    public void sidebar(Player player) {
        Scoreboard scoreboard = this.getScoreboard(player.getUniqueId());
        Objective objective = scoreboard.registerNewObjective("wv-sidebar", Criteria.DUMMY, Component.empty());
        objective.setAutoUpdateDisplay(true);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.numberFormat(NumberFormat.blank());

        objective.getScore("01_logo").customName(LOGO);
        objective.getScore("02_empty").customName(Component.empty());
        objective.getScore("03_online-text").customName(Component.space().append(Component.text("Online")).append(Component.text(":", NamedTextColor.DARK_GRAY)));
        objective.getScore("04_online_value").customName(Component.space().append(Component.text(Bukkit.getOnlinePlayers().size(), NamedTextColor.GREEN)));
        objective.getScore("05_empty").customName(Component.empty());
        objective.getScore("06_balance-text").customName(Component.space().append(Component.text("Kontostand")).append(Component.text(":", NamedTextColor.DARK_GRAY)));
        objective.getScore("07_balance-value").customName(Component.space().append(Component.text("-- $", NamedTextColor.YELLOW)));
        objective.getScore("08_empty").customName(Component.empty());
        objective.getScore("09_highestgroup-text").customName(Component.space().append(Component.text("Rang")).append(Component.text(":", NamedTextColor.DARK_GRAY)));
        objective.getScore("10_highestgroup-value").customName(Component.space());
        objective.getScore("11_empty").customName(Component.empty());

        player.setScoreboard(scoreboard);
    }

    public void updateScore(Player player, String score, Component value) {
        Scoreboard scoreboard = this.getScoreboard(player.getUniqueId());
        Objective objective = scoreboard.getObjective("wv-sidebar");

        if (objective == null || !objective.getScore(score).isScoreSet())
            return;

        objective.getScore(score).customName(value);
    }

    public void playerList(Player player) {
        Scoreboard scoreboard = this.getScoreboard(player.getUniqueId());

        this.sortedGroups.forEach(group -> {
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
        if (group == null) return "99_default";
        int index = this.groupOrder.getOrDefault(group.getName().toLowerCase(), 99);
        return String.format("%02d_%s", index, group.getName().toLowerCase());
    }
}
