package de.wintervillage.main.player.listener.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import de.wintervillage.main.player.PlayerHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class AdvancementPacketListener extends PacketAdapter {

    private final PlayerHandler playerHandler;

    public AdvancementPacketListener(JavaPlugin javaPlugin, PlayerHandler playerHandler) {
        super(javaPlugin, ListenerPriority.NORMAL, PacketType.Play.Server.ADVANCEMENTS);
        this.playerHandler = playerHandler;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        final PacketContainer packet = event.getPacket();

        if (!event.getPlayer().getPersistentDataContainer().has(this.playerHandler.applyingKey)) {
            event.setCancelled(false);
            return;
        }

        // modify 'added' | List<AdvancementHolder>
        Optional<List<InternalStructure>> added = packet.getLists(InternalStructure.getConverter()).optionRead(0);
        if (added.isEmpty()) return;

        added.get().forEach(advancementHolder -> {
            // record AdvancementHolder(ResourceLocation id, Advancement value)
            InternalStructure advancement = advancementHolder.getStructures().read(1);

            Optional<InternalStructure> displayInfo = advancement.getOptionalStructures().read(1);
            if (displayInfo.isEmpty()) return;

            displayInfo.get().getBooleans().writeDefaults(); // reset booleans
            displayInfo.get().getBooleans().writeSafely(0, false); // showToast
            displayInfo.get().getBooleans().writeSafely(1, false); // announceChat | somehow it has ZERO impact???
            //displayInfo.get().getBooleans().writeSafely(2, true);  // hidden
        });
    }
}
