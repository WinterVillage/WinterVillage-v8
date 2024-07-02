package de.wintervillage.main.economy.shop;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopManager {

    private WinterVillage winterVillage;

    public ShopManager(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public boolean shopExists(Location loc){
        //Database check (Contains Location?)
        return false;
    }

    public void saveShop(Shop shop){
        if(shopExists(shop.getShopLocation())){
            removeShop(shop);
        }

        //Database save (Save Location)
    }

    public void removeShop(Shop shop){
        //Database remove (Remove Location)
    }

    public Shop getShop(Location loc){
        //Database get (Get Location)
        return null;
    }

    /**
    * Öffnet das Inventar für den Spieler, um Items zu kaufen
    * */
    public void openSellsInventory(Player player, Shop shop){
        Inventory inventory = Bukkit.createInventory(null, 45,
                shop.getShopName() + " - " + shop.getShopLocation().getBlockX() + ":" + shop.getShopLocation().getBlockY() + ":" + shop.getShopLocation().getBlockZ());

        ItemStack item_space = this.winterVillage.itemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, "<color:black>");
        ItemStack item_plus_one = this.winterVillage.itemUtils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1, "<color:green><bold>+1</bold>");
        ItemStack item_minus_one = this.winterVillage.itemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, "<color:red><bold>-1</bold>");
        ItemStack item_plus_ten = this.winterVillage.itemUtils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1, "<color:green><bold>+10</bold>");
        ItemStack item_minus_ten = this.winterVillage.itemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, "<color:red><bold>-10</bold>");
        ItemStack item_buy = this.winterVillage.itemUtils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1, "<color:green>Kaufen <color:gray>[<color:green><bold>" + shop.getItemPrice() + "</bold><color:gray>]");
        ItemStack item_cancel = this.winterVillage.itemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, "<color:red>Abbrechen");

        ItemStack item_showcase = new ItemStack(Material.BARRIER);

        if(shop.getItemAmount() > 0){
            item_showcase = new ItemStack(shop.getShopInventory().getContents()[0].getType());
        }

        for(int i = 0; i < inventory.getSize(); i++){
            inventory.setItem(i, item_space);
        }

        inventory.setItem(10, item_minus_ten);
        inventory.setItem(11, item_minus_one);
        inventory.setItem(13, item_showcase);
        inventory.setItem(15, item_plus_one);
        inventory.setItem(16, item_plus_ten);
        inventory.setItem(30, item_cancel);
        inventory.setItem(32, item_buy);

        player.openInventory(inventory);

    }

}
