package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.event.events.PlayerUpdateEvent;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ListenerSI_PlayerUpdate implements Listener {

    private WinterVillage winterVillage;

    public ListenerSI_PlayerUpdate(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerUpdate(PlayerUpdateEvent event){
        Player player = event.getPlayer();
        ItemStack leggings_player = player.getInventory().getLeggings();

        if(this.winterVillage.specialItems.isSpecialItem(leggings_player, Component.text("Santa's Pants"))) {
            player.setWalkSpeed(0.35f);
        } else if(player.getWalkSpeed() > 0.2f) {
            player.setWalkSpeed(0.2f);
        }

        if(player.getOpenInventory() != null && player.getOpenInventory().getTopInventory() != null
            && player.getOpenInventory().title().equals(Component.text("WV Enchantment Table", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){

            Inventory inventory = player.getOpenInventory().getTopInventory();
            ItemStack item = inventory.getItem(10);
            ItemStack item_enchantment = inventory.getItem(12);

            if(item != null && item_enchantment != null && EnchantmentUtils.isWVEnchantment(item_enchantment)
                && item.getAmount() == 1 && EnchantmentUtils.getWVEnchantmentAmount(item) < 4){

                if(inventory.getItem(16) == null || inventory.getItem(16).getType() == Material.AIR){
                    ItemStack item_result = EnchantmentUtils.enchantWV(item, item_enchantment);
                    inventory.setItem(16, item_result);
                }

            } else if(inventory.getItem(16) != null && inventory.getItem(16).getType() != Material.AIR){
                inventory.setItem(16, new ItemStack(Material.AIR));
            }

        }
    }

}
