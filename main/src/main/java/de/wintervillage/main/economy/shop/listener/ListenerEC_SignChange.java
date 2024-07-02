package de.wintervillage.main.economy.shop.listener;

import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.shop.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class ListenerEC_SignChange implements Listener {

    private WinterVillage winterVillage;

    public ListenerEC_SignChange(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
        this.winterVillage.getServer().getPluginManager().registerEvents(this, this.winterVillage);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event){
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();;

        if(this.winterVillage.shopManager.shopExists(location))
            return;

        if(event.line(0).contains(Component.text("[Shop]"))
            && !PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(event.line(1))).isEmpty()
            && !PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(event.line(2))).isEmpty()) {

            String shop_name = PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(event.line(1)));
            float item_price = 0;

            try {
                item_price = Float.parseFloat(PlainTextComponentSerializer.plainText().serialize(Objects.requireNonNull(event.line(2))));
            } catch (NumberFormatException e) {
                player.sendMessage(this.winterVillage.PREFIX + "Der Preis muss eine Zahl sein.");
                return;
            }

            event.line(0, Component.text("<gradient:#ff0000:#ffffff:#ff0000>------------</gradient>"));
            event.line(1, Component.text("<color:white><bold>" + shop_name + "</bold><color:gray>"));
            event.line(2, Component.text("<color:gray>[<color:red>" + item_price + "<color:gray>]"));
            event.line(3, Component.text("<gradient:#ff0000:#ffffff:#ff0000>------------</gradient>"));

            Shop shop = new Shop(shop_name, item_price, location, player);
            this.winterVillage.shopManager.saveShop(shop);

            player.sendMessage(this.winterVillage.PREFIX + "Der Shop " + shop_name + " wurde erfolgreich erstellt.");
        }
    }

}
