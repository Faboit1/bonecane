package io.github.faboit1.bonecane.listener;

import io.github.faboit1.bonecane.util.GrowthUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles right-click bonemeal application to sugar cane and cactus.
 */
public final class PlayerInteractListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle main-hand right-clicks to avoid double-firing.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.BONE_MEAL) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Material blockType = block.getType();
        if (blockType != Material.SUGAR_CANE && blockType != Material.CACTUS) return;

        // Cancel the vanilla interaction so no default bonemeal effect fires.
        event.setCancelled(true);

        // Only the bottom block may be bonemealed.
        if (!GrowthUtil.isBottomBlock(block)) return;

        if (GrowthUtil.tryGrow(block)) {
            Player player = event.getPlayer();
            consumeBonemeal(player, item);
            GrowthUtil.playBonemealEffect(block);
        }
    }

    /** Removes one bonemeal from the player's hand (skipped in Creative mode). */
    private static void consumeBonemeal(Player player, ItemStack item) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            item.setType(Material.AIR);
        }
    }
}
