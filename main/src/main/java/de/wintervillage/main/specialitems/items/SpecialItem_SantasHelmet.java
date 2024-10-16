package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.event.events.PlayerUpdateEvent;
import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpecialItem_SantasHelmet extends SpecialItem {

    public SpecialItem_SantasHelmet(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Helmet"), Material.DIAMOND_HELMET, 1, true);
        this.setItem(item);
        this.setNameStr("santas_helmet");
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event){
        Player player = event.getPlayer();

        if(isSpecialitem(player.getInventory().getHelmet())){
            if(player.isSwimming()){
                player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 200, 2, false, false, false));
            }

            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 200, 2, false, false, false));
        }
    }

}
