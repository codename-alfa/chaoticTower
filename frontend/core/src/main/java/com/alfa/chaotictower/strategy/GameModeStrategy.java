package com.alfa.chaotictower.strategy;

import com.alfa.chaotictower.entity.Player;

/**
 * Strategy Pattern (Design Pattern #7).
 * Encapsulates mode-specific win/loss conditions so that PlayingScreen
 * does not need to branch on the game mode type.
 */
public interface GameModeStrategy {

    /** Returns the display name of this game mode (e.g. "SURVIVAL", "RACE"). */
    String getModeName();

    /** Number of lives each player starts with. */
    int getInitialLives();

    /**
     * Called every frame to check if any win condition has been triggered.
     * @param players     array of active players (1 or 2 elements)
     * @param elapsedTime seconds since the round started
     * @param maxHeights  parallel array — max tower height per player
     * @return true if the game should end due to a WIN
     */
    boolean checkWinCondition(Player[] players, double elapsedTime, float[] maxHeights);

    /**
     * Called every frame to check if any lose condition has been triggered.
     * @return true if the game should end due to a LOSS
     */
    boolean checkLoseCondition(Player[] players, double elapsedTime);

    /**
     * Produces a human-readable result text for the GameOverScreen.
     * Called after checkWinCondition or checkLoseCondition returns true.
     */
    String getResultText(Player[] players, double elapsedTime, float[] maxHeights);

    /**
     * Returns the game mode string used when submitting scores to the backend.
     * E.g. "SURVIVAL", "RACE", "TIME_ATTACK".
     */
    String getBackendModeKey();
}
