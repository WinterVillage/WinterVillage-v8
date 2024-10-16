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

public class SpecialItem_LuckyBuildBlock extends SpecialItem {

    ArrayList<ItemStack> items = new ArrayList<>();

    public SpecialItem_LuckyBuildBlock(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Lucky Build Block"), Material.SPONGE, 1, true);
        this.setItem(item);
        this.setNameStr("lucky_build_block");

        items.add(new ItemStack(Material.STONE));
        items.add(new ItemStack(Material.COBWEB));
        items.add(new ItemStack(Material.ACACIA_LOG));
        items.add(new ItemStack(Material.STRIPPED_ACACIA_LOG));
        items.add(new ItemStack(Material.BIRCH_LOG));
        items.add(new ItemStack(Material.STRIPPED_BIRCH_LOG));
        items.add(new ItemStack(Material.CHERRY_LOG));
        items.add(new ItemStack(Material.STRIPPED_CHERRY_LOG));
        items.add(new ItemStack(Material.JUNGLE_LOG));
        items.add(new ItemStack(Material.STRIPPED_JUNGLE_LOG));
        items.add(new ItemStack(Material.DARK_OAK_LOG));
        items.add(new ItemStack(Material.STRIPPED_OAK_LOG));
        items.add(new ItemStack(Material.SPRUCE_LOG));
        items.add(new ItemStack(Material.STRIPPED_SPRUCE_LOG));
        items.add(new ItemStack(Material.MANGROVE_LOG));
        items.add(new ItemStack(Material.STRIPPED_MANGROVE_LOG));
        items.add(new ItemStack(Material.CLAY));
        items.add(new ItemStack(Material.BAMBOO_BLOCK));
        items.add(new ItemStack(Material.GLASS));
        items.add(new ItemStack(Material.GLOWSTONE));
        items.add(new ItemStack(Material.MOSS_BLOCK));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event){
        if(event.isCancelled()) return;

        if(isSpecialitem(event.getItemInHand())){
            this.winterVillage.specialItems.setSIBlock(event.getBlock(), "lucky_build_block", true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event){
        if(event.isCancelled()) return;

        if(this.winterVillage.specialItems.isSIBlock(event.getBlock(), "lucky_build_block")){
            event.setDropItems(false);

            Random random = new Random();

            int total_item_amount = 64 * random.nextInt(1, 5);

            while(total_item_amount > 0){
                int current_item_amount = 16 * random.nextInt(1, 5);

                if(current_item_amount > total_item_amount){
                    current_item_amount = total_item_amount;
                }

                ItemStack item = items.get(random.nextInt(items.size()));
                item.setAmount(current_item_amount);
                event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), item);

                total_item_amount -= current_item_amount;
            }
        }
    }

}
