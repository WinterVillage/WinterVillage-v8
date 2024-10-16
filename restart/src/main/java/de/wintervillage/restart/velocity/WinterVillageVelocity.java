package de.wintervillage.restart.velocity;

import com.google.gson.JsonArray;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.wintervillage.common.core.config.Document;
import de.wintervillage.common.core.translation.MiniMessageTranslator;
import de.wintervillage.restart.core.RestartManagement;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
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

        this.resolveConfig();
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        this.restartManagement = new RestartManagement(this.dataDirectory.resolve("config.json"));
        this.start();

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

                        Bukkit.broadcast(Component.text("Server is restarting..."));
                    },
                    (minutesBefore) -> Bukkit.broadcast(Component.text("Server is restarting in " + minutesBefore + " minutes...")));
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void resolveConfig() {
        try {
            Path configPath = this.dataDirectory.resolve("config.json");
            Files.createDirectories(configPath.getParent());

            Document defaultConfig = new Document();
            defaultConfig.append("scheduled", new JsonArray());

            if (!Files.exists(configPath)) {
                defaultConfig.save(configPath);
            }
        } catch (IOException exception) {
            this.logger.error("Failed to resolve configuration", exception);
        }
    }
}
