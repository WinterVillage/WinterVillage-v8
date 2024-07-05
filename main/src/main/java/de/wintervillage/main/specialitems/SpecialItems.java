package de.wintervillage.main.specialitems;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.specialitems.listener.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Objects;

public class SpecialItems {

    private WinterVillage winterVillage;

    @Inject
    public SpecialItems(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        new ListenerSI_BlockBreakPlace(this.winterVillage);
        new ListenerSI_Furnace(this.winterVillage);
        new ListenerSI_InventoryClickClose(this.winterVillage);
        new ListenerSI_PlayerInteract(this.winterVillage);
        new ListenerSI_PlayerUpdate(this.winterVillage);
    }

    public ItemStack getSpecialItem(Component name, Material material, int amount, boolean unbreakable){
        ItemStack item = new ItemStack(material, amount);

        ItemMeta meta_item = item.getItemMeta();
        meta_item.displayName(Component.text("SI - ", NamedTextColor.RED, TextDecoration.BOLD).append(name.color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false)));

        List<Component> item_lore = List.of(Component.text("SI - ", NamedTextColor.RED, TextDecoration.BOLD).append(name.color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false)));
        meta_item.lore(item_lore);

        meta_item.setUnbreakable(unbreakable);
        item.setItemMeta(meta_item);

        return item;
    }

    public void setSIBlock(Block block, String key, boolean value){
        block.setMetadata(key, new FixedMetadataValue(this.winterVillage, value));
    }

    public boolean isSIBlock(Block block, String key){
        return block.hasMetadata(key) && block.getMetadata(key).getFirst().asBoolean();
    }

    public boolean isSpecialItem(ItemStack item, Component name){
        if(item == null || item.getItemMeta() == null || item.getItemMeta().displayName() == null || !item.getItemMeta().hasLore())
            return false;

        return Objects.requireNonNull(item.getItemMeta().lore()).getFirst().equals(Component.text("SI - ", NamedTextColor.RED, TextDecoration.BOLD).append(name.color(NamedTextColor.WHITE).decoration(TextDecoration.BOLD, false)));
    }

}
