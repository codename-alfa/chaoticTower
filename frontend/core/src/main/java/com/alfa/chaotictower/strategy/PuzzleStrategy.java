package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;


public class PuzzleStrategy implements GameModeStrategy {

    private static final float LASER_HEIGHT = 15f;  
    private static final int TARGET_BLOCKS  = 25;   
    private static final float FLOOR_PENALTY = 0.5f; 

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
        return 999; 
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
        
        
        return false;
    }

    
    public boolean isAboveLaser(int playerIndex, float maxHeight) {
        return maxHeight >= getEffectiveLaserHeight(playerIndex);
    }

    @Override
    public String getResultText(Player[] players, double elapsedTime, float[] maxHeights) {
        if (players.length == 1) {
            return "Blocks placed: " + blocksPlaced[0] + "\nTime: " + String.format("%.1f", elapsedTime) + "s";
        }
        
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

    
    public float getEffectiveLaserHeight(int playerIndex) {
        return LASER_HEIGHT - floorPenalty[playerIndex];
    }

    
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
