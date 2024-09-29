package de.wintervillage.proxy.commands.punish;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import de.wintervillage.proxy.WinterVillage;
import de.wintervillage.proxy.commands.punish.sub.*;

public class PunishCommand {

    /**
     * /punish ban <name> (reason)
     * /punish kick <name> (reason)
     * /punish mute <name> (reason)
     * /punish unban <name>
     * /punish unmute <name>
     */

    private final WinterVillage winterVillage;

    public PunishCommand(WinterVillage winterVillage) {
        this.winterVillage = winterVillage;
    }

    public BrigadierCommand create() {
        LiteralCommandNode<CommandSource> node = BrigadierCommand.literalArgumentBuilder("punish")
                .requires(source -> source.hasPermission("wintervillage.proxy.command.punish"))
                .then(BrigadierCommand.literalArgumentBuilder("ban").redirect(new BanSubCommand(this.winterVillage).create()))
                .then(BrigadierCommand.literalArgumentBuilder("kick").redirect(new KickSubCommand(this.winterVillage).create()))
                .then(BrigadierCommand.literalArgumentBuilder("mute").redirect(new MuteSubCommand(this.winterVillage).create()))
                .then(BrigadierCommand.literalArgumentBuilder("unban").redirect(new UnbanSubCommand(this.winterVillage).create()))
                .then(BrigadierCommand.literalArgumentBuilder("unmute").redirect(new UnmuteSubCommand(this.winterVillage).create()))
                .build();
        return new BrigadierCommand(node);
    }
}
