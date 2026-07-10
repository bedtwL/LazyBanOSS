package me.bedtwL.oss;

import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

public final class LazyBanOSS extends Plugin implements Listener {
    @Getter
    private static LazyBanOSS instance;
    File configFile;
    Configuration config;
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance=this;
         if (!getDataFolder().exists())
            getDataFolder().mkdir();
        configFile = new File(getDataFolder(), "config.yml");

        // Copy default config if it doesn't exist
        if (!configFile.exists()) {
            try (InputStream in = getResourceAsStream("config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        };
        config.set("ban-msg",config.getString("ban-msg","§r§fbedtw§cL§fServer: §cYou have been banned!\n§9Discord: §nhttps://discord.gg/2dkzvPUmja"));
        getProxy().getPluginManager().registerListener(this,this);
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new BanCommand("lban"));
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new UnbanCommand("lunban"));
    }
    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent e) {
        if (config.getList("banned",new ArrayList<>()).contains(String.valueOf(e.getPlayer().getUniqueId().toString())))
            e.getPlayer().disconnect(config.getString("ban-msg"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
