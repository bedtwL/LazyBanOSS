package me.bedtwL.oss.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import me.bedtwL.oss.util.DataUtils;
import me.bedtwL.oss.util.MojangApi;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;
import java.util.UUID;

public final class UnbanCommand {

    public static BrigadierCommand create() {
        LiteralCommandNode<CommandSource> unbanNode = LiteralArgumentBuilder.<CommandSource>literal("lunban")
                .requires(source -> source.hasPermission("bedtwL.oss.lazyban.unban"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("target", StringArgumentType.word())
                        .executes(context -> {
                            String target = StringArgumentType.getString(context, "target");
                            unbanCmd(context.getSource(), target);
                            return 1;
                        })
                ).build();
        return new BrigadierCommand(unbanNode);
    }

    private static void unbanCmd(CommandSource sender, String target) {
        UUID uuid = null;
        try {
            uuid = UUID.fromString(target);
        } catch (IllegalArgumentException ignored) {
            Optional<UUID> optionalUUID = MojangApi.getUuid(target);
            if (optionalUUID.isPresent()) uuid = optionalUUID.get();
        }
        if (uuid == null) {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer not found!"));
            return;
        }

        String uuidStr = uuid.toString();
        if (DataUtils.isBanned(uuidStr)) {
            DataUtils.removeBan(uuidStr);
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&aUnbanned " + target + "!"));
        } else {
            sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&cPlayer not found in database!"));
        }
    }

}