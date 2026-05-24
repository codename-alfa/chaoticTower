package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;


public class MoveLeftCommand implements InputCommand {

    private static final float MOVE_STEP = 0.5f;

    @Override
    public void execute(Block block) {
        if (block == null || !block.isControlled()) return;
        Vector2 pos = block.body.getPosition();
        float origX = pos.x;
        float moveStep = MOVE_STEP * block.getScale();
        float newX = origX - moveStep;
        
        block.body.setTransform(newX, pos.y, block.body.getAngle());
        if (CollisionUtils.hasOverlap(block.body, block.body.getWorld())) {
            
            block.body.setTransform(origX, pos.y, block.body.getAngle());
        }
    }
}
