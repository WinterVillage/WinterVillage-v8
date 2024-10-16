package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.event.events.PlayerUpdateEvent;
import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class SpecialItem_SantasShield extends SpecialItem {

    public SpecialItem_SantasShield(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Shield"), Material.SHIELD, 1, true);
        this.setItem(item);
        this.setNameStr("santas_shield");
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event){
        Player player = event.getPlayer();

        if(isSpecialitem(player.getInventory().getItemInMainHand()) || isSpecialitem(player.getInventory().getItemInOffHand())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 1, false, false, false));

        }
    }

    @EventHandler
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof Player player){
            if(isSpecialitem(player.getInventory().getItemInMainHand()) || isSpecialitem(player.getInventory().getItemInOffHand())) {
                Random random = new Random();

                if(random.nextInt(3) == 0){
                    if(event.getDamager() instanceof Player attacker){
                        attacker.setHealth(attacker.getHealthScale() - event.getDamage() * 0.5);
                    } else if(event.getDamager() instanceof Monster monster){
                        monster.setHealth(monster.getHealth() - event.getDamage() * 0.5);
                    }
                    event.setDamage(event.getDamage() * 0.5);
                }

            }
        }
    }
}
