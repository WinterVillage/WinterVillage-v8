package de.wintervillage.main.shop;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.components.util.ItemNbt;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// to replace the NBT Tag given in the constructor
public class CustomGuiItem extends GuiItem {

    private ItemStack itemStack;

    public CustomGuiItem(@NotNull final ItemStack itemStack) {
        this(itemStack, null);
    }

    public CustomGuiItem(@NotNull ItemStack itemStack, @Nullable GuiAction<@NotNull InventoryClickEvent> action) {
        super(itemStack, action);

        // remove the NBT Tag
        this.itemStack = ItemNbt.removeTag(itemStack.clone(), "mf-gui");
    }

    @NotNull
    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
