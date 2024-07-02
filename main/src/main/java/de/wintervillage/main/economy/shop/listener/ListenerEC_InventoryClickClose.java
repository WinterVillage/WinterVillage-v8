package de.wintervillage.main.economy.shop.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ListenerEC_InventoryClickClose implements Listener {

    private WinterVillage winterVillage;

    public ListenerEC_InventoryClickClose(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getWhoClicked() instanceof Player player){
            String inventory_title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
            Inventory inventory = event.getInventory();

            if(inventory_title.contains(" - ") && inventory_title.contains(":")){
                String shop_name = inventory_title.split(" - ")[0];
                int x = Integer.parseInt(inventory_title.split(" - ")[1].split(":")[0]);
                int y = Integer.parseInt(inventory_title.split(" - ")[1].split(":")[1]);
                int z = Integer.parseInt(inventory_title.split(" - ")[1].split(":")[2]);

                Location inventory_location = new Location(player.getWorld(), x, y, z);
                Shop shop = this.winterVillage.shopManager.getShop(inventory_location);

                if(shop != null){
                    if(!player.equals(shop.getShopOwner())){
                        event.setCancelled(true);

                        ItemStack item = event.getCurrentItem();
                        if(item!=null && !shop.getShopInventory().isEmpty() && item.hasItemMeta() && item.getItemMeta().hasDisplayName()){
                            ItemMeta itemMeta = item.getItemMeta();
                            String display_name = PlainTextComponentSerializer.plainText().serialize(itemMeta.displayName());

                            int index_item_showcase = 13;
                            ItemStack item_showcase = inventory.getItem(index_item_showcase);

                            if(item_showcase == null || item_showcase.getType() == Material.BARRIER)
                                return;

                            if(display_name.contains("+1") && item_showcase.getAmount() < 64){

                                if(item_showcase.getAmount() + 1 <= shop.getItemAmount()){
                                    item_showcase.setAmount(item_showcase.getAmount() + 1);
                                    inventory.setItem(index_item_showcase, item_showcase);
                                }

                            } else if(display_name.contains("-1") && item_showcase.getAmount() > 1) {

                                item_showcase.setAmount(item_showcase.getAmount() - 1);
                                inventory.setItem(index_item_showcase, item_showcase);

                            } else if(display_name.contains("+10") && item_showcase.getAmount() < 55) {

                                if(item_showcase.getAmount() + 10 <= shop.getItemAmount()){
                                    item_showcase.setAmount(item_showcase.getAmount() + 10);
                                    inventory.setItem(index_item_showcase, item_showcase);
                                }

                            } else if(display_name.contains("-10") && item_showcase.getAmount() > 10) {

                                item_showcase.setAmount(item_showcase.getAmount() - 10);
                                inventory.setItem(index_item_showcase, item_showcase);

                            } else if(display_name.contains("Kaufen")) {
                                int total_price = (int) (shop.getItemPrice() * item_showcase.getAmount());

                                if(this.winterVillage.economyManager.getBalance(player) >= total_price){
                                    this.winterVillage.economyManager.transferMoney(player, shop.getShopOwner(), total_price);
                                    player.getInventory().addItem(item_showcase);
                                    player.sendMessage(this.winterVillage.PREFIX + "Du hast " + item_showcase.getAmount() + "x " + item_showcase.getType().name() + " für " + total_price + " abgekauft.");
                                } else {
                                    player.sendMessage(this.winterVillage.PREFIX + "Du hast nicht genug Geld.");
                                }

                                player.closeInventory();

                            } else if(display_name.contains("Abbrechen")) {
                                player.closeInventory();
                            }

                            int index_item_buy = 32;
                            ItemStack item_buy = inventory.getItem(index_item_buy);

                            if(item_buy != null && item_buy.hasItemMeta()){
                                ItemMeta itemMetaBuy = item_buy.getItemMeta();

                                float total_price = shop.getItemPrice() * inventory.getItem(index_item_showcase).getAmount();

                                itemMetaBuy.displayName(Component.text("<color:green>Kaufen <color:gray>[<color:green><bold>" + total_price + "</bold><color:gray>]"));
                                item_buy.setItemMeta(itemMetaBuy);

                                inventory.setItem(index_item_buy, item_buy);
                            }
                        }
                    } else {
                        //Nur ein Item-Typ pro Shop
                        if(!event.getInventory().isEmpty()){
                            ItemStack itemStack = event.getInventory().getContents()[0];

                            if(event.getCurrentItem() != null && event.getCurrentItem().getType() != itemStack.getType()){
                                event.setCancelled(true);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if(event.getPlayer() instanceof Player player){
            String inventory_title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

            if(inventory_title.contains(" - ") && inventory_title.contains(":")){
                String shop_name = inventory_title.split(" - ")[0];
                int x = Integer.parseInt(inventory_title.split(" - ")[1].split(":")[0]);
                int y = Integer.parseInt(inventory_title.split(" - ")[1].split(":")[1]);
                int z = Integer.parseInt(inventory_title.split(" - ")[1].split(":")[2]);

                Location inventory_location = new Location(player.getWorld(), x, y, z);
                Shop shop = this.winterVillage.shopManager.getShop(inventory_location);

                if(shop != null){
                    if(player.equals(shop.getShopOwner())){
                        shop.setShopInventory(event.getInventory());
                        this.winterVillage.shopManager.saveShop(shop);
                    }
                }
            }
        }
    }

}
