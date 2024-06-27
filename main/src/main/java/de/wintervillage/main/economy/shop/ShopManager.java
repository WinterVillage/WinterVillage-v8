package de.wintervillage.main.economy.shop;

import de.wintervillage.main.WinterVillage;
import org.bukkit.Location;
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
        //Database save (Save Location)
    }

    public void removeShop(Shop shop){
        //Database remove (Remove Location)
    }

    public Shop getShop(Location loc){
        //Database get (Get Location)
        return null;
    }

    public void openShopInventory(Shop shop){
        //Inventory for the Shop Owner to Add Items
    }

    public void openSellsInventory(Shop shop){
        //Inventory for the Customer to Buy Items
        //Changeable Amount of Items
        //Agree (including total prize) and Decline Buttons
    }

}
