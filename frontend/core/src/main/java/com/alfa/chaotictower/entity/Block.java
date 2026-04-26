package com.alfa.chaotictower.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;

public class Block implements Pool.Poolable {
    public Body body;
    private final float TILE_SIZE = 1.0f;
    private boolean isControlled;

    public void init(World world, float x, float y, Vector2[] tileOffsets) {
        if (body == null) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(x, y);
            body = world.createBody(bodyDef);
        } else {
            body.setTransform(x, y, 0);
            body.setLinearVelocity(0, 0);
            body.setAngularVelocity(0);
            body.setActive(true);
            body.setAwake(true);
            while (body.getFixtureList().size > 0) {
                body.destroyFixture(body.getFixtureList().first());
            }
        }

        float halfTile = TILE_SIZE / 2f;
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.density = 5.0f;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0.02f;

        for (Vector2 offset : tileOffsets) {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(halfTile, halfTile, new Vector2(offset).scl(TILE_SIZE), 0);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
            shape.dispose();
        }

        setControlled(true);
    }

    public void setControlled(boolean controlled) {
        this.isControlled = controlled;
        if (controlled) {
            body.setGravityScale(0);
            body.setFixedRotation(true);
        } else {
            body.setGravityScale(2.5f);
            body.setFixedRotation(false);
            body.setAngularDamping(0.8f);
        }
    }

    public boolean isControlled() {
        return isControlled;
    }

    @Override
    public void reset() {
        if (body != null) {
            body.setActive(false);
        }
    }
}
