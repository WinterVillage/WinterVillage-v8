package de.wintervillage.main.adventcalendar;

import com.google.inject.Inject;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.adventcalendar.listener.ListenerAC_InventoryClick;
import de.wintervillage.main.economy.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;

public class AdventCalendar {

    private WinterVillage winterVillage;

    @Inject
    public AdventCalendar(){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        new ListenerAC_InventoryClick(this.winterVillage);
    }

    public void openAdventsCalender(Player player){
        int total_days = 24;
        int current_day = LocalDate.now().getDayOfMonth();

        Inventory inventory_calender = Bukkit.createInventory(null, 54, Component.text("Adventskalender", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));

        ItemStack item_space = ItemUtils.createItemStack(Material.BLACK_STAINED_GLASS_PANE, 1, Component.text(" ", NamedTextColor.BLACK));
        ItemStack item_day;

        for(int i = 0; i < 54; i++){
            inventory_calender.setItem(i, item_space);
        }

        for(int i = 0; i < total_days; i++){
            int index_slot = 10 + i + i/7 * 2;

            if(LocalDate.now().getMonthValue() == 12 || LocalDate.now().getMonthValue() == 1){
                if(i+1 <= current_day){
                    if(isOpened(player, i+1)){
                        item_day = ItemUtils.createItemStack(Material.ORANGE_STAINED_GLASS_PANE, 1, Component.text("Türchen " + (i+1), NamedTextColor.namedColor(0xFFA500)));
                    } else {
                        item_day = ItemUtils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, 1, Component.text("Türchen " + (i+1), NamedTextColor.GREEN));

                    }
                } else {
                    item_day = ItemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, Component.text("Türchen " + (i+1), NamedTextColor.RED));
                }
            } else {
                item_day = ItemUtils.createItemStack(Material.RED_STAINED_GLASS_PANE, 1, Component.text("Türchen " + (i+1), NamedTextColor.RED));
            }

            inventory_calender.setItem(index_slot, item_day);
        }

        player.openInventory(inventory_calender);
    }

    public boolean isOpened(Player player, int day){
        //TODO: Datenbank-Abfrage: Spieler hat Türchen an Tag "day" schon geöffnet?
        return false;
    }

    public void setOpened(Player player, int day, boolean opened){
        //TODO: Datenbank-Schreiben: Spieler hat Türchen an Tag "day" geöffnet
    }

    public ItemStack getReward(int day){
        ItemStack item_reward = new ItemStack(Material.AIR);

        //TODO: Datenbank-Abfrage: Item für Tag "day" holen

        return item_reward;
    }

    public void setReward(int day, ItemStack item){
        //TODO: Datenbank-Schreiben: Item für Tag "day" setzen
    }

}
