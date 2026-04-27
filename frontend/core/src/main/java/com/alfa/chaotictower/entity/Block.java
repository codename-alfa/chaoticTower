package com.alfa.chaotictower.entity;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Pool;

public class Block implements Pool.Poolable {

    private static final float TILE_SIZE = 1.0f;
    private static final float HALF_TILE = (TILE_SIZE / 2f) - 0.01f;

    public Body body;
    public int ownerId;
    private boolean isControlled;

    // FixtureDef di-cache per-instance: dibuat SEKALI di konstruktor,
    // di-reuse setiap kali init() dipanggil untuk block yang sama.
    private final FixtureDef cachedFixtureDef;

    public Block() {
        cachedFixtureDef = new FixtureDef();
        cachedFixtureDef.density = 10.0f;
        cachedFixtureDef.friction = 3.0f;
        cachedFixtureDef.restitution = 0.0f;
    }

    /**
     * Inisialisasi block dengan body Box2D BARU.
     *
     * DESAIN: Body selalu dibuat fresh di sini karena PlayingScreen
     * menghancurkan body lama via world.destroyBody() sebelum memanggil
     * blockPool.free(). Dengan demikian, body == null saat init() dipanggil
     * dan tidak ada risiko referensi ke world yang sudah di-dispose.
     */
    public void init(World world, float x, float y, Vector2[] tileOffsets, int ownerId) {
        this.ownerId = ownerId;

        // Selalu buat body baru — body lama sudah dihancurkan di PlayingScreen
        // sebelum block dikembalikan ke pool.
        BodyDef bodyDef = new BodyDef();
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
            body.setGravityScale(0);
            body.setFixedRotation(true);
            body.setLinearDamping(0);
        } else {
            body.setLinearVelocity(0, 0);
            body.setGravityScale(2.5f);
            body.setFixedRotation(false);
            body.setAngularDamping(0.8f);
            body.setLinearDamping(0.1f);
        }
    }

    public boolean isControlled() {
        return isControlled;
    }

    /**
     * Dipanggil oleh Pool saat block dikembalikan via blockPool.free().
     * Body sudah dihancurkan oleh PlayingScreen sebelum ini dipanggil,
     * jadi cukup null-kan referensinya.
     */
    @Override
    public void reset() {
        body = null;
        ownerId = -1;
        isControlled = false;
    }
}
