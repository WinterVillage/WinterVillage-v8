package de.wintervillage.restart.paper;

import de.wintervillage.common.core.translation.MiniMessageTranslator;
import de.wintervillage.restart.core.RestartManagement;
import de.wintervillage.restart.core.config.ConfigHandler;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.provider.SpecificCloudServiceProvider;
import eu.cloudnetservice.wrapper.configuration.WrapperConfiguration;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class WinterVillagePaper extends JavaPlugin {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ConfigHandler configHandler;
    private RestartManagement restartManagement;

    private SpecificCloudServiceProvider serviceProvider;

    /**
     * Usage: {@link Component#join(JoinConfiguration.Builder, ComponentLike...)} to send a message with prefix
     */
    public final JoinConfiguration prefix = JoinConfiguration.builder()
            .prefix(Component.translatable("wintervillage.prefix"))
            .separator(Component.empty())
            .build();

    @Override
    public void onLoad() {
        this.configHandler = new ConfigHandler(Paths.get(this.getDataFolder().getAbsolutePath(), "config.json"));
    }

    @Override
    public void onEnable() {
        this.restartManagement = new RestartManagement(this.configHandler);
        this.start();

        // cloudnet
        this.serviceProvider = InjectionLayer.ext().instance(WrapperConfiguration.class).serviceInfoSnapshot().provider();

        // translations
        MiniMessageTranslator translator = new MiniMessageTranslator(Key.key("wintervillage", "translations"));

        ResourceBundle bundleGerman = ResourceBundle.getBundle("Bundle", Locale.GERMANY, UTF8ResourceBundleControl.get());
        ResourceBundle bundleEnglish = ResourceBundle.getBundle("Bundle", Locale.US, UTF8ResourceBundleControl.get());

        translator.registerAll(Locale.US, bundleEnglish, true);
        translator.registerAll(Locale.GERMANY, bundleGerman, true);
        translator.defaultLocale(Locale.GERMANY);

        GlobalTranslator.translator().addSource(translator);
    }

    @Override
    public void onDisable() {
        if (this.executorService != null && !this.executorService.isShutdown()) this.executorService.shutdown();
    }

    private void start() {
        this.executorService.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            this.restartManagement.checkScheduledRestarts(now,
                    () -> {
                        this.executorService.shutdown();
                        Bukkit.broadcast(Component.join(
                                this.prefix,
                                Component.translatable("wintervillage.server-restarts"))
                        );

                        this.serviceProvider.delete();
                    },
                    (secondsBefore) -> {
                        int minutes = secondsBefore / 60;

                        Bukkit.broadcast(Component.join(
                                this.prefix,
                                Component.translatable("wintervillage.server-restarts-timer",
                                        secondsBefore >= 60 ? Component.text(minutes + "min") : Component.text(secondsBefore + "s")))
                        );
                    });
        }, 0, 1, TimeUnit.SECONDS);
    }
}
