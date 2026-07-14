package me.bedtwL.oss;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.*;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import me.bedtwL.oss.api.WebAPI;
import me.bedtwL.oss.utils.BanEntry;
import me.bedtwL.oss.utils.DataUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "lazybanoss", name = "LazyBanOSS", version = "1.0.0",
        url = "https://bedtwL.com", description = "Just a simple ban plugin", authors = "bedtwL")
public final class LazyBanOSS {
    @Getter
    private static LazyBanOSS instance;
    @Getter
    private static ProxyServer proxy;
    @Getter
    private final Logger logger;
    @Getter
    private final Path dataDirectory;
    @Getter
    private static File configFile;
    public static CommentedConfigurationNode config;
    @Getter
    public static String DB_URL;

    @Inject
    public LazyBanOSS(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        proxy = server;
        this.logger = logger;
        instance = this;
        if (!dataDirectory.toFile().exists())
            dataDirectory.toFile().mkdir();
        configFile = new File(dataDirectory.toFile(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception ignored) {
            }
        }
        YamlConfigurationLoader loader = YamlConfigurationLoader.builder()
                .file(configFile)
                .build();
        try {
            config = loader.load(ConfigurationOptions.defaults());
        } catch (ConfigurateException e) {
            e.printStackTrace();
            return;
        }
        try {
            config.node("ban-msg").set(config.node("ban-msg").getString("§r§fbedtw§cL§fServer: §c{0}\n§9Discord: §nhttps://discord.gg/2dkzvPUmja"));
            config.node("def-ban-reason").set(config.node("def-ban-reason").getString("§cYou have been banned!"));
            config.node("web-api", "enabled").set(config.node("web-api", "enabled").getBoolean(false));
            config.node("web-api", "port").set(config.node("web-api", "port").getInt(5029));
            config.node("web-api", "auth").set(config.node("web-api", "auth").getString("ur-key"));
            config.node("db-url").set(config.node("db-url").getString("jdbc:sqlite:" + new File(dataDirectory.toFile(), "database.db").getAbsolutePath()));
            DB_URL = config.node("db-url").getString();
            DataUtils.init();
            try {
                loader.save(config);
            } catch (ConfigurateException e) {
                e.printStackTrace();
            }
            if (config.node("web-api", "enabled").getBoolean()) {
                new WebAPI().startServer(config.node("web-api","port").getInt(5029));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        CommandManager commandManager = server.getCommandManager();
        BrigadierCommand banCommand = BanCommand.create();
        CommandMeta banMeta = commandManager.metaBuilder("lban")
                .aliases("ban")
                .plugin(this)
                .build();
        CommandMeta unbanMeta = commandManager.metaBuilder("lunban")
                .aliases("unban")
                .plugin(this)
                .build();

        LiteralCommandNode<CommandSource> unbanNode = LiteralArgumentBuilder.<CommandSource>literal("lunban")
                .requires(source -> source.hasPermission("bedtwL.oss.lazyban.unban"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("target", StringArgumentType.word())
                        .executes(context -> {
                            CommandSource sender = context.getSource();
                            String target = StringArgumentType.getString(context, "target");
                            if (DataUtils.IsBanned(target)) {
                                BanEntry e = DataUtils.getBan(target);
                                e.setBanEnd(0);
                                DataUtils.saveBan(e);
                                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cUnbanned!"));
                            } else {
                                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer not found in database!"));
                            }
                            return 1;
                        })
                )
                .build();
        commandManager.register(banMeta, banCommand);
        commandManager.register(unbanMeta, new BrigadierCommand(unbanNode));
    }
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getEventManager().register(this, LoginEvent.class, e -> {
            if (DataUtils.IsBanned(e.getPlayer().getUniqueId().toString()))
                e.getPlayer().disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(DataUtils.getBannedReason(e.getPlayer().getUniqueId().toString())));
        });
    }
}
