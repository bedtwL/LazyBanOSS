package me.bedtwL.oss;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;

public class BanCommand extends Command {
    public BanCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bedtwL.oss.lazyban.ban")) {
            sender.sendMessage("nothing here!");
            return;
        }
        ProxiedPlayer p= ProxyServer.getInstance().getPlayer(args[0]);
        p.disconnect(LazyBanOSS.getInstance().config.getString("ban-msg"));
        ArrayList<String> a= (ArrayList<String>) LazyBanOSS.getInstance().config.getList("banned",new ArrayList<String>());
        a.add(p.getUniqueId().toString());
        LazyBanOSS.getInstance().config.set("banned",a);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(LazyBanOSS.getInstance().config, LazyBanOSS.getInstance().configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sender.sendMessage("banned");
    }
}
