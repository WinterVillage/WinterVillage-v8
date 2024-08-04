package de.wintervillage.main.antifreezle.listener;

import de.wintervillage.main.WinterVillage;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ListenerAF_InventoryClick implements Listener {

    private final WinterVillage winterVillage;

    public ListenerAF_InventoryClick(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player player))
            return;

        if(event.getView().getTitle().toLowerCase().contains("antifreezle")){
            event.setCancelled(true);

            if(event.getInventory().getHolder() == null || !(event.getInventory().getHolder() instanceof Player anti_player))
                return;

            ItemStack item_clicked = event.getCurrentItem();
            if(item_clicked == null || item_clicked.getItemMeta() == null || item_clicked.getType() == Material.AIR)
                return;

            String str_anti_tool_name = item_clicked.getItemMeta().getDisplayName().replace("§c§l", "");

            if(str_anti_tool_name.equalsIgnoreCase("Heart-Taker")){
                anti_player.damage(2.0f);
                return;
            }

            if(this.winterVillage.antiFreezle.is_anti_tool_activated(anti_player, str_anti_tool_name)){
                this.winterVillage.antiFreezle.deactivate_anti_tool(anti_player, str_anti_tool_name);

                ItemMeta meta_item_anti_tool = item_clicked.getItemMeta();
                meta_item_anti_tool.removeEnchantments();
                item_clicked.setItemMeta(meta_item_anti_tool);
            } else {
                this.winterVillage.antiFreezle.activate_anti_tool(anti_player, str_anti_tool_name);

                ItemMeta meta_item_anti_tool = item_clicked.getItemMeta();
                meta_item_anti_tool.addEnchant(Enchantment.FORTUNE, 1, true);
                item_clicked.setItemMeta(meta_item_anti_tool);
            }

        }
    }

}
