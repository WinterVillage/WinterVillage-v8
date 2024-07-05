package de.wintervillage.main.adventcalendar.listener;

import de.wintervillage.main.WinterVillage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ListenerAC_InventoryClick implements Listener {

    private WinterVillage winterVillage;

    public ListenerAC_InventoryClick(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, winterVillage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getView().title().equals(Component.text("Adventskalender", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))){
            event.setCancelled(true);

            if(!(event.getWhoClicked() instanceof Player player))
                return;

            ItemStack item = event.getCurrentItem();

            if(item == null || item.getItemMeta() == null || item.getItemMeta().displayName() == null){
                event.setCancelled(true);
                return;
            }

            ItemMeta meta_item = item.getItemMeta();
            String display_name = PlainTextComponentSerializer.plainText().serialize(meta_item.displayName());

            if(!display_name.contains(" ")){
                event.setCancelled(true);
                return;
            }

            int day = 1;

            try {
                day = Integer.parseInt(display_name.split(" ")[1]);
            } catch (NumberFormatException e){
                event.setCancelled(true);
                return;
            }

            if(item.getType().equals(Material.GREEN_STAINED_GLASS_PANE)){
                this.winterVillage.adventCalendar.setOpened(player, day, true);
                player.sendMessage(this.winterVillage.PREFIX.append(Component.text("Du hast das Türchen " + day + " geöffnet!", NamedTextColor.GREEN)));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

                ItemStack item_reward = this.winterVillage.adventCalendar.getReward(day);

                if(player.getInventory().getContents().length >= player.getInventory().getSize()){
                    player.getLocation().getWorld().dropItem(player.getLocation(), item_reward);
                } else {
                    player.getInventory().addItem(item_reward);
                }

                player.closeInventory();
            } else if(item.getType().equals(Material.ORANGE_STAINED_GLASS_PANE)){
                player.sendMessage(this.winterVillage.PREFIX.append(Component.text("Du hast das Türchen " + day + " bereits geöffnet!", NamedTextColor.RED)));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.closeInventory();
            } else if(item.getType().equals(Material.RED_STAINED_GLASS_PANE)){
                player.sendMessage(this.winterVillage.PREFIX.append(Component.text("Dieses Türchen kann erst ab dem " + day + ". Dezember geöffnet werden!", NamedTextColor.RED)));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                player.closeInventory();
            }
        }
    }

}
