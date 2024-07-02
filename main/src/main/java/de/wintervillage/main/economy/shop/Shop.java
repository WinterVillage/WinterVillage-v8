package de.wintervillage.main.economy.shop;

import de.wintervillage.main.WinterVillage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class Shop {

    private WinterVillage winterVillage;

    private Inventory shop_inventory;
    private String shop_name;
    private Location shop_location;
    private Player shop_owner;
    private float item_price;

    public Shop(String shop_name, float item_price, Location shop_location, Player shop_owner){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.shop_name = shop_name;
        this.item_price = item_price;
        this.shop_location = shop_location;
        this.shop_owner = shop_owner;

        this.shop_inventory = Bukkit.createInventory(null, 45,
                shop_name + " - " + this.shop_location.getBlockX() + ":" + this.shop_location.getBlockY() + ":" + this.shop_location.getBlockZ());
    }

    public void setShopInventory(Inventory inventory){
        this.shop_inventory = inventory;
    }

    public void setShopOwner(Player player){
        this.shop_owner = player;
    }

    public void setShopLocation(Location location) {
        this.shop_location = location;
    }

    public void setShopName(String name) {
        this.shop_name = name;
    }

    public void setItemPrice(float price) {
        this.item_price = price;
    }

    public Inventory getShopInventory(){
        return this.shop_inventory;
    }

    public Player getShopOwner(){
        return this.shop_owner;
    }

    public Location getShopLocation(){
        return this.shop_location;
    }

    public String getShopName(){
        return this.shop_name;
    }

    public float getItemPrice(){
        return this.item_price;
    }

    public int getItemAmount(){
        int item_amount = 0;

        for(int i = 0; i < this.shop_inventory.getSize(); i++){
            if(this.shop_inventory.getItem(i) != null){
                item_amount += this.shop_inventory.getItem(i).getAmount();
            }
        }

        return item_amount;
    }

}
