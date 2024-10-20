package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SpecialItemWVE_Unbreakable extends SpecialItem {

    public SpecialItemWVE_Unbreakable(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("WVE: Unbreakable"), Material.ENCHANTED_BOOK, 1, true);
        this.setItem(item);
        this.setNameStr("wve_unbreakable");
    }

}
