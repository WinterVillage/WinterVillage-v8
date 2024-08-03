package de.wintervillage.main.death.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class Listener_PlayerDeath implements Listener {

    private WinterVillage winterVillage;

    public Listener_PlayerDeath(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getPlayer();

        if(player.getKiller() != null) {
            event.deathMessage(this.winterVillage.PREFIX.append(MiniMessage.miniMessage().deserialize("Der Spieler <color:red>" + PlainTextComponentSerializer.plainText().serialize(player.displayName()) + "<color:white> wurde von <color:red>" + PlainTextComponentSerializer.plainText().serialize(player.getKiller().displayName()) + "<color:white> getötet.")).decoration(TextDecoration.ITALIC, false));
        } else {
            event.deathMessage(this.winterVillage.PREFIX.append(MiniMessage.miniMessage().deserialize("Der Spieler <color:red>" + PlainTextComponentSerializer.plainText().serialize(player.displayName()) + "<color:white> ist gestorben.")).decoration(TextDecoration.ITALIC, false));
        }

        Block block_chest = player.getLocation().getBlock();
        block_chest.setType(Material.CHEST);

        Chest chest = (Chest) block_chest.getState();

        int item_amount = 0;
        for(ItemStack item : player.getInventory().getContents()){
            if(item == null) continue;
            item_amount++;
        }

        if(item_amount > 27){
            Block block_second_chest = block_chest.getRelative(BlockFace.WEST);
            block_second_chest.setType(Material.CHEST);
            Chest second_chest = (Chest) block_second_chest.getState();

            org.bukkit.block.data.type.Chest chest_data_chest = (org.bukkit.block.data.type.Chest) chest.getBlockData();
            org.bukkit.block.data.type.Chest chest_data_second_chest = (org.bukkit.block.data.type.Chest) second_chest.getBlockData();

            chest_data_chest.setType(org.bukkit.block.data.type.Chest.Type.RIGHT);
            chest_data_second_chest.setType(org.bukkit.block.data.type.Chest.Type.LEFT);

            chest.setBlockData(chest_data_chest);
            second_chest.setBlockData(chest_data_second_chest);

            chest.update(true, false);
            second_chest.update(true, false);
        }

        for(ItemStack item : player.getInventory().getContents()){
            if(item == null) continue;
            chest.getInventory().addItem(item);
        }

        event.getDrops().clear();

        Block block_sign = block_chest.getRelative(BlockFace.SOUTH);
        block_sign.setType(Material.OAK_SIGN);
        Sign sign = (Sign) block_sign.getState();

        sign.getSide(Side.FRONT).line(0, Component.text("R.I.P."));
        sign.getSide(Side.FRONT).line(1, player.displayName());
        sign.update();

        this.winterVillage.deathManager.setDeathLocation(player, player.getLocation());
    }

}
