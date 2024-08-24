package de.wintervillage.common.core.translation;

import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * @see <a href="https://github.com/KyoriPowered/adventure/pull/972/files#diff-0b7a651495effdf724e42b107012ed0489868b011f2ca0ed59ae6a0026e680ee">ArgumentTag.java in PR #972</a>
 */
public class ArgumentTag implements TagResolver {

    private static final String NAME = "arg";

    private final List<? extends ComponentLike> argumentComponents;

    public ArgumentTag(List<? extends ComponentLike> argumentComponents) {
        this.argumentComponents = Objects.requireNonNull(argumentComponents, "argumentComponents");
    }

    @Override
    public @Nullable Tag resolve(@NotNull String name, @NotNull ArgumentQueue arguments, @NotNull Context ctx) throws ParsingException {
        final int index = arguments.popOr("No argument number provided").asInt().orElseThrow(() -> ctx.newException("Invalid argument number", arguments));

        if (index < 0 || index >= this.argumentComponents.size()) {
            throw ctx.newException("Argument index out of bounds", arguments);
        }

        return Tag.inserting(this.argumentComponents.get(index));
    }

    @Override
    public boolean has(@NotNull String name) {
        return name.equals(NAME);
    }
}
