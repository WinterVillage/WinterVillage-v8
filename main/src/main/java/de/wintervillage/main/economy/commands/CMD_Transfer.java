package de.wintervillage.main.economy.commands;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.economy.Transaction;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CMD_Transfer {

    private WinterVillage winterVillage;

    public CMD_Transfer(Commands commands){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);

        this.register(commands);
    }

    public void register(Commands commands){
        final LiteralArgumentBuilder<CommandSourceStack> transfer_builder = Commands.literal("transfer")
                .then(
                        Commands.argument("receiver", ArgumentTypes.player())
                                .then(
                                        Commands.argument("amount", FloatArgumentType.floatArg(0))
                                                .executes((source) -> {
                                                    Player receiver = source.getArgument("receiver", Player.class);
                                                    float amount = source.getArgument("amount", Float.class);

                                                    if(!(source.getSource().getExecutor() instanceof Player sender)){
                                                        source.getSource().getSender().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                                                        return 0;
                                                    }

                                                    if(this.winterVillage.economyManager.transferMoney(sender, receiver, amount)){
                                                        sender.sendMessage(this.winterVillage.PREFIX + "Du hast " + receiver.getName() + " " + amount + " WV$ überwiesen.");
                                                        receiver.sendMessage(this.winterVillage.PREFIX + "Du hast von " + sender.getName() + " " + amount + " WV$ erhalten.");
                                                    } else {
                                                        sender.sendMessage(this.winterVillage.PREFIX + "Du hast nicht genug Geld.");
                                                    }

                                                    return 1;
                                                })
                                )
                ).then(
                        Commands.literal("history").executes((source) -> {
                            if(!(source.getSource().getSender() instanceof Player player)){
                                source.getSource().getSender().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                                return 0;
                            }

                            this.openTransactionList(player, player);

                            return 1;
                        })
                                .then(
                                        Commands.argument("player", ArgumentTypes.player())
                                                .executes((source) -> {
                                                    Player player = source.getArgument("player", Player.class);

                                                    if(!(source.getSource().getSender() instanceof Player executor)){
                                                        source.getSource().getSender().sendMessage(this.winterVillage.PREFIX + "Dieser Command ist nur durch einen Spieler ausführbar.");
                                                        return 0;
                                                    }

                                                    //Todo: Check Permission
                                                    if(!executor.isOp()){
                                                        executor.sendMessage(this.winterVillage.PREFIX + "Du hast keine Berechtigung.");
                                                        return 0;
                                                    }

                                                    this.openTransactionList(executor, player);

                                                    return 1;
                                        })
                                )

                );

        commands.register(this.winterVillage.getPluginMeta(), transfer_builder.build(), "Überweist Geld an einen Spieler.", List.of());
    }

    public void openTransactionList(Player executor, Player player){
        ArrayList<Transaction> transactions = this.winterVillage.economyManager.getTransactionHistory(player);

        ItemStack transaction_book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta book_meta = (BookMeta) transaction_book.getItemMeta();
        book_meta.setTitle("Transaktionshistorie");
        book_meta.setAuthor("WV-Bank");

        if(player!=executor){
            book_meta.setTitle("Transaktionshistorie von " + player.getName());
        }

        StringBuilder pageContent = new StringBuilder();

        for(Transaction transaction : transactions){
            String date = transaction.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            String amount = (transaction.getSender().getName().equalsIgnoreCase(player.getName()) ? "§a" : "§c") + transaction.getAmount() + " §fWV$";
            String player_name = transaction.getSender().getName();

            String entry = "§7" + date + " | " + amount + " | §f" + player_name + "\n";

            if(pageContent.length() + entry.length() > 240){
                book_meta.addPage(pageContent.toString());
                pageContent = new StringBuilder();
            }

            pageContent.append(entry);
        }

        if(!pageContent.isEmpty()){
            book_meta.addPage(pageContent.toString());
        }

        transaction_book.setItemMeta(book_meta);

        executor.openBook(transaction_book);
    }

}
