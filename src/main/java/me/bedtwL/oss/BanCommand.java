package me.bedtwL.oss;

import me.bedtwL.oss.utils.BanEntry;
import me.bedtwL.oss.utils.DataUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

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
        if (args.length > 0) {
            banCmd(sender, args);
        } else {
            sender.sendMessage("§c/lban <player|uuid> [time] [reason]");
            sender.sendMessage("§c#if time=-1 or empty then it will be banned forever");
        }
    }

    public static void banCmd(CommandSender sender, String[] args) {
        BanEntry banEntry = null;
        ProxiedPlayer p = null;
        long sec;
        String uuid;
        String name = "=ERR=";
        if (args[0].length() > 16) {
            //its uuid
            uuid = args[0];
            p = LazyBanOSS.getInstance().getProxy().getPlayer(UUID.fromString(uuid));
            if (p != null) name = p.getName();
        } else {
            p = LazyBanOSS.getInstance().getProxy().getPlayer(args[0]);
            if (p == null) {
                //TODO: fetch player into from mojang api
                sender.sendMessage("§cPlayer is offline!");
                return;
            }
            uuid = p.getUniqueId().toString();
            name = p.getName();
        }
        if (args.length > 1) {
            if (args[1] != null) {
                if (!Objects.equals(args[1], "-1")) {
                    sec = DataUtils.parseToSeconds(args[1]);
                    if (sec == -1) {
                        sender.sendMessage("Correct format: 5s, 10m, 2h, 14d, 1mo, 1y");
                        return;
                    }
                } else sec = -1;
            } else sec = -1;

            if (args.length > 2) {
                if (args[2] != null) {
                    String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (sec == -1) {
                        banEntry = new BanEntry(uuid, name, reason, -1);
                        sender.sendMessage("§cBanned §f" + name + "§c with reason: " + reason);
                    } else {
                        banEntry = new BanEntry(uuid, name, reason, System.currentTimeMillis() / 1000 + sec);
                        sender.sendMessage("§cBanned §f" + name + "§c " + args[1] + " and with reason: " + reason);
                    }
                } else {
                    if (sec == -1) {
                        banEntry = new BanEntry(uuid, name, -1);
                        sender.sendMessage("§cBanned §f" + name + "§c Forever!");
                    } else {
                        banEntry = new BanEntry(uuid, name, System.currentTimeMillis() / 1000 + sec);
                        sender.sendMessage("§cBanned §f" + name + "§c " + args[1] + "!");
                    }
                }
            } else {
                if (sec == -1) {
                    banEntry = new BanEntry(uuid, name, -1);
                    sender.sendMessage("§cBanned §f" + name + "§c Forever!");
                } else {
                    banEntry = new BanEntry(uuid, name, System.currentTimeMillis() / 1000 + sec);
                    sender.sendMessage("§cBanned §f" + name + "§c " + args[1] + "!");
                }
            }
        } else {
            banEntry = new BanEntry(uuid, name, -1);
            sender.sendMessage("§cBanned §f" + name + "§c Forever!");
        }
        DataUtils.saveBan(banEntry);
        if (p != null) p.disconnect(banEntry.getReasonCombined());
    }
}
