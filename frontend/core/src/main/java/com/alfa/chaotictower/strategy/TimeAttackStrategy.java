package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;


public class TimeAttackStrategy implements GameModeStrategy {

    private static final float TARGET_HEIGHT = 20f;  
    private static final double TIME_LIMIT   = 120.0; 

    @Override
    public String getModeName() {
        return "Time Attack";
    }

    @Override
    public int getInitialLives() {
        return 3;
    }

    @Override
    public boolean checkWinCondition(Player[] players, double elapsedTime, float[] maxHeights) {
        
        return maxHeights.length > 0 && maxHeights[0] >= TARGET_HEIGHT && elapsedTime <= TIME_LIMIT;
    }

    @Override
    public boolean checkLoseCondition(Player[] players, double elapsedTime) {
        
        if (elapsedTime > TIME_LIMIT) return true;
        for (Player p : players) {
            if (p != null && p.getLives() <= 0) return true;
        }
        return false;
    }

    @Override
    public String getResultText(Player[] players, double elapsedTime, float[] maxHeights) {
        if (maxHeights.length > 0 && maxHeights[0] >= TARGET_HEIGHT && elapsedTime <= TIME_LIMIT) {
            return "You reached the top!\nTime: " + String.format("%.1f", elapsedTime) + "s";
        }

        if (players[0].getLives() <= 0) {
            return "You lost all your lives!\nMax Height: " + String.format("%.1f", maxHeights[0]) + "m";
        }

        return "Time's up!\nMax Height: " + String.format("%.1f", maxHeights[0]) + "m";
    }

    @Override
    public String getBackendModeKey() {
        return "TIME_ATTACK";
    }

    public float getTargetHeight() {
        return TARGET_HEIGHT;
    }

    public double getTimeLimit() {
        return TIME_LIMIT;
    }
}
