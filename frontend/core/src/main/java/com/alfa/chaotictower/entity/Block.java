package com.alfa.chaotictower.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;

public class Block implements Pool.Poolable {

    public static final float TILE_SIZE = 0.5f;
    private static final float HALF_TILE = (TILE_SIZE / 2f) - 0.01f;

    private static final float DENSITY              = 8.0f;
    private static final float FRICTION             = 10.0f;
    private static final float RESTITUTION          = 0.0f;

    private static final float RELEASED_GRAVITY_SCALE   = 1.8f;
    private static final float RELEASED_ANGULAR_DAMPING = 8.0f;
    private static final float RELEASED_LINEAR_DAMPING  = 0.8f;

    public Body body;
    public int ownerId;
    private boolean isControlled;
    private int tetrominoType;
    /** Tile center positions in local body space (offset * TILE_SIZE). */
    private Vector2[] localTilePositions;

    private final FixtureDef cachedFixtureDef;

    private float scale = 1.0f;
    private boolean frosted = false;

    public Block() {
        cachedFixtureDef = new FixtureDef();
        cachedFixtureDef.density     = DENSITY;
        cachedFixtureDef.friction    = FRICTION;
        cachedFixtureDef.restitution = RESTITUTION;
    }

    public void init(World world, float x, float y, Vector2[] tileOffsets, int ownerId, int tetrominoType, float scale) {
        this.ownerId = ownerId;
        this.tetrominoType = tetrominoType;
        this.scale = scale;

        // Store scaled tile positions for rendering
        localTilePositions = new Vector2[tileOffsets.length];
        for (int i = 0; i < tileOffsets.length; i++) {
            localTilePositions[i] = new Vector2(tileOffsets[i]).scl(TILE_SIZE * scale);
        }

        BodyDef bodyDef = new BodyDef();
        // WAJIB DynamicBody — bukan KinematicBody.
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        
        // Align spawn X to correct grid line/cell based on width parity to eliminate landing snap
        float grid = TILE_SIZE * scale;
        float adjustedX = x;
        if (tileOffsets != null && tileOffsets.length > 0) {
            float firstTileX = tileOffsets[0].x;
            boolean isEven = Math.abs(firstTileX - Math.round(firstTileX)) > 0.25f;
            if (isEven) {
                adjustedX = x + grid / 2f;
            }
        }
        bodyDef.position.set(adjustedX, y);
        body = world.createBody(bodyDef);

        float halfTile = (TILE_SIZE * scale / 2f) - 0.01f;
        for (Vector2 offset : tileOffsets) {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(halfTile, halfTile, new Vector2(offset).scl(TILE_SIZE * scale), 0);
            cachedFixtureDef.shape = shape;
            body.createFixture(cachedFixtureDef);
            shape.dispose();
        }
        cachedFixtureDef.shape = null;

        setControlled(true);
    }

    public float getSnappedX() {
        if (body == null) return 0f;
        Vector2 pos = body.getPosition();
        float grid = TILE_SIZE * scale;
        if (localTilePositions != null && localTilePositions.length > 0) {
            float angle = body.getAngle();
            float cos = com.badlogic.gdx.math.MathUtils.cos(angle);
            float sin = com.badlogic.gdx.math.MathUtils.sin(angle);
            Vector2 localPos = localTilePositions[0];
            float offsetRotX = (localPos.x * cos - localPos.y * sin) / grid;
            boolean isEven = Math.abs(offsetRotX - Math.round(offsetRotX)) > 0.25f;

            if (isEven) {
                float halfGrid = grid / 2f;
                return Math.round((pos.x - halfGrid) / grid) * grid + halfGrid;
            } else {
                return Math.round(pos.x / grid) * grid;
            }
        } else {
            return Math.round(pos.x / grid) * grid;
        }
    }

    public void setControlled(boolean controlled) {
        this.isControlled = controlled;
        if (controlled) {
            body.setGravityScale(0f);
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            body.setFixedRotation(true);
            body.setLinearDamping(0f);
            body.setAngularDamping(0f);
        } else {
            // Snap X to nearest grid cell using the helper so landing snap distance is exactly 0.0f
            float snappedX = getSnappedX();
            body.setTransform(snappedX, body.getPosition().y, body.getAngle());

            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            body.setGravityScale(RELEASED_GRAVITY_SCALE);
            body.setFixedRotation(false);
            body.setAngularDamping(RELEASED_ANGULAR_DAMPING);
            body.setLinearDamping(RELEASED_LINEAR_DAMPING);
        }
    }

    public boolean isControlled() {
        return isControlled;
    }

    public int getTetrominoType() {
        return tetrominoType;
    }

    public Vector2[] getLocalTilePositions() {
        return localTilePositions;
    }

    // ─── Spell visual flags ────────────────────────────────────────
    private boolean cemented = false;
    private boolean ivied    = false;

    public boolean isCemented() { return cemented; }
    public void setCemented(boolean cemented) { this.cemented = cemented; }

    public boolean isIvied() { return ivied; }
    public void setIvied(boolean ivied) { this.ivied = ivied; }

    public float getScale() { return scale; }
    public void setScale(float scale) { this.scale = scale; }

    public boolean isFrosted() { return frosted; }
    public void setFrosted(boolean frosted) { this.frosted = frosted; }

    @Override
    public void reset() {
        body              = null;
        ownerId           = -1;
        isControlled      = false;
        tetrominoType     = 0;
        localTilePositions = null;
        cemented          = false;
        ivied             = false;
        scale             = 1.0f;
        frosted           = false;
    }
}
