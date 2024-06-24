package de.wintervillage.main.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import de.wintervillage.main.WinterVillage;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class CMD_Freeze {

    private final WinterVillage winterVillage;

    public CMD_Freeze(Commands commands){
        this.winterVillage = JavaPlugin.getPlugin(WinterVillage.class);
        this.register(commands);
    }

    private void register(Commands commands) {
        LiteralArgumentBuilder<CommandSourceStack> builder_freeze = Commands.literal("freeze")
                .then(
                        Commands.literal("all")
                                .executes((source) -> {
                                    this.winterVillage.freeze_all = !this.winterVillage.freeze_all;
                                    source.getSource().getExecutor().sendMessage(this.winterVillage.PREFIX + "Der Freeze-All-Status wurde " + (this.winterVillage.freeze_all ? "aktiviert" : "deaktiviert") + ".");
                                    return 1;
                                })
                ).then(
                        Commands.argument("player", ArgumentTypes.player())
                                .executes((source) -> {
                                    Player player = source.getArgument("player", Player.class);

                                    if(player_frozen(player)){
                                        freeze_player(player, false);
                                        source.getSource().getExecutor().sendMessage(this.winterVillage.PREFIX + "Der Spieler " + player.getName() + " wurde entfroren.");
                                    } else {
                                        freeze_player(player, true);
                                        source.getSource().getExecutor().sendMessage(this.winterVillage.PREFIX + "Der Spieler " + player.getName() + " wurde eingefroren.");
                                    }

                                    return 1;
                                })
                );
    }

    public void freeze_player(Player player, boolean freeze){
        if(!freeze){
            player.getPersistentDataContainer().remove(this.winterVillage.key_frozen);
        } else {
            player.getPersistentDataContainer().set(this.winterVillage.key_frozen, PersistentDataType.BOOLEAN, true);
        }
    }

    public boolean player_frozen(Player player) {
        return player.getPersistentDataContainer().has(this.winterVillage.key_frozen);
    }

}
