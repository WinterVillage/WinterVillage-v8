package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;

public class SpecialItem_LuckyItemBlock extends SpecialItem {

    ArrayList<ItemStack> items = new ArrayList<>();

    public SpecialItem_LuckyItemBlock(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Lucky Item Block"), Material.SPONGE, 1, true);
        this.setItem(item);
        this.setNameStr("lucky_item_block");

        items.add(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
        items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
        items.add(new ItemStack(Material.DIAMOND, 8));
        items.add(new ItemStack(Material.IRON_INGOT, 16));
        items.add(new ItemStack(Material.GOLD_INGOT, 16));
        items.add(new ItemStack(Material.COAL, 32));
        items.add(new ItemStack(Material.REDSTONE_BLOCK, 32));
        items.add(new ItemStack(Material.BEACON, 1));
        items.add(new ItemStack(Material.LAPIS_LAZULI, 32));
        items.add(new ItemStack(Material.EMERALD, 8));
        items.add(new ItemStack(Material.TRIDENT, 1));
        items.add(new ItemStack(Material.NETHERITE_INGOT, 2));
        items.add(new ItemStack(Material.CARROT, 1));
        items.add(new ItemStack(Material.POISONOUS_POTATO, 1));
        items.add(new ItemStack(Material.MACE, 1));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.isCancelled()) return;

        if(isSpecialitem(event.getItemInHand())){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "lucky_item_block", true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()) return;

        if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "lucky_item_block")){
            event.setDropItems(false);

            Random random = new Random();

            int total_item_amount = random.nextInt(4, 11);

            while(total_item_amount > 0){
                ItemStack item = items.get(random.nextInt(items.size()));
                event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);
                total_item_amount--;
            }
        }
    }

}
