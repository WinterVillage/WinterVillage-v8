package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SpecialItemWVE_XPBoost extends SpecialItem {

    public SpecialItemWVE_XPBoost(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("WVE: XPBoost"), Material.ENCHANTED_BOOK, 1, true);
        this.setItem(item);
        this.setNameStr("wve_xpboost");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()) return;

        if(EnchantmentUtils.hasWVEnchantment(event.getPlayer().getInventory().getItemInMainHand(), "xpboost")) {
            Random random = new Random();
            event.setExpToDrop(event.getExpToDrop() * random.nextInt(2, 4));
        }
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent event){
        if(event.getEntity().getKiller() != null){
            Player player = event.getEntity().getKiller();

            if(EnchantmentUtils.hasWVEnchantment(player.getInventory().getItemInMainHand(), "xpboost")) {
                Random random = new Random();
                event.setDroppedExp(event.getDroppedExp() * random.nextInt(2, 4));
            }
        }
    }

}
