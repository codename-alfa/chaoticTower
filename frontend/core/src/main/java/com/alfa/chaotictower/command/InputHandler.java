package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.entity.Player;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;


public class InputHandler {

    
    private static final float DAS_DELAY = 0.20f;
    
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

    
    private float leftHoldTime  = 0f;
    private float rightHoldTime = 0f;
    private boolean leftWasPressed  = false;
    private boolean rightWasPressed = false;

    
    public InputHandler(int leftKey, int rightKey, int dropKey, int rotateKey) {
        this.leftKey   = leftKey;
        this.rightKey  = rightKey;
        this.dropKey   = dropKey;
        this.rotateKey = rotateKey;
    }

    
    public void handleInput(Player player) {
        Block block = player.getCurrentBlock();
        if (block == null || !block.isControlled()) return;

        float delta = Gdx.graphics.getDeltaTime();

        
        float speedMul = player.isSpedUp() ? 3.0f : 1.0f;
        softDropNormal.setSpeedMultiplier(speedMul);
        softDropFast.setSpeedMultiplier(speedMul);

        
        boolean isFastDrop = Gdx.input.isKeyPressed(dropKey);
        (isFastDrop ? softDropFast : softDropNormal).execute(block);

        
        handleDASKey(block, leftKey, moveLeft, delta, true);
        
        handleDASKey(block, rightKey, moveRight, delta, false);

        
        if (Gdx.input.isKeyJustPressed(rotateKey)) {
            rotate.execute(block);
        }
    }

    
    private void handleDASKey(Block block, int key, InputCommand command, float delta, boolean isLeft) {
        boolean pressed = Gdx.input.isKeyPressed(key);
        boolean wasPressed = isLeft ? leftWasPressed : rightWasPressed;
        float holdTime = isLeft ? leftHoldTime : rightHoldTime;

        if (pressed) {
            if (!wasPressed) {
                
                command.execute(block);
                holdTime = 0f;
            } else {
                
                holdTime += delta;
                if (holdTime >= DAS_DELAY) {
                    
                    float arrTime = holdTime - DAS_DELAY;
                    float prevArrTime = arrTime - delta;
                    
                    if ((int)(arrTime / ARR_INTERVAL) > (int)(Math.max(0, prevArrTime) / ARR_INTERVAL)) {
                        command.execute(block);
                    }
                }
            }
        } else {
            holdTime = 0f;
        }

        
        if (isLeft) {
            leftHoldTime = holdTime;
            leftWasPressed = pressed;
        } else {
            rightHoldTime = holdTime;
            rightWasPressed = pressed;
        }
    }
}
