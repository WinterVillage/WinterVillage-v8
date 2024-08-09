package de.wintervillage.main.specialitems.commands.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.wintervillage.main.WinterVillage;
import de.wintervillage.main.specialitems.SpecialItem;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class Argument_SpecialItem implements CustomArgumentType.Converted<SpecialItem, String> {

    private WinterVillage winterVillage;

    public Argument_SpecialItem(WinterVillage winterVillage){
        this.winterVillage = winterVillage;
    }

    @Override
    public @NotNull SpecialItem convert(@NotNull String str) throws CommandSyntaxException {
        SpecialItem specialItem = this.winterVillage.specialItems.getSIByName(str);

        if(specialItem == null){
            Message message = MessageComponentSerializer.message().serialize(Component.text("Das SpecialItem " + str + " existiert nicht."));
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }

        return specialItem;
    }

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull <S> CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        for(SpecialItem specialItem : this.winterVillage.specialItems.getSpecialItems()){
            builder.suggest(specialItem.getNameStr());
        }

        return CompletableFuture.completedFuture(builder.build());
    }
}
