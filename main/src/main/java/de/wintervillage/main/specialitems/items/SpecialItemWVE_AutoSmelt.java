package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import de.wintervillage.main.specialitems.utils.EnchantmentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class SpecialItemWVE_AutoSmelt extends SpecialItem {

    public SpecialItemWVE_AutoSmelt() {
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("WVE: AutoSmelt"), Material.ENCHANTED_BOOK, 1, true);
        this.setItem(item);
        this.setNameStr("wve_autosmelt");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        // Cancelled
        // | If the player tries to place a specialitem within a plot where he is not allowed to
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ItemStack item_in_hand = player.getInventory().getItemInMainHand();
        World world = event.getBlock().getWorld();

        if(EnchantmentUtils.hasWVEnchantment(item_in_hand, "autosmelt")){
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
