package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;

/**
 * Race Mode (Multiplayer only):
 *   First player whose tower reaches the target height wins.
 *   Players can still lose lives; if both lose all lives, it's a draw.
 */
public class RaceStrategy implements GameModeStrategy {

    private static final float TARGET_HEIGHT = 20f; // 20 meters (Box2D units)

    @Override
    public String getModeName() {
        return "Race";
    }

    @Override
    public int getInitialLives() {
        return 3;
    }

    @Override
    public boolean checkWinCondition(Player[] players, double elapsedTime, float[] maxHeights) {
        for (float h : maxHeights) {
            if (h >= TARGET_HEIGHT) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkLoseCondition(Player[] players, double elapsedTime) {
        // In 2P race, game ends if BOTH players lose all lives
        boolean allDead = true;
        for (Player p : players) {
            if (p != null && p.getLives() > 0) {
                allDead = false;
                break;
            }
        }
        return allDead;
    }

    @Override
    public String getResultText(Player[] players, double elapsedTime, float[] maxHeights) {
        // Check who reached target height first
        int winnerIdx = -1;
        for (int i = 0; i < maxHeights.length; i++) {
            if (maxHeights[i] >= TARGET_HEIGHT) {
                winnerIdx = i;
                break;
            }
        }

        if (winnerIdx >= 0) {
            return "Player " + (winnerIdx + 1) + " reached the top!\nTime: " + String.format("%.1f", elapsedTime) + "s";
        }

        // Both lost all lives
        return "Both players fell! It's a draw.";
    }

    @Override
    public String getBackendModeKey() {
        return "RACE";
    }

    public float getTargetHeight() {
        return TARGET_HEIGHT;
    }
}
