package de.wintervillage.main.economy;

import de.wintervillage.main.WinterVillage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.ArrayList;

public class EconomyManager {

    private WinterVillage winterVillage;

    public EconomyManager() {
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
    }

    public float getBalance(Player player) {
        // Implementieren der Datenbank-Abfrage bzgl. Kontostand
        return 0;
    }

    public void setBalance(Player player, float amount) {
        // Implementieren des Datenbank-Schreibens bzgl. Kontostand
    }

    public void addMoney(Player player, float amount) {
        setBalance(player, getBalance(player) + amount);
    }

    public boolean transferMoney(Player sender, Player receiver, float amount) {
        if(getBalance(sender) < amount) {
            return false;
        }

        addMoney(sender, -amount); //Entfernen des Geldes vom Sender-Konto
        addMoney(receiver, amount); //Hinzufügen des Geldes zum Empfänger-Konto

        Transaction transaction = new Transaction(LocalDate.now(), sender, receiver, amount);
        addTransaction(sender, transaction);
        addTransaction(receiver, transaction);

        return true;
    }

    public void addTransaction(Player player, Transaction transaction){
        // Implementieren des Datenbank-Schreibens bzgl. Transaktion
    }

    public ArrayList<Transaction> getTransactionHistory(Player player) {
        ArrayList<Transaction> transactionHistory = new ArrayList<>();

        // Implementieren der Datenbank-Abfrage bzgl. Transaktionshistorie

        return transactionHistory;
    }

    public ArrayList<Transaction> getTransactionsAtDate(Player player, LocalDate date) {
        ArrayList<Transaction> transactions = new ArrayList<>();

        for(Transaction transaction : transactions){
            if(transaction.getDate().equals(date)){
                transactions.add(transaction);
            }
        }

        return transactions;
    }

}
