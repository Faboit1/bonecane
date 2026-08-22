package io.github.faboit1.bonecane.util;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

/**
 * Thin scheduler abstraction that works on Paper, Folia, and forks like Canvas.
 * Uses reflection so the compiled bytecode never references Folia-specific types
 * directly, avoiding NoSuchMethodError on forks with relocated classes.
 */
public final class SchedulerUtil {

    private static final boolean FOLIA;
    private static final MethodHandle GET_REGION_SCHEDULER;
    private static final MethodHandle REGION_SCHEDULER_RUN;

    static {
        boolean folia = false;
        MethodHandle getScheduler = null;
        MethodHandle run = null;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            // Resolve handles reflectively so we don't hardcode the return type
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            getScheduler = lookup.findVirtual(
                    org.bukkit.Server.class, "getRegionScheduler", MethodType.methodType(
                            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler")));
            Class<?> regionSchedulerClass = getScheduler.type().returnType();
            run = lookup.findVirtual(regionSchedulerClass, "run",
                    MethodType.methodType(
                            Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask"),
                            Plugin.class, Location.class, Consumer.class));
            folia = true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {
        }
        FOLIA = folia;
        GET_REGION_SCHEDULER = getScheduler;
        REGION_SCHEDULER_RUN = run;
    }

    private SchedulerUtil() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    /**
     * Runs a task on the next tick on the region that owns {@code location}.
     * On Paper this delegates to the global scheduler; on Folia/Canvas it uses
     * the region scheduler so the task executes on the correct region thread.
     */
    public static void runAtLocation(Plugin plugin, Location location, Runnable task) {
        if (FOLIA) {
            try {
                Object regionScheduler = GET_REGION_SCHEDULER.invoke(plugin.getServer());
                REGION_SCHEDULER_RUN.invoke(regionScheduler, plugin, location,
                        (Consumer<?>) scheduledTask -> task.run());
            } catch (Throwable e) {
                throw new RuntimeException("Failed to schedule region task", e);
            }
        } else {
            plugin.getServer().getScheduler().runTask(plugin, task);
        }
    }
}
