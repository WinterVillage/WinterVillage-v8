package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.event.events.PlayerUpdateEvent;
import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

public class SpecialItem_SantasPants extends SpecialItem {

    public SpecialItem_SantasPants(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Pants"), Material.DIAMOND_LEGGINGS, 1, true);
        this.setItem(item);
        this.setNameStr("santas_pants");
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event) {
        Player player = event.getPlayer();
        ItemStack leggings_player = player.getInventory().getLeggings();

        if(isSpecialitem(leggings_player)) {
            player.setWalkSpeed(0.35f);
        } else if(player.getWalkSpeed() > 0.2f) {
            player.setWalkSpeed(0.2f);
        }
    }
}
