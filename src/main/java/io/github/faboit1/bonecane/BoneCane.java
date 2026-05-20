package io.github.faboit1.bonecane;

import io.github.faboit1.bonecane.listener.DispenserListener;
import io.github.faboit1.bonecane.listener.PlayerInteractListener;
import io.github.faboit1.bonecane.util.GrowthUtil;
import org.bukkit.plugin.java.JavaPlugin;

public final class BoneCane extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadGrowthConfig();
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new DispenserListener(this), this);
        getLogger().info("BoneCane enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BoneCane disabled.");
    }

    private void loadGrowthConfig() {
        String modeStr = getConfig().getString("mode", "legacy");
        GrowthUtil.GrowthMode mode;
        if ("chance".equalsIgnoreCase(modeStr)) {
            mode = GrowthUtil.GrowthMode.CHANCE;
        } else {
            if (!"legacy".equalsIgnoreCase(modeStr)) {
                getLogger().warning("Unknown growth mode '" + modeStr + "' in config.yml — defaulting to 'legacy'.");
            }
            mode = GrowthUtil.GrowthMode.LEGACY;
        }

        double doubleChance = getConfig().getDouble("legacy.double-grow-chance", 0.10);
        double growChance = getConfig().getDouble("chance.grow-chance", 0.10);
        int maxHeight = getConfig().getInt("max-height", GrowthUtil.DEFAULT_MAX_HEIGHT);

        GrowthUtil.configure(mode, doubleChance, growChance, maxHeight);

        getLogger().info("Growth mode: " + mode.name().toLowerCase()
                + (mode == GrowthUtil.GrowthMode.LEGACY
                        ? " (double-grow-chance=" + doubleChance + ")"
                        : " (grow-chance=" + growChance + ")")
                + ", max-height=" + maxHeight);
    }
}
