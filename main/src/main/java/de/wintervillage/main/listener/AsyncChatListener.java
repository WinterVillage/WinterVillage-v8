package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;

public class AsyncChatListener implements Listener, ChatRenderer {

    private final WinterVillage winterVillage;

    public AsyncChatListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler
    public void execute(AsyncChatEvent event) {
        event.renderer(this);
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        User user = this.winterVillage.luckPerms.getUserManager().getUser(source.getUniqueId());

        Collection<Group> groups = user.getInheritedGroups(this.winterVillage.luckPerms.getPlayerAdapter(Player.class).getQueryOptions(source));
        Group highestGroup = groups.stream()
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .orElse(this.winterVillage.luckPerms.getGroupManager().getGroup("default"));

        // format: GROUP USERNAME | MESSAGE
        return Component.empty()
                .append(MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getPrefix()))
                .append(Component.space())
                .append(sourceDisplayName)
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(message);
    }
}
