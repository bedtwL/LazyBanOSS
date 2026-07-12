package me.bedtwL.oss;

import me.bedtwL.oss.utils.BanEntry;
import me.bedtwL.oss.utils.DataUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

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
        //TODO: mojang api req player data
        if (DataUtils.IsBanned(args[0])) {
            BanEntry e= DataUtils.getBan(args[0]);
            e.setBanEnd(0);
            DataUtils.saveBan(e);
            sender.sendMessage("§cUnbanned!");
        }
        else
            sender.sendMessage("§cPlayer not found in database!");
    }
}
