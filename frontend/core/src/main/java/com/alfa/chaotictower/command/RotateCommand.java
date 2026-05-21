package com.alfa.chaotictower.command;

import com.alfa.chaotictower.entity.Block;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Rotates the controlled block 90 degrees clockwise.
 * If the rotation causes an overlap (with walls or other blocks),
 * attempts wall-kick shifts of ±0.5 and ±1.0 units.
 * If all kicks fail, reverts to the original transform.
 */
public class RotateCommand implements InputCommand {

    private static final float ROTATE_ANGLE = (float) Math.PI / 2;
    private static final float[] KICK_OFFSETS = { 0f, 0.5f, -0.5f, 1.0f, -1.0f };

    @Override
    public void execute(Block block) {
        if (block == null || !block.isControlled()) return;

        Body body = block.body;
        Vector2 origPos = body.getPosition().cpy();
        float origAngle = body.getAngle();
        float newAngle = origAngle + ROTATE_ANGLE;

        World world = body.getWorld();

        for (float kick : KICK_OFFSETS) {
            body.setTransform(origPos.x + kick, origPos.y, newAngle);

            // Check if any fixture of this block overlaps with other bodies
            if (!hasOverlap(body, world)) {
                return; // Rotation + kick successful
            }
        }

        // All kicks failed — revert
        body.setTransform(origPos, origAngle);
    }

    /**
     * Checks whether any fixture of the given body overlaps with other bodies
     * by querying the AABB of each fixture.
     */
    private boolean hasOverlap(Body body, World world) {
        for (Fixture fixture : body.getFixtureList()) {
            Shape shape = fixture.getShape();
            if (shape instanceof PolygonShape) {
                PolygonShape poly = (PolygonShape) shape;

                // Compute world-space AABB of this fixture
                float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
                float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;

                Vector2 vertex = new Vector2();
                for (int i = 0; i < poly.getVertexCount(); i++) {
                    poly.getVertex(i, vertex);
                    Vector2 worldV = body.getWorldPoint(vertex);
                    minX = Math.min(minX, worldV.x);
                    minY = Math.min(minY, worldV.y);
                    maxX = Math.max(maxX, worldV.x);
                    maxY = Math.max(maxY, worldV.y);
                }

                // Shrink AABB slightly to avoid false positives at touching edges
                float shrink = 0.02f;
                minX += shrink; minY += shrink;
                maxX -= shrink; maxY -= shrink;

                // Query world for overlapping fixtures
                final boolean[] found = { false };
                world.QueryAABB(new QueryCallback() {
                    @Override
                    public boolean reportFixture(Fixture f) {
                        if (f.getBody() != body) {
                            found[0] = true;
                            return false; // Stop query
                        }
                        return true; // Continue query
                    }
                }, minX, minY, maxX, maxY);

                if (found[0]) return true;
            }
        }
        return false;
    }
}
