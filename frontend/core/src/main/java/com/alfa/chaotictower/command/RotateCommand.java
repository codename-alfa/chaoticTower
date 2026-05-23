package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Rotates the controlled block 90 degrees clockwise.
 * If the rotation causes an overlap (with walls or other blocks),
 * attempts wall-kick shifts of ±0.5 and ±1.0 units (scaled by block scale).
 * If all kicks fail, reverts to the original transform.
 */
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

        // Temporarily rotate the block to the new angle to calculate the exact snapped X for the new parity
        body.setTransform(origPos.x, origPos.y, newAngle);
        float snappedX = block.getSnappedX();

        // Scale the wall-kick offsets by the block's current scale
        float scale = block.getScale();
        float[] kickOffsets = { 0f, 0.5f * scale, -0.5f * scale, 1.0f * scale, -1.0f * scale };

        for (float kick : kickOffsets) {
            body.setTransform(snappedX + kick, origPos.y, newAngle);

            // Check if any fixture of this block overlaps with other bodies
            if (!CollisionUtils.hasOverlap(body, world)) {
                return; // Rotation + kick successful
            }
        }

        // All kicks failed — revert
        body.setTransform(origPos, origAngle);
    }
}
