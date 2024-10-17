package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class SpecialItem_SantasBow extends SpecialItem {

    private final NamespacedKey key;

    public SpecialItem_SantasBow() {
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Santa's Bow"), Material.BOW, 1, true);
        this.setItem(item);
        this.setNameStr("santas_bow");

        this.key = new NamespacedKey("wintervillage", "specialitems/santas_bow");
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player && event.getBow() != null) {
            if (!isSpecialitem(event.getBow())) return;

            PersistentDataContainer container = event.getProjectile().getPersistentDataContainer();
            container.set(this.key, PersistentDataType.BOOLEAN, true);
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow arrow) {
            PersistentDataContainer container = arrow.getPersistentDataContainer();

            if (!container.has(this.key, PersistentDataType.BOOLEAN)) return;

            if (!(event.getEntity() instanceof Monster monster)) return;

            Random rnd = new Random();
            if (rnd.nextInt(2) == 0) event.setDamage(monster.getHealth() * 2);
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event) {
        if (!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent)) return;

        if (!(damageEvent.getDamager() instanceof Arrow arrow)) return;
        PersistentDataContainer container = arrow.getPersistentDataContainer();

        if (!container.has(this.key, PersistentDataType.BOOLEAN)) return;

        if (!event.getDrops().isEmpty()) {
            Location location = event.getEntity().getLocation();

            event.getDrops().forEach(drop -> location.getWorld().dropItemNaturally(location, drop));
            event.setDroppedExp(event.getDroppedExp() * 2);
        }
    }

}
