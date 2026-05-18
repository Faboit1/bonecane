package io.github.faboit1.bonecane.listener;

import io.github.faboit1.bonecane.BoneCane;
import io.github.faboit1.bonecane.util.GrowthUtil;
import io.github.faboit1.bonecane.util.GrowthUtil.GrowthResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handles dispenser-fired bonemeal applied to sugar cane and cactus.
 */
public final class DispenserListener implements Listener {

    private final BoneCane plugin;

    public DispenserListener(BoneCane plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        if (event.getItem().getType() != Material.BONE_MEAL) return;

        Block dispenserBlock = event.getBlock();
        if (dispenserBlock.getType() != Material.DISPENSER) return;
        if (!(dispenserBlock.getBlockData() instanceof Directional directional)) return;

        Block target = dispenserBlock.getRelative(directional.getFacing());

        Material targetType = target.getType();
        if (targetType != Material.SUGAR_CANE && targetType != Material.CACTUS) return;

        // We handle this event ourselves — prevent vanilla dispense behaviour.
        event.setCancelled(true);

        // Only the bottom block of the column may be bonemealed.
        if (!GrowthUtil.isBottomBlock(target)) return;

        // Schedule for the next tick so that vanilla has time to restore the
        // cancelled item back into the dispenser inventory before we remove it.
        // Without this delay, the dispenser inventory appears empty during the
        // event (the item was pre-removed by the server before firing), which
        // causes the last bonemeal in a dispenser to never be consumed.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            GrowthResult result = GrowthUtil.tryGrow(target);
            if (result != GrowthResult.SKIPPED) {
                removeOneBonemeal(dispenserBlock);
                if (result == GrowthResult.GREW) {
                    GrowthUtil.playBonemealEffect(target);
                }
            }
        });
    }

    /**
     * Removes one bonemeal item from the dispenser's internal inventory.
     * Always uses {@link Inventory#setItem} to avoid relying on whether the
     * ItemStack references returned by {@link Inventory#getContents()} are
     * live or snapshot copies.
     */
    private static void removeOneBonemeal(Block dispenserBlock) {
        if (!(dispenserBlock.getState() instanceof Dispenser dispenser)) return;
        Inventory inventory = dispenser.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == Material.BONE_MEAL) {
                if (stack.getAmount() > 1) {
                    ItemStack reduced = stack.clone();
                    reduced.setAmount(stack.getAmount() - 1);
                    inventory.setItem(i, reduced);
                } else {
                    inventory.setItem(i, null);
                }
                return;
            }
        }
    }
}
