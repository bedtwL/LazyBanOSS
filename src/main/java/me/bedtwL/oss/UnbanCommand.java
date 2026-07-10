package me.bedtwL.oss;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;
import java.util.ArrayList;

public class UnbanCommand extends Command {
    public UnbanCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("bedtwL.oss.lazyban.unban")) {
            sender.sendMessage("nothing here!");
            return;
        }
        ArrayList<String> a= (ArrayList<String>) LazyBanOSS.getInstance().config.getList("banned",new ArrayList<String>());
        a.remove(args[0]);
        LazyBanOSS.getInstance().config.set("banned",a);
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(LazyBanOSS.getInstance().config, LazyBanOSS.getInstance().configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sender.sendMessage("unbanned");
    }
}
