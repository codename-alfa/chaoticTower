package com.alfa.chaotictower.command;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Utility class for checking physical overlaps in the Box2D world.
 */
public class CollisionUtils {

    /**
     * Checks whether any fixture of the given body overlaps with other bodies
     * by querying the AABB of each fixture.
     * Shrinks the query bounding boxes slightly to allow perfectly adjacent placements.
     */
    public static boolean hasOverlap(Body body, World world) {
        for (Fixture fixture : body.getFixtureList()) {
            Shape shape = fixture.getShape();
            if (polyShapeHasOverlap(body, world, shape)) {
                return true;
            }
        }
        return false;
    }

    private static boolean polyShapeHasOverlap(Body body, World world, Shape shape) {
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
        return false;
    }
}
