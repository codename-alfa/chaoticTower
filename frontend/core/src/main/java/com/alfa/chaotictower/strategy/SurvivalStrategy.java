package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;

/**
 * Survival Mode:
 *   SP — Survive as long as possible with 3 lives. No win condition; game ends when lives = 0.
 *   MP — Both players start with 3 lives. The player who loses all lives first loses;
 *         the other player wins.
 */
public class SurvivalStrategy implements GameModeStrategy {

    @Override
    public String getModeName() {
        return "Survival";
    }

    @Override
    public int getInitialLives() {
        return 3;
    }

    @Override
    public boolean checkWinCondition(Player[] players, double elapsedTime, float[] maxHeights) {
        // Survival has no proactive win condition.
        // The game ends only when a player loses (see checkLoseCondition).
        return false;
    }

    @Override
    public boolean checkLoseCondition(Player[] players, double elapsedTime) {
        for (Player p : players) {
            if (p != null && p.getLives() <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getResultText(Player[] players, double elapsedTime, float[] maxHeights) {
        if (players.length == 1) {
            // Single player
            return "You survived " + String.format("%.1f", elapsedTime) + " seconds!";
        }

        // Multiplayer — determine winner
        if (players[0].getLives() <= 0 && players[1].getLives() > 0) {
            return "Player 2 Wins!";
        } else if (players[1].getLives() <= 0 && players[0].getLives() > 0) {
            return "Player 1 Wins!";
        } else {
            return "Draw!";
        }
    }

    @Override
    public String getBackendModeKey() {
        return "SURVIVAL";
    }
}
