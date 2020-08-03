package com.ruinscraft.stickers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StickersCommand implements CommandExecutor {

    private StickerCodeStorage storage;

    public StickersCommand(StickerCodeStorage storage) {
        this.storage = storage;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("group.sponsor")) {
            player.sendMessage(ChatColor.RED + "You must be a Sponsor to use this.");

            return true;
        }

        storage.queryCode(player.getUniqueId()).thenAccept(code -> {
           if (code != null) {
               player.sendMessage(ChatColor.GOLD + "Your sticker code is: " + code);
           } else {
               String newCode = StickersPlugin.createCode();
               storage.insertCode(player.getUniqueId(), newCode);
               player.sendMessage(ChatColor.GOLD + "Your sticker code is: " + newCode);
           }
        });

        return true;
    }

}
