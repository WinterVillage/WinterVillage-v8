package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class SpecialItem_SantasKeks extends SpecialItem {

    public SpecialItem_SantasKeks(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Keks"), Material.COOKIE, 1, false);
        this.setItem(item);
        this.setNameStr("santas_keks");
    }

    @EventHandler
    public void onPlayerEat(PlayerItemConsumeEvent event){
        Player player = event.getPlayer();

        if(isSpecialitem(event.getItem())){
            player.setFoodLevel(player.getFoodLevel() + 6);
            event.setReplacement(event.getItem());
        }
    }

}
