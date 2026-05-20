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

    /** Supported growth modes. */
    public enum GrowthMode {
        /**
         * Legacy mode: bonemeal always grows the plant by 1 block
         * (with a configurable small chance of +2).
         */
        LEGACY,
        /**
         * Chance mode: bonemeal is consumed on every use but only grows
         * the plant with a configurable probability.
         */
        CHANCE
    }

    /**
     * Result of a {@link #tryGrow} call, indicating what happened and
     * whether bonemeal should be consumed.
     */
    public enum GrowthResult {
        /** Plant is already at max height – do nothing, do not consume. */
        SKIPPED,
        /** Bonemeal was consumed but the plant did not grow (chance mode). */
        CONSUMED,
        /** Bonemeal was consumed and the plant grew. */
        GREW
    }

    private static GrowthMode mode = GrowthMode.LEGACY;
    private static double doubleGrowChance = 0.10;
    private static double chanceGrowChance = 0.10;

    private GrowthUtil() {}

    /**
     * Applies the current config to {@link GrowthUtil}.
     * Call this from {@code onEnable} (and on config reload).
     */
    public static void configure(GrowthMode newMode, double legacyDoubleChance, double chanceGrowProbability) {
        mode = newMode;
        doubleGrowChance = legacyDoubleChance;
        chanceGrowChance = chanceGrowProbability;
    }

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
     * <p>Behaviour depends on the configured {@link GrowthMode}:</p>
     * <ul>
     *   <li><b>LEGACY</b>: always grows by 1 (small chance for +2); bonemeal
     *       is consumed only when growth occurs.</li>
     *   <li><b>CHANCE</b>: bonemeal is always consumed (when below max height);
     *       the plant grows with a configurable probability.</li>
     * </ul>
     *
     * @return {@link GrowthResult#SKIPPED} if the column is already at max
     *         height, {@link GrowthResult#GREW} if at least one block was
     *         placed, or {@link GrowthResult#CONSUMED} if the bonemeal was
     *         used but no block was placed (chance mode only).
     */
    public static GrowthResult tryGrow(Block bottomBlock) {
        int height = getStackHeight(bottomBlock);
        if (height >= MAX_HEIGHT) {
            return GrowthResult.SKIPPED;
        }

        return mode == GrowthMode.CHANCE
                ? tryGrowChance(bottomBlock, height)
                : tryGrowLegacy(bottomBlock, height);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private static GrowthResult tryGrowLegacy(Block bottomBlock, int height) {
        Material type = bottomBlock.getType();

        Block topBlock = bottomBlock;
        for (int i = 1; i < height; i++) {
            topBlock = topBlock.getRelative(BlockFace.UP);
        }

        int targetGrowth = ThreadLocalRandom.current().nextDouble() < doubleGrowChance ? 2 : 1;
        targetGrowth = Math.min(targetGrowth, MAX_HEIGHT - height);

        int placed = 0;
        for (int i = 0; i < targetGrowth; i++) {
            Block next = topBlock.getRelative(BlockFace.UP);

            if (next.getType() != Material.AIR) break;
            if (type == Material.CACTUS && !canPlaceCactus(next)) break;

            next.setType(type);
            topBlock = next;
            placed++;
        }

        return placed > 0 ? GrowthResult.GREW : GrowthResult.SKIPPED;
    }

    private static GrowthResult tryGrowChance(Block bottomBlock, int height) {
        // Bonemeal is always consumed in chance mode (when below max height).
        if (ThreadLocalRandom.current().nextDouble() >= chanceGrowChance) {
            return GrowthResult.CONSUMED;
        }

        Material type = bottomBlock.getType();

        Block topBlock = bottomBlock;
        for (int i = 1; i < height; i++) {
            topBlock = topBlock.getRelative(BlockFace.UP);
        }

        Block next = topBlock.getRelative(BlockFace.UP);
        if (next.getType() != Material.AIR) return GrowthResult.CONSUMED;
        if (type == Material.CACTUS && !canPlaceCactus(next)) return GrowthResult.CONSUMED;

        next.setType(type);
        return GrowthResult.GREW;
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
