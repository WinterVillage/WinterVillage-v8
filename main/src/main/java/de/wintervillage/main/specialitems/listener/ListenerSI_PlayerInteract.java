package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class ListenerSI_PlayerInteract implements Listener {

    private WinterVillage winterVillage;

    public ListenerSI_PlayerInteract(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null){
            Block block = event.getClickedBlock();

            if(this.winterVillage.specialItems.isSIBlock(block, "disenchantment_table")){
                if(player.getInventory().getItemInMainHand().getType() != Material.AIR){
                    EnchantmentUtils.openDisenchantmentTable(player, player.getInventory().getItemInMainHand());
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }
                event.setCancelled(true);
            } else if(this.winterVillage.specialItems.isSIBlock(block, "wve_table")){
                EnchantmentUtils.openWVEnchantmentTable(player);
                event.setCancelled(true);
            }
        }

        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR){
            if(player.getInventory().getItemInMainHand().getType() != Material.AIR){

                if(this.winterVillage.specialItems.isSpecialItem(player.getInventory().getItemInMainHand(), Component.text("Backpack"))){
                    BlockStateMeta blockStateMeta = (BlockStateMeta) player.getInventory().getItemInMainHand().getItemMeta();
                    ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

                    Inventory inventory_backpack = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, Component.text("Backpack", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
                    inventory_backpack.setContents(shulkerBox.getInventory().getContents());

                    //TODO: Load Inventory from Database

                    player.openInventory(inventory_backpack);

                    event.setCancelled(true);
                }

            }
        }
    }

}
