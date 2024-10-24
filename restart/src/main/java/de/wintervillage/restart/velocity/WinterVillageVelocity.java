package de.wintervillage.restart.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
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
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "wintervillage-restartmanagement",
        name = "WinterVillage-RestartManagement",
        version = "8-SNAPSHOT",
        authors = {"Voldechse"},
        dependencies = {
                @Dependency(id = "cloudnet-bridge", optional = false)
        }
)
public final class WinterVillageVelocity {

    private @Inject
    final ProxyServer proxyServer;
    private @Inject
    final Logger logger;
    private final Path dataDirectory;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private RestartManagement restartManagement;

    private SpecificCloudServiceProvider serviceProvider;

    /**
     * Usage: {@link Component#join(JoinConfiguration.Builder, ComponentLike...)} to send a message with prefix
     */
    public final JoinConfiguration prefix = JoinConfiguration.builder()
            .prefix(Component.translatable("wintervillage.prefix"))
            .separator(Component.empty())
            .build();

    @Inject
    public WinterVillageVelocity(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        ConfigHandler configHandler = new ConfigHandler(this.dataDirectory.resolve("config.json"));
        this.restartManagement = new RestartManagement(configHandler);
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

    private void start() {
        this.executorService.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            this.restartManagement.checkScheduledRestarts(now,
                    () -> {
                        this.executorService.shutdown();
                        this.proxyServer.sendMessage(Component.join(
                                this.prefix,
                                Component.translatable("wintervillage.server-restarts"))
                        );

                        this.serviceProvider.delete();
                    },
                    (secondsBefore) -> {
                        int minutes = secondsBefore / 60;

                        this.proxyServer.sendMessage(Component.join(
                                this.prefix,
                                Component.translatable("wintervillage.server-restarts-timer",
                                        secondsBefore >= 60 ? Component.text(minutes + " min") : Component.text(secondsBefore + " s")))
                        );
                    });
        }, 0, 1, TimeUnit.SECONDS);
    }
}
