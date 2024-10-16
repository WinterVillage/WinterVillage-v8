package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SpecialItem_SantasBow extends SpecialItem {

    public SpecialItem_SantasBow(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Bow"), Material.BOW, 1, true);
        this.setItem(item);
        this.setNameStr("santas_bow");
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event){
        if(event.getEntity() instanceof Player player){
            ItemStack bow = player.getInventory().getItemInMainHand();
            if(isSpecialitem(bow)){
                event.getProjectile().customName(Component.text("santa_arrow"));
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof Arrow arrow){
            if(arrow.customName() != null && arrow.customName().contains(Component.text("santa_arrow"))){
                if(event.getEntity() instanceof Monster monster){
                    Random rdm = new Random();
                    if(rdm.nextInt(2) == 0){
                        event.setDamage(monster.getHealth() * 2);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event){
        if(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent){
            if(damageEvent.getDamager() instanceof Arrow arrow){
                if(arrow.customName() != null && arrow.customName().contains(Component.text("santa_arrow"))){
                    if(!event.getDrops().isEmpty()){
                        Location location = event.getEntity().getLocation();

                        event.getDrops().forEach(drop -> location.getWorld().dropItemNaturally(location, drop));
                        event.setDroppedExp(event.getDroppedExp() * 2);
                    }
                }
            }
        }
    }

}
