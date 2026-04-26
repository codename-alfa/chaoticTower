package com.alfa.chaotictower.entity;

import com.alfa.chaotictower.factory.BlockFactory;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    private final int id;
    private int lives;
    private Block currentBlock;
    private final float spawnX;
    private final float spawnY;
    private float spawnTimer = 0;
    private float invulnerableTimer = 0;

    public Player(int id, float spawnX, float spawnY) {
        this.id = id;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.lives = 3;
    }

    public void spawnNewBlock(World world) {
        currentBlock = BlockFactory.getInstance().spawnBlock(world, spawnX, spawnY, id);
    }

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public void clearCurrentBlock() {
        currentBlock = null;
        spawnTimer = 0.5f;
    }

    public void update(float delta) {
        if (currentBlock == null) {
            spawnTimer -= delta;
        }
        if (invulnerableTimer > 0) {
            invulnerableTimer -= delta;
        }
    }

    public boolean canSpawn() {
        return currentBlock == null && spawnTimer <= 0;
    }

    public void loseLife() {
        if (invulnerableTimer <= 0) {
            lives--;
            invulnerableTimer = 2.0f;
        }
    }

    public int getLives() {
        return lives;
    }

    public int getId() {
        return id;
    }
}
