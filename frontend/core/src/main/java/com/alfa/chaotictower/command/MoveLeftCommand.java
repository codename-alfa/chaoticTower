package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;

/** Shifts the controlled block one step to the left. */
public class MoveLeftCommand implements InputCommand {

    private static final float MOVE_STEP = 0.5f;

    @Override
    public void execute(Block block) {
        if (block == null || !block.isControlled()) return;
        Vector2 pos = block.body.getPosition();
        block.body.setTransform(pos.x - MOVE_STEP, pos.y, block.body.getAngle());
    }
}
