package de.wintervillage.main.economy.shop.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.shop.Shop;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class ListenerEC_BlockBreak implements Listener {

    private WinterVillage winterVillage;

    public ListenerEC_BlockBreak(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        if(event.getBlock() instanceof Sign){
            Shop shop = this.winterVillage.shopManager.getShop(location);

            if(shop!=null){
                if(shop.getShopOwner().equals(player)){
                    this.winterVillage.shopManager.removeShop(shop);
                    player.sendMessage(this.winterVillage.PREFIX + "Der Shop " + shop.getShopName() + " wurde erfolgreich gelöscht.");
                } else {
                    event.setCancelled(true);
                    player.sendMessage(this.winterVillage.PREFIX + "Du kannst keine fremden Shops löschen.");
                }
            }
        }
    }

}
