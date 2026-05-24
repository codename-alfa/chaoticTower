package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;


public interface GameModeStrategy {

    
    String getModeName();

    
    int getInitialLives();

    
    boolean checkWinCondition(Player[] players, double elapsedTime, float[] maxHeights);

    
    boolean checkLoseCondition(Player[] players, double elapsedTime);

    
    String getResultText(Player[] players, double elapsedTime, float[] maxHeights);

    
    String getBackendModeKey();
}
