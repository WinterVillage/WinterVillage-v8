package de.wintervillage.main.specialitems;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SpecialItem implements Listener {

    public WinterVillage winterVillage;
    private ItemStack item;
    private String name;

    public SpecialItem(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public Component getName(){
        if(item == null || item.getItemMeta() == null || item.getItemMeta().lore() == null || item.getItemMeta().lore().isEmpty())
            return Component.text("");

        return item.getItemMeta().lore().getFirst();
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setNameStr(String name) {
        this.name = name;
    }

    public String getNameStr() {
        return name;
    }

    public boolean isSpecialitem(ItemStack item){
        return item != null && item.getItemMeta() != null
                && item.getItemMeta().lore() != null && !item.getItemMeta().lore().isEmpty()
                && item.getItemMeta().lore().get(0).equals(this.getName());
    }

}
