package io.github.faboit1.bonecane.listener;

import io.github.faboit1.bonecane.util.GrowthUtil;
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

        if (GrowthUtil.tryGrow(target)) {
            removeOneBonemeal(dispenserBlock);
            GrowthUtil.playBonemealEffect(target);
        }
    }

    /**
     * Removes one bonemeal item from the dispenser's internal inventory.
     * Called only after successful growth so bonemeal is never wasted.
     */
    private static void removeOneBonemeal(Block dispenserBlock) {
        if (!(dispenserBlock.getState() instanceof Dispenser dispenser)) return;
        Inventory inventory = dispenser.getInventory();
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == Material.BONE_MEAL) {
                if (stack.getAmount() > 1) {
                    stack.setAmount(stack.getAmount() - 1);
                } else {
                    inventory.setItem(i, null);
                }
                return;
            }
        }
    }
}
