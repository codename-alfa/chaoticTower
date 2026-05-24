package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;


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
            
            return "You survived " + String.format("%.1f", elapsedTime) + " seconds!";
        }

        
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
