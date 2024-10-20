package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpecialItemWVE_Freeze extends SpecialItem {

    public SpecialItemWVE_Freeze(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("WVE: Freeze"), Material.ENCHANTED_BOOK, 1, true);
        this.setItem(item);
        this.setNameStr("wve_freeze");
    }

    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player player) {
            ItemStack item_in_hand = player.getInventory().getItemInMainHand();

            if(EnchantmentUtils.hasWVEnchantment(item_in_hand, "freeze")) {
                if(event.getEntity() instanceof Player attacked){
                    attacked.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, false, false, false));
                } else if(event.getEntity() instanceof Monster monster){
                    monster.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 2, false, false, false));
                }
            }
        }
    }

}
