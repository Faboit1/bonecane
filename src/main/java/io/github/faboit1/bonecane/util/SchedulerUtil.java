package io.github.faboit1.bonecane.util;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

/**
 * Thin scheduler abstraction that works on both Paper and Folia.
 */
public final class SchedulerUtil {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        FOLIA = folia;
    }

    private SchedulerUtil() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Runs a task on the next tick on the region that owns {@code location}.
     * On Paper this delegates to the global scheduler; on Folia it uses the
     * region scheduler so the task executes on the correct region thread.
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (FOLIA) {
            plugin.getServer().getRegionScheduler().run(plugin, location, scheduledTask -> task.run());
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }
}
