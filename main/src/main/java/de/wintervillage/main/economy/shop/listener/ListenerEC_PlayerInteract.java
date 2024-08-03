package de.wintervillage.main.economy.shop.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.shop.Shop;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ListenerEC_PlayerInteract implements Listener {

    private WinterVillage winterVillage;

    public ListenerEC_PlayerInteract(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() != null
            && event.getClickedBlock() instanceof Sign){
            Shop shop = this.winterVillage.shopManager.getShop(event.getClickedBlock().getLocation());

            if(shop != null){
                if(player.equals(shop.getShopOwner())){
                    player.openInventory(shop.getShopInventory());
                } else {
                    this.winterVillage.shopManager.openSellsInventory(player, shop);
                }

                event.setCancelled(true);
            }

        }
    }

}
