package io.github.faboit1.bonecane.util;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class containing all growth logic for sugar cane and cactus.
 * Designed to be allocation-efficient and called only from events.
 */
public final class GrowthUtil {

    /** Maximum allowed stack height (vanilla max for sugar cane / cactus). */
    public static final int MAX_HEIGHT = 3;

    /** Probability of growing by 2 instead of 1. */
    private static final double DOUBLE_GROW_CHANCE = 0.10;

    private GrowthUtil() {}

    /**
     * Returns {@code true} if the given block is the BOTTOM block of a sugar
     * cane or cactus column (i.e. the block directly below is NOT the same
     * material).
     */
    public static boolean isBottomBlock(Block block) {
        Material type = block.getType();
        if (type != Material.SUGAR_CANE && type != Material.CACTUS) {
            return false;
        }
        return block.getRelative(BlockFace.DOWN).getType() != type;
    }

    /**
     * Counts how many blocks tall the column is, starting from
     * {@code bottomBlock} and going upward (capped at {@link #MAX_HEIGHT}).
     */
    public static int getStackHeight(Block bottomBlock) {
        int height = 1;
        Block above = bottomBlock.getRelative(BlockFace.UP);
        while (above.getType() == bottomBlock.getType() && height < MAX_HEIGHT) {
            height++;
            above = above.getRelative(BlockFace.UP);
        }
        return height;
    }

    /**
     * Attempts to grow the column whose bottom block is {@code bottomBlock}.
     *
     * <ul>
     *   <li>Does nothing and returns {@code false} if the column is already
     *       {@value #MAX_HEIGHT} blocks tall.</li>
     *   <li>Grows by +1 most of the time, +2 with a
     *       {@value #DOUBLE_GROW_CHANCE} probability – never exceeding
     *       {@value #MAX_HEIGHT}.</li>
     *   <li>Validates placement for cactus (air required on all four sides of
     *       each new block).</li>
     *   <li>Returns {@code true} only if at least one block was placed.</li>
     * </ul>
     */
    public static boolean tryGrow(Block bottomBlock) {
        int height = getStackHeight(bottomBlock);
        if (height >= MAX_HEIGHT) {
            return false;
        }

        Material type = bottomBlock.getType();

        // Walk to the current top block.
        Block topBlock = bottomBlock;
        for (int i = 1; i < height; i++) {
            topBlock = topBlock.getRelative(BlockFace.UP);
        }

        int targetGrowth = ThreadLocalRandom.current().nextDouble() < DOUBLE_GROW_CHANCE ? 2 : 1;
        targetGrowth = Math.min(targetGrowth, MAX_HEIGHT - height);

        int placed = 0;
        for (int i = 0; i < targetGrowth; i++) {
            Block next = topBlock.getRelative(BlockFace.UP);

            if (next.getType() != Material.AIR) {
                break;
            }
            if (type == Material.CACTUS && !canPlaceCactus(next)) {
                break;
            }

            next.setType(type);
            topBlock = next;
            placed++;
        }

        return placed > 0;
    }

    /**
     * Checks whether a cactus block may be placed at the given position.
     * Cactus requires all four horizontal neighbours to be air.
     */
    private static boolean canPlaceCactus(Block block) {
        return block.getRelative(BlockFace.NORTH).getType() == Material.AIR
                && block.getRelative(BlockFace.SOUTH).getType() == Material.AIR
                && block.getRelative(BlockFace.EAST).getType() == Material.AIR
                && block.getRelative(BlockFace.WEST).getType() == Material.AIR;
    }

    /**
     * Plays the vanilla bonemeal visual and audio effect at the given block.
     * This is shared by both the player right-click and dispenser code paths.
     */
    public static void playBonemealEffect(Block block) {
        block.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                block.getLocation().add(0.5, 0.5, 0.5),
                15, 0.5, 0.5, 0.5, 0);
        block.getWorld().playSound(
                block.getLocation(),
                Sound.ITEM_BONE_MEAL_USE,
                SoundCategory.BLOCKS,
                1.0f, 1.0f);
    }
}
