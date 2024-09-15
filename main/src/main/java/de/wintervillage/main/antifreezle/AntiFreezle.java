package de.wintervillage.main.antifreezle;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.antifreezle.listener.ListenerAF_BlockBreakBuild;
import de.wintervillage.main.antifreezle.listener.ListenerAF_InventoryClick;
import de.wintervillage.main.antifreezle.listener.ListenerAF_PlayerQuit;
import de.wintervillage.main.specialitems.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class AntiFreezle {

    private WinterVillage winterVillage;

    private ArrayList<ItemStack> anti_tools;
    private HashMap<Player, ArrayList<String>> active_anti_tools;

    @Inject
    public AntiFreezle(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.active_anti_tools = new HashMap<>();

        this.fill_anti_tools();
        this.register_listener();
    }

    public void open_anti_inventory(Player player, Player anti_player){
        Inventory inventory = Bukkit.createInventory(anti_player, 9, "§c§lAntiFreezle §f- " + anti_player.getDisplayName());

        for(int i = 0; i < anti_tools.size(); i++){
            if(i >= inventory.getSize())
                break;

            ItemStack item_anti_tool = anti_tools.get(i);

            if(item_anti_tool.getItemMeta() != null){
                if(is_anti_tool_activated(anti_player, item_anti_tool.getItemMeta().getDisplayName().replace("§c§l", ""))) {
                    ItemMeta meta_item_anti_tool = item_anti_tool.getItemMeta();
                    meta_item_anti_tool.addEnchant(Enchantment.FORTUNE, 1, true);
                    item_anti_tool.setItemMeta(meta_item_anti_tool);
                }
            }

            inventory.setItem(i, anti_tools.get(i));
        }

        player.openInventory(inventory);
    }

    public void activate_anti_tool(Player player, String tool_name){
        if(!active_anti_tools.containsKey(player))
            active_anti_tools.put(player, new ArrayList<>());

        ArrayList<String> active_anti_tools_player = active_anti_tools.get(player);

        if(!active_anti_tools_player.contains(tool_name.toLowerCase()))
            active_anti_tools_player.add(tool_name.toLowerCase());

        active_anti_tools.put(player, active_anti_tools_player);
    }

    public void deactivate_anti_tool(Player player, String tool_name){
        if(!active_anti_tools.containsKey(player))
            return;

        ArrayList<String> active_anti_tools_player = active_anti_tools.get(player);
        active_anti_tools_player.remove(tool_name.toLowerCase());
        active_anti_tools.put(player, active_anti_tools_player);
    }

    public boolean is_anti_tool_activated(Player player, String tool_name){
        if(active_anti_tools.containsKey(player)){
            ArrayList<String> active_anti_tools_player = active_anti_tools.get(player);
            return active_anti_tools_player.contains(tool_name.toLowerCase());
        }

        return false;
    }

    public void clear_anti_tools(Player player){
        active_anti_tools.remove(player);
    }

    private void fill_anti_tools(){
        anti_tools = new ArrayList<>();

        //Tool-Swap -> Swaps the tool in the main hand while block breaking with another tool in the hotbar
        anti_tools.add(ItemUtils.createItemStack(Material.DIAMOND_PICKAXE, 1, "§c§lTool-Swap"));

        //Tool-Break -> While block breaking: The block isn't broken and the tool in the main hand gets dropped instead (with break sound)
        anti_tools.add(ItemUtils.createItemStack(Material.COBBLESTONE, 1, "§c§lTool-Break"));

        //Heart-Taker -> Damages the Player by 1 heart
        anti_tools.add(ItemUtils.createItemStack(Material.RED_DYE, 1, "§c§lHeart-Taker"));
    }

    private void register_listener(){
        new ListenerAF_BlockBreakBuild(this.winterVillage);
        new ListenerAF_PlayerQuit(this.winterVillage);
        new ListenerAF_InventoryClick(this.winterVillage);
    }

}
