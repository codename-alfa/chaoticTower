package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;

/**
 * Sets the controlled block's fall speed — normal or fast (soft drop).
 * This command is applied continuously (every frame while the key is held),
 * unlike the other commands which fire once on key-press.
 *
 * Supports a speed multiplier for the SpeedUp dark magic spell.
 */
public class SoftDropCommand implements InputCommand {

    private static final float FALL_SPEED_NORMAL = -2.0f;
    private static final float FALL_SPEED_FAST   = -8.0f;

    private final boolean fast;
    private float speedMultiplier = 1.0f;

    /**
     * @param fast true if the player is holding the drop key, false for normal speed
     */
    public SoftDropCommand(boolean fast) {
        this.fast = fast;
    }

    /**
     * Set a speed multiplier (used by SpeedUp spell).
     * 1.0 = normal, 3.0 = 3x faster.
     */
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
