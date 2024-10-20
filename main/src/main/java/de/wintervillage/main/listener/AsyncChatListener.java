package de.wintervillage.main.listener;

import de.wintervillage.main.WinterVillage;
import eu.cloudnetservice.driver.channel.ChannelMessage;
import eu.cloudnetservice.driver.channel.ChannelMessageTarget;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.event.EventManager;
import eu.cloudnetservice.driver.event.events.channel.ChannelMessageReceiveEvent;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.network.buffer.DataBuf;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.luckperms.api.model.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class AsyncChatListener implements Listener, ChatRenderer {

    private final WinterVillage winterVillage;

    public AsyncChatListener() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);

        EventManager eventManager = InjectionLayer.ext().instance(EventManager.class);
        eventManager.registerListener(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(AsyncChatEvent event) {
        if (event.signedMessage().message().startsWith("@global")) {
            String message = event.signedMessage().message();
            message = message.replaceFirst("^@global\\s", "");
            if (message.isEmpty()) { // no content, prevents the chat from being flooded with empty messages
                event.renderer(this);
                return;
            }

            Group highestGroup = this.winterVillage.playerHandler.highestGroup(event.getPlayer());
            DataBuf dataBuf = DataBuf.empty()
                    .writeString(event.getPlayer().getName())
                    .writeString(highestGroup.getName())
                    .writeString(message);

            ChannelMessage.builder()
                    .message("wintervillage:chat")
                    .channel("sync_message")
                    .target(ChannelMessageTarget.Type.GROUP, "worlds")
                    .buffer(dataBuf)
                    .build()
                    .sendSingleQuery();

            event.setCancelled(true);
            return;
        }

        event.renderer(this);
    }

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        Group highestGroup = this.winterVillage.playerHandler.highestGroup(source);

        // format: GROUP USERNAME | MESSAGE
        return Component.empty()
                .append(MiniMessage.miniMessage().deserialize(highestGroup.getCachedData().getMetaData().getPrefix()))
                .append(Component.space())
                .append(sourceDisplayName)
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(message);
    }

    @EventListener
    private void handle(ChannelMessageReceiveEvent event) {
        if (event.channel().equals("sync_message") && "wintervillage:chat".equals(event.message())) {
            String playerName = event.content().readString();
            String groupName = event.content().readString();
            if (!this.winterVillage.luckPerms.getGroupManager().isLoaded(groupName)) {
                event.content().release();
                this.winterVillage.getLogger().warning("Received a chat message with an unknown group: " + groupName);
                return;
            }
            String message = event.content().readString();
            Group group = this.winterVillage.luckPerms.getGroupManager().getGroup(groupName);

            // format: @global GROUP USERNAME | MESSAGE
            Bukkit.broadcast(Component.empty()
                    .append(Component.text("@global", NamedTextColor.RED))
                    .append(Component.space())
                    .append(MiniMessage.miniMessage().deserialize(group.getCachedData().getMetaData().getPrefix()))
                    .append(Component.space())
                    .append(Component.text(playerName))
                    .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                    .append(MiniMessage.miniMessage().deserialize(message)));

            event.content().release();
        }
    }
}
