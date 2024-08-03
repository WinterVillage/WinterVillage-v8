package de.wintervillage.main.economy.shop;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.shop.listener.ListenerEC_BlockBreak;
import de.wintervillage.main.economy.shop.listener.ListenerEC_InventoryClickClose;
import de.wintervillage.main.economy.shop.listener.ListenerEC_PlayerInteract;
import de.wintervillage.main.economy.shop.listener.ListenerEC_SignChange;
import de.wintervillage.main.economy.utils.ItemUtils;
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

    @Inject
    public ShopManager(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        new ListenerEC_BlockBreak(this.winterVillage);
        new ListenerEC_InventoryClickClose(this.winterVillage);
        new ListenerEC_PlayerInteract(this.winterVillage);
        new ListenerEC_SignChange(this.winterVillage);
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

        ItemStack item_space = ItemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, "<color:black>");
        ItemStack item_plus_one = ItemUtils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1, "<color:green><bold>+1</bold>");
        ItemStack item_minus_one = ItemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, "<color:red><bold>-1</bold>");
        ItemStack item_plus_ten = ItemUtils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1, "<color:green><bold>+10</bold>");
        ItemStack item_minus_ten = ItemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, "<color:red><bold>-10</bold>");
        ItemStack item_buy = ItemUtils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1, "<color:green>Kaufen <color:gray>[<color:green><bold>" + shop.getItemPrice() + "</bold><color:gray>]");
        ItemStack item_cancel = ItemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, "<color:red>Abbrechen");

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
