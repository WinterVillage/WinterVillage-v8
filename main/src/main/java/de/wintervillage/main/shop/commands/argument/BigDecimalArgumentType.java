package de.wintervillage.main.shop.commands.argument;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class BigDecimalArgumentType implements CustomArgumentType.Converted<BigDecimal, String> {

    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public @NotNull BigDecimal convert(@NotNull String nativeType) throws CommandSyntaxException {
        try {
            return new BigDecimal(nativeType);
        } catch (NumberFormatException e) {
            Message message = MessageComponentSerializer.message().serialize(Component.text("Invalid number"));
            throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
        }
    }

    public static BigDecimalArgumentType bigDecimal() {
        return new BigDecimalArgumentType();
    }
}
