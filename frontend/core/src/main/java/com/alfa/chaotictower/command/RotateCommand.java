package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;

/** Rotates the controlled block 90 degrees clockwise. */
public class RotateCommand implements InputCommand {

    @Override
    public void execute(Block block) {
        if (block == null || !block.isControlled()) return;
        Vector2 pos = block.body.getPosition();
        block.body.setTransform(pos.x, pos.y, block.body.getAngle() + (float) Math.PI / 2);
    }
}
