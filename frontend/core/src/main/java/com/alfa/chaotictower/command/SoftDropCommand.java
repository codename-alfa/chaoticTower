package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;

/**
 * Sets the controlled block's fall speed — normal or fast (soft drop).
 * This command is applied continuously (every frame while the key is held),
 * unlike the other commands which fire once on key-press.
 */
public class SoftDropCommand implements InputCommand {

    private static final float FALL_SPEED_NORMAL = -1.5f;
    private static final float FALL_SPEED_FAST   = -12.0f;

    private final boolean fast;

    /**
     * @param fast true if the player is holding the drop key, false for normal speed
     */
    public SoftDropCommand(boolean fast) {
        this.fast = fast;
    }

    @Override
    public void execute(Block block) {
        if (block == null || !block.isControlled()) return;
        float speed = fast ? FALL_SPEED_FAST : FALL_SPEED_NORMAL;
        block.body.setLinearVelocity(0, speed);
    }
}
