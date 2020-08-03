package com.ruinscraft.stickers;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class StickersPlugin extends JavaPlugin {

    private StickerCodeStorage storage;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        File googleCredsFile = new File(getDataFolder(), "credentials.json");

        if (!googleCredsFile.exists()) {
            getLogger().warning("No credentials.json file in plugin dir");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        storage = new GoogleSheetsStickerCodeStorage(this);

        getCommand("stickers").setExecutor(new StickersCommand(storage));
    }

    public static String createCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
