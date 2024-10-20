package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpecialItem_SantasSword extends SpecialItem {

    public SpecialItem_SantasSword(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Sword"), Material.DIAMOND_SWORD, 1, true);
        this.setItem(item);
        this.setNameStr("santas_sword");
    }

    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player player){

            if(isSpecialitem(player.getInventory().getItemInMainHand())) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1, false, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1, false, false, false));
            }

        }
    }

}
