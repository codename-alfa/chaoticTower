package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;


public class SoftDropCommand implements InputCommand {

    private static final float FALL_SPEED_NORMAL = -2.0f;
    private static final float FALL_SPEED_FAST   = -8.0f;

    private final boolean fast;
    private float speedMultiplier = 1.0f;

    
    public SoftDropCommand(boolean fast) {
        this.fast = fast;
    }

    
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    @Override
    public void execute(Block block) {
        if (block == null || !block.isControlled()) return;
        float speed = fast ? FALL_SPEED_FAST : FALL_SPEED_NORMAL;
        block.body.setLinearVelocity(0, speed * speedMultiplier);
    }
}
