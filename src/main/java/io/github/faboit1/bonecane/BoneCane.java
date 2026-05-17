package io.github.faboit1.bonecane;

import io.github.faboit1.bonecane.listener.DispenserListener;
import io.github.faboit1.bonecane.listener.PlayerInteractListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoneCane extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new DispenserListener(), this);
        getLogger().info("BoneCane enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BoneCane disabled.");
    }
}
