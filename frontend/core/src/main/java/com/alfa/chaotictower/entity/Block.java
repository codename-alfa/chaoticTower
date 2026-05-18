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

    private static final float DENSITY              = 1.5f;
    private static final float FRICTION             = 0.55f;
    private static final float RESTITUTION          = 0.0f;

    private static final float RELEASED_GRAVITY_SCALE   = 1.2f;
    private static final float RELEASED_ANGULAR_DAMPING = 2.5f;
    private static final float RELEASED_LINEAR_DAMPING  = 0.05f;

    public Body body;
    public int ownerId;
    private boolean isControlled;
    private int tetrominoType;
    /** Tile center positions in local body space (offset * TILE_SIZE). */
    private Vector2[] localTilePositions;

    private final FixtureDef cachedFixtureDef;

    public Block() {
        cachedFixtureDef = new FixtureDef();
        cachedFixtureDef.density     = DENSITY;
        cachedFixtureDef.friction    = FRICTION;
        cachedFixtureDef.restitution = RESTITUTION;
    }

    public void init(World world, float x, float y, Vector2[] tileOffsets, int ownerId, int tetrominoType) {
        this.ownerId = ownerId;
        this.tetrominoType = tetrominoType;

        // Store scaled tile positions for rendering
        localTilePositions = new Vector2[tileOffsets.length];
        for (int i = 0; i < tileOffsets.length; i++) {
            localTilePositions[i] = new Vector2(tileOffsets[i]).scl(TILE_SIZE);
        }

        BodyDef bodyDef = new BodyDef();
        // WAJIB DynamicBody — bukan KinematicBody.
        // KinematicBody tidak mendapat penetration correction dari StaticBody,
        // sehingga blok menembus pulau tanpa hambatan.
        // DynamicBody mendapat koreksi penuh; gravitasi dimatikan via gravityScale=0.
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        body = world.createBody(bodyDef);

        for (Vector2 offset : tileOffsets) {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(HALF_TILE, HALF_TILE, new Vector2(offset).scl(TILE_SIZE), 0);
            cachedFixtureDef.shape = shape;
            body.createFixture(cachedFixtureDef);
            shape.dispose();
        }
        cachedFixtureDef.shape = null;

        setControlled(true);
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

    @Override
    public void reset() {
        body              = null;
        ownerId           = -1;
        isControlled      = false;
        tetrominoType     = 0;
        localTilePositions = null;
    }
}
