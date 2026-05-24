package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;


public class RotateCommand implements InputCommand {

    private static final float ROTATE_ANGLE = (float) Math.PI / 2;

    @Override
    public void execute(Block block) {
        if (block == null || !block.isControlled()) return;

        Body body = block.body;
        Vector2 origPos = body.getPosition().cpy();
        float origAngle = body.getAngle();
        float newAngle = origAngle + ROTATE_ANGLE;

        World world = body.getWorld();

        
        body.setTransform(origPos.x, origPos.y, newAngle);
        float snappedX = block.getSnappedX();

        
        float scale = block.getScale();
        float[] kickOffsets = { 0f, 0.5f * scale, -0.5f * scale, 1.0f * scale, -1.0f * scale };

        for (float kick : kickOffsets) {
            body.setTransform(snappedX + kick, origPos.y, newAngle);

            
            if (!CollisionUtils.hasOverlap(body, world)) {
                return; 
            }
        }

        
        body.setTransform(origPos, origAngle);
    }
}
