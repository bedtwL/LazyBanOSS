package me.bedtwL.oss;

import lombok.Getter;
import me.bedtwL.oss.api.WebAPI;
import me.bedtwL.oss.utils.DataUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;

public final class LazyBanOSS extends Plugin implements Listener {
    @Getter
    private static LazyBanOSS instance;
    @Getter
    private static File configFile;
    @Getter
    private static Configuration config;
    @Getter
    public static String DB_URL;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance=this;
         if (!getDataFolder().exists())
            getDataFolder().mkdir();
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (Exception ignored) {
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (Exception ignored) {
        };
        config.set("ban-msg",config.getString("ban-msg","§r§fbedtw§cL§fServer: §c{0}\n§9Discord: §nhttps://discord.gg/2dkzvPUmja"));
        config.set("def-ban-reason",config.getString("def-ban-reason","§cYou have been banned!"));
        config.set("web-api.enabled",config.getBoolean("web-api.enabled",false));
        config.set("web-api.port",config.getInt("web-api.port",5029));
        config.set("web-api.auth",config.getString("web-api.auth","ur-key"));
        config.set("db-url",config.getString("db-url","jdbc:sqlite:"+new File(getDataFolder(),"database.db").getAbsolutePath()));
        DB_URL=config.getString("db-url");
        DataUtils.init();
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
        } catch (IOException ignored) {
        }
        getProxy().getPluginManager().registerListener(this,this);
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanCommand("lban"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnbanCommand("lunban"));
        if (config.getBoolean("web-api.enabled")) {
            WebAPI.setLogger(getLogger());
            new WebAPI().startServer();
        }
    }
    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent e) {
        if (DataUtils.IsBanned(e.getPlayer().getUniqueId().toString()))
            e.getPlayer().disconnect(DataUtils.getBannedReason(e.getPlayer().getUniqueId().toString()));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
