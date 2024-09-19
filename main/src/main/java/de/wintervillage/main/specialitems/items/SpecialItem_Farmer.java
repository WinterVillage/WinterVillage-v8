package de.wintervillage.main.specialitems.items;

import de.wintervillage.main.specialitems.SpecialItem;
import de.wintervillage.main.specialitems.SpecialItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SpecialItem_Farmer extends SpecialItem {

    public SpecialItem_Farmer(){
        super();
        ItemStack item = SpecialItems.getSpecialItem(Component.text("Farmer"), Material.GRINDSTONE, 1, false);
        this.setItem(item);
        this.setNameStr("farmer");
    }

}
