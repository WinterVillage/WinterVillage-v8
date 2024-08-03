package de.wintervillage.main.economy;

import de.wintervillage.main.WinterVillage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {

    private WinterVillage winterVillage;

    private Player sender, receiver;
    private LocalDate date;
    private float amount;


    public Transaction() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public Transaction(LocalDate date, Player sender, Player receiver, float amount) {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public Transaction(String transaction_string){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        String[] parts = transaction_string.split(" - ");

        this.date = LocalDate.parse(parts[0]);
        this.sender = this.winterVillage.getServer().getPlayer(UUID.fromString(parts[1]));
        this.receiver = this.winterVillage.getServer().getPlayer(UUID.fromString(parts[2]));
        this.amount = Float.parseFloat(parts[3]);
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setSender(Player sender) {
        this.sender = sender;
    }

    public void setReceiver(Player receiver) {
        this.receiver = receiver;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public Player getSender() {
        return sender;
    }

    public Player getReceiver() {
        return receiver;
    }

    public float getAmount() {
        return amount;
    }

    public String getTransactionString() {
        return date + " - " + sender.getUniqueId().toString() + " - " + receiver.getUniqueId().toString() + " - " + amount;
    }

}
