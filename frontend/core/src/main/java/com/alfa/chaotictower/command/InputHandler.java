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
 *
 * Supports DAS (Delayed Auto-Shift) and ARR (Auto-Repeat Rate)
 * for left/right movement, matching Tricky Towers' hold-to-slide behavior.
 */
public class InputHandler {

    // DAS: initial delay before auto-repeat kicks in (seconds)
    private static final float DAS_DELAY = 0.20f;
    // ARR: interval between repeated moves once DAS triggers (seconds)
    private static final float ARR_INTERVAL = 0.05f;

    private final int leftKey;
    private final int rightKey;
    private final int dropKey;
    private final int rotateKey;

    private final MoveLeftCommand  moveLeft  = new MoveLeftCommand();
    private final MoveRightCommand moveRight = new MoveRightCommand();
    private final RotateCommand    rotate    = new RotateCommand();
    private final SoftDropCommand  softDropNormal = new SoftDropCommand(false);
    private final SoftDropCommand  softDropFast   = new SoftDropCommand(true);

    // DAS/ARR timers for left and right keys
    private float leftHoldTime  = 0f;
    private float rightHoldTime = 0f;
    private boolean leftWasPressed  = false;
    private boolean rightWasPressed = false;

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

        float delta = Gdx.graphics.getDeltaTime();

        // Apply speed multiplier from SpeedUp spell
        float speedMul = player.isSpedUp() ? 3.0f : 1.0f;
        softDropNormal.setSpeedMultiplier(speedMul);
        softDropFast.setSpeedMultiplier(speedMul);

        // Continuous command: set fall speed every frame (reuse cached instances)
        boolean isFastDrop = Gdx.input.isKeyPressed(dropKey);
        (isFastDrop ? softDropFast : softDropNormal).execute(block);

        // DAS/ARR for left movement
        handleDASKey(block, leftKey, moveLeft, delta, true);
        // DAS/ARR for right movement
        handleDASKey(block, rightKey, moveRight, delta, false);

        // Discrete commands: fire once on key-press
        if (Gdx.input.isKeyJustPressed(rotateKey)) {
            rotate.execute(block);
        }
    }

    /**
     * Handles DAS/ARR logic for a single direction key.
     * On initial press: immediately fire the command.
     * While held: after DAS_DELAY, fire at ARR_INTERVAL rate.
     */
    private void handleDASKey(Block block, int key, InputCommand command, float delta, boolean isLeft) {
        boolean pressed = Gdx.input.isKeyPressed(key);
        boolean wasPressed = isLeft ? leftWasPressed : rightWasPressed;
        float holdTime = isLeft ? leftHoldTime : rightHoldTime;

        if (pressed) {
            if (!wasPressed) {
                // Initial press — fire immediately
                command.execute(block);
                holdTime = 0f;
            } else {
                // Held down — accumulate time
                holdTime += delta;
                if (holdTime >= DAS_DELAY) {
                    // In ARR zone: fire at ARR_INTERVAL
                    float arrTime = holdTime - DAS_DELAY;
                    float prevArrTime = arrTime - delta;
                    // Fire if we crossed an ARR interval boundary
                    if ((int)(arrTime / ARR_INTERVAL) > (int)(Math.max(0, prevArrTime) / ARR_INTERVAL)) {
                        command.execute(block);
                    }
                }
            }
        } else {
            holdTime = 0f;
        }

        // Store back
        if (isLeft) {
            leftHoldTime = holdTime;
            leftWasPressed = pressed;
        } else {
            rightHoldTime = holdTime;
            rightWasPressed = pressed;
        }
    }
}
