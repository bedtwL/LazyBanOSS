package me.bedtwL.oss;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import me.bedtwL.oss.utils.BanEntry;
import me.bedtwL.oss.utils.DataUtils;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class BanCommand {

    public static BrigadierCommand create() {
        LiteralCommandNode<CommandSource> banNode = LiteralArgumentBuilder.<CommandSource>literal("lban")
                .requires(source -> source.hasPermission("bedtwL.oss.lazyban.ban"))
                .executes(context -> {
                    CommandSource sender = context.getSource();
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c/lban <player|uuid> [time] [reason]"));
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&c#if time=-1 or empty then it will be banned forever"));
                    return 1;
                })
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("args", StringArgumentType.greedyString())
                        .suggests((ctx, builder) -> {
                            LazyBanOSS.getProxy().getAllPlayers().forEach(player -> builder.suggest(
                                    player.getUsername(),
                                    VelocityBrigadierMessage.tooltip(MiniMessage.miniMessage().deserialize(player.getUsername()))
                            ));
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            CommandSource sender = context.getSource();
                            String rawArgs = StringArgumentType.getString(context, "args");
                            String[] args = rawArgs.split(" ");
                            banCmd(sender, args);
                            return 1;
                        })
                )
                .build();
        return new BrigadierCommand(banNode);
    }

    public static void banCmd(CommandSource sender, String[] args) {
        BanEntry banEntry = null;
        Player p = null;
        long sec;
        String uuid;
        String name = "=ERR=";
        if (args[0].length() > 16) {
            //its uuid
            uuid = args[0];
            Optional<Player> pa = LazyBanOSS.getProxy().getPlayer(args[0]);
            if (pa.isPresent()) p = pa.get();
            if (p != null) name = p.getUsername();
        } else {
            Optional<Player> pa = LazyBanOSS.getProxy().getPlayer(args[0]);
            if (pa.isPresent()) p = pa.get();
            if (p == null) {
                //TODO: fetch player into from mojang api
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cPlayer is offline!"));
                return;
            }
            uuid = p.getUniqueId().toString();
            name = p.getUsername();
        }
        if (args.length > 1) {
            if (args[1] != null) {
                if (!Objects.equals(args[1], "-1")) {
                    sec = DataUtils.parseToSeconds(args[1]);
                    if (sec == -1) {
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("Correct format: 5s, 10m, 2h, 14d, 1mo, 1y"));
                        return;
                    }
                } else sec = -1;
            } else sec = -1;

            if (args.length > 2) {
                if (args[2] != null) {
                    String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    if (sec == -1) {
                        banEntry = new BanEntry(uuid, name, reason, -1);
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cBanned §f" + name + "§c with reason: " + reason));
                    } else {
                        banEntry = new BanEntry(uuid, name, reason, System.currentTimeMillis() / 1000 + sec);
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cBanned §f" + name + "§c " + args[1] + " and with reason: " + reason));
                    }
                } else {
                    if (sec == -1) {
                        banEntry = new BanEntry(uuid, name, -1);
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cBanned §f" + name + "§c Forever!"));
                    } else {
                        banEntry = new BanEntry(uuid, name, System.currentTimeMillis() / 1000 + sec);
                        sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cBanned §f" + name + "§c " + args[1] + "!"));
                    }
                }
            } else {
                if (sec == -1) {
                    banEntry = new BanEntry(uuid, name, -1);
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cBanned §f" + name + "§c Forever!"));
                } else {
                    banEntry = new BanEntry(uuid, name, System.currentTimeMillis() / 1000 + sec);
                    sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cBanned §f" + name + "§c " + args[1] + "!"));
                }
            }
        } else {
            banEntry = new BanEntry(uuid, name, -1);
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("§cBanned §f" + name + "§c Forever!"));
        }
        DataUtils.saveBan(banEntry);
        if (p != null)
            p.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(banEntry.getReasonCombined()));
    }
}