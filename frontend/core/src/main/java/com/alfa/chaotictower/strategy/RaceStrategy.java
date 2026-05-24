package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;


public class RaceStrategy implements GameModeStrategy {

    private static final float TARGET_HEIGHT = 20f; 

    @Override
    public String getModeName() {
        return "Race";
    }

    @Override
    public int getInitialLives() {
        return 999; 
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
        
        return false;
    }

    @Override
    public String getResultText(Player[] players, double elapsedTime, float[] maxHeights) {
        
        int winnerIdx = -1;
        float highest = -1f;
        for (int i = 0; i < maxHeights.length; i++) {
            if (maxHeights[i] >= TARGET_HEIGHT) {
                if (winnerIdx == -1 || maxHeights[i] > highest) {
                    winnerIdx = i;
                    highest = maxHeights[i];
                }
            }
        }

        if (winnerIdx >= 0) {
            if (players.length == 1) {
                return "You reached the top!\nTime: " + String.format("%.1f", elapsedTime) + "s";
            }
            return "Player " + (winnerIdx + 1) + " reached the top!\nTime: " + String.format("%.1f", elapsedTime) + "s";
        }

        return "Race completed!";
    }

    @Override
    public String getBackendModeKey() {
        return "RACE";
    }

    public float getTargetHeight() {
        return TARGET_HEIGHT;
    }
}

