package de.wintervillage.main.specialitems.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class ListenerSI_BlockBreakPlace implements Listener {

    private WinterVillage winterVillage;

    public ListenerSI_BlockBreakPlace(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item_in_hand = player.getInventory().getItemInMainHand();
        World world = event.getBlock().getWorld();

        if(this.winterVillage.specialItems.isSpecialItem(item_in_hand, Component.text("TimberAxt"))){
            if(event.getBlock().getType().name().toLowerCase().contains("log")){
                timber(event.getBlock().getLocation());
                event.setCancelled(true);
            }
        } else if(EnchantmentUtils.hasWVEnchantment(item_in_hand, "autosmelt")){
            int fortunteLevel = item_in_hand.getEnchantmentLevel(Enchantment.FORTUNE);

            Bukkit.recipeIterator().forEachRemaining(recipe -> {
                if(recipe instanceof FurnaceRecipe){
                    FurnaceRecipe furnaceRecipe = (FurnaceRecipe) recipe;
                    if(furnaceRecipe.getInput().getType() == event.getBlock().getType()){
                        ItemStack item_smelted = furnaceRecipe.getResult();
                        int amount_smelted = dropAmountWithFortune(fortunteLevel, item_smelted.getAmount());
                        item_smelted.setAmount(amount_smelted);
                        world.dropItemNaturally(event.getBlock().getLocation(), item_smelted);
                        event.setDropItems(false);
                    }
                }
            });

        }

        if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "disenchantment_table")){
            event.setDropItems(false);
            ItemStack item_disenchantment_table = this.winterVillage.specialItems.getSpecialItem(Component.text("Disenchantment Table"), Material.GRINDSTONE, 1, true);
            world.dropItemNaturally(event.getBlock().getLocation(), item_disenchantment_table);
        } else if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "fast_furnace")){
            event.setDropItems(false);
            ItemStack item_fastfunace = this.winterVillage.specialItems.getSpecialItem(Component.text("Fast Furnace"), Material.FURNACE, 1, true);
            world.dropItemNaturally(event.getBlock().getLocation(), item_fastfunace);
        } else if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "wve_table")){
            event.setDropItems(false);
            ItemStack item_wve_table = this.winterVillage.specialItems.getSpecialItem(Component.text("WV Enchantment Table"), Material.ENCHANTING_TABLE, 1, true);
            world.dropItemNaturally(event.getBlock().getLocation(), item_wve_table);
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        ItemStack item_placed = event.getItemInHand();

        if(this.winterVillage.specialItems.isSpecialItem(item_placed, Component.text("Disenchantment Table"))){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "disenchantment_table", true);
        } else if(this.winterVillage.specialItems.isSpecialItem(item_placed, Component.text("Fast Furnace"))){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "fast_furnace", true);
        } else if(this.winterVillage.specialItems.isSpecialItem(item_placed, Component.text("Backpack"))){
            event.setCancelled(true);
        } else if(this.winterVillage.specialItems.isSpecialItem(item_placed, Component.text("WV Enchantment Table"))){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "wve_table", true);
        }
    }

    private void timber(Location location){
        World world = location.getWorld();
        world.getBlockAt(location).breakNaturally();

        for(double alpha = 0; alpha < 2*Math.PI; alpha += Math.PI/4){
            Location location_around = location.clone().add(Math.cos(alpha), 0, Math.sin(alpha));
            if(location_around.getBlock().getType().name().toLowerCase().contains("log")){
                timber(location_around);
            }
        }

        Location location_above = location.clone().add(0, 1, 0);
        if(location_above.getBlock().getType().name().toLowerCase().contains("log")){
            timber(location_above);
        }
    }

    private int dropAmountWithFortune(int fortuneLevel, int baseAmount){
        if(fortuneLevel > 0){
            Random random = new Random();
            int amount_extra = random.nextInt(fortuneLevel + 1);
            return baseAmount + amount_extra;
        }

        if(System.out.checkError())
        {
            System.out.println("Error occured!!");
        }
        return baseAmount;
    }

}
