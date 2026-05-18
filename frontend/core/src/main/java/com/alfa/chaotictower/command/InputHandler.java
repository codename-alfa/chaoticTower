package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.entity.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Command Pattern (Design Pattern #8).
 * Maps physical key-bindings for a specific player to InputCommand objects,
 * then executes them against that player's currently controlled block.
 *
 * This cleanly separates P1 (WASD) from P2 (Arrow keys) without
 * if-else chains in PlayingScreen.
 */
public class InputHandler {

    private final int leftKey;
    private final int rightKey;
    private final int dropKey;
    private final int rotateKey;

    private final MoveLeftCommand  moveLeft  = new MoveLeftCommand();
    private final MoveRightCommand moveRight = new MoveRightCommand();
    private final RotateCommand    rotate    = new RotateCommand();

    /**
     * @param leftKey   key code for moving left
     * @param rightKey  key code for moving right
     * @param dropKey   key code for soft-drop (hold)
     * @param rotateKey key code for rotating
     */
    public InputHandler(int leftKey, int rightKey, int dropKey, int rotateKey) {
        this.leftKey   = leftKey;
        this.rightKey  = rightKey;
        this.dropKey   = dropKey;
        this.rotateKey = rotateKey;
    }

    /**
     * Polls the keyboard and executes the appropriate commands on the player's block.
     * @param player the player whose block we are controlling
     */
    public void handleInput(Player player) {
        Block block = player.getCurrentBlock();
        if (block == null || !block.isControlled()) return;

        // Continuous command: set fall speed every frame
        boolean isFastDrop = Gdx.input.isKeyPressed(dropKey);
        new SoftDropCommand(isFastDrop).execute(block);

        // Discrete commands: fire once on key-press
        if (Gdx.input.isKeyJustPressed(leftKey)) {
            moveLeft.execute(block);
        }
        if (Gdx.input.isKeyJustPressed(rightKey)) {
            moveRight.execute(block);
        }
        if (Gdx.input.isKeyJustPressed(rotateKey)) {
            rotate.execute(block);
        }
    }
}
