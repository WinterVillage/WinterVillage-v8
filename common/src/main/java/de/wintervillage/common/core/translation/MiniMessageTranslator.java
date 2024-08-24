package de.wintervillage.common.core.translation;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class MiniMessageTranslator implements Translator {

    private final TranslationRegistry registry;
    private final MiniMessage miniMessage;

    public MiniMessageTranslator(@NotNull Key key) {
        this.registry = TranslationRegistry.create(key);
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public @NotNull Key name() {
        return this.registry.name();
    }

    @Override
    public @NotNull TriState hasAnyTranslations() {
        return this.registry.hasAnyTranslations();
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return this.registry.translate(key, locale);
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        Component translated = this.registry.translate(component, locale);

        if (translated != null) {
            String translatedText = Component.text()
                    .content(translated.toString())
                    .build()
                    .content();
            return this.miniMessage.deserialize(translatedText);
        } else {
            MessageFormat format = this.registry.translate(component.key(), locale);
            if (format == null) return null;

            Object[] args = component.arguments().stream()
                    .map(arg -> {
                        if (arg instanceof Component) {
                            return this.extract((Component) arg);
                        }
                        return arg.toString();
                    }).toArray();
            String formatted = format.format(args);
            return this.miniMessage.deserialize(formatted, new ArgumentTag(component.arguments()));
        }
    }

    public void registerAll(@NotNull Locale locale, @NotNull ResourceBundle bundle, boolean override) {
        this.registry.registerAll(locale, bundle, override);
    }

    public void defaultLocale(@NotNull Locale locale) {
        this.registry.defaultLocale(locale);
    }

    private String extract(Component component) {
        StringBuilder builder = new StringBuilder();
        if (component instanceof TextComponent) builder.append(((TextComponent) component).content());
        component.children().forEach(child -> builder.append(this.extract(child)));
        return builder.toString();
    }
}
