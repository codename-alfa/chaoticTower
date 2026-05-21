package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;

/**
 * Puzzle Mode:
 *   Goal: Stack as many blocks as possible BELOW a laser line.
 *   If a block falls off, the floor rises as a penalty (reducing available space).
 *   Game ends when a block crosses the laser line or the target block count is reached.
 *
 *   SP: Solo challenge to fit maximum blocks.
 *   MP: Both players stack — most blocks placed below the line wins.
 */
public class PuzzleStrategy implements GameModeStrategy {

    private static final float LASER_HEIGHT = 15f;  // meters above pedestal top
    private static final int TARGET_BLOCKS  = 25;   // blocks to place to "win"
    private static final float FLOOR_PENALTY = 0.5f; // floor rises this much per lost block

    private final int[] blocksPlaced;
    private final float[] floorPenalty;

    public PuzzleStrategy(int playerCount) {
        blocksPlaced = new int[playerCount];
        floorPenalty = new float[playerCount];
    }

    @Override
    public String getModeName() {
        return "Puzzle";
    }

    @Override
    public int getInitialLives() {
        return 999; // Effectively infinite — no lives mechanic in Puzzle
    }

    @Override
    public boolean checkWinCondition(Player[] players, double elapsedTime, float[] maxHeights) {
        for (int i = 0; i < players.length; i++) {
            if (blocksPlaced[i] >= TARGET_BLOCKS) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkLoseCondition(Player[] players, double elapsedTime) {
        // Lose if any player's tower crosses the laser line
        // (checked externally via maxHeight in PlayingScreen)
        return false;
    }

    /**
     * Check if the tower has crossed the laser line (adjusted for floor penalty).
     */
    public boolean isAboveLaser(int playerIndex, float maxHeight) {
        return maxHeight >= getEffectiveLaserHeight(playerIndex);
    }

    @Override
    public String getResultText(Player[] players, double elapsedTime, float[] maxHeights) {
        if (players.length == 1) {
            return "Blocks placed: " + blocksPlaced[0] + "\nTime: " + String.format("%.1f", elapsedTime) + "s";
        }
        // MP: player who placed more blocks wins
        if (blocksPlaced[0] > blocksPlaced[1]) {
            return "Player 1 Wins!\nP1: " + blocksPlaced[0] + " blocks  P2: " + blocksPlaced[1] + " blocks";
        } else if (blocksPlaced[1] > blocksPlaced[0]) {
            return "Player 2 Wins!\nP1: " + blocksPlaced[0] + " blocks  P2: " + blocksPlaced[1] + " blocks";
        }
        return "Draw! Both placed " + blocksPlaced[0] + " blocks";
    }

    @Override
    public String getBackendModeKey() {
        return "PUZZLE";
    }

    // ─── Puzzle-specific API ─────────────────────────────────────────

    public void onBlockPlaced(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < blocksPlaced.length) {
            blocksPlaced[playerIndex]++;
        }
    }

    public void onBlockLost(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < floorPenalty.length) {
            floorPenalty[playerIndex] += FLOOR_PENALTY;
        }
    }

    public int getBlocksPlaced(int playerIndex) {
        return blocksPlaced[playerIndex];
    }

    /** Laser height in world units (above pedestal top), adjusted for penalties. */
    public float getEffectiveLaserHeight(int playerIndex) {
        return LASER_HEIGHT - floorPenalty[playerIndex];
    }

    /** Raw laser height (no penalty). */
    public float getLaserHeight() {
        return LASER_HEIGHT;
    }

    public float getFloorPenalty(int playerIndex) {
        return floorPenalty[playerIndex];
    }

    public int getTargetBlocks() {
        return TARGET_BLOCKS;
    }
}
