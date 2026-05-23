package com.alfa.chaotictower.entity;

import com.alfa.chaotictower.factory.BlockFactory;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    private final int id;
    private int lives;
    private int score;
    private Block currentBlock;
    private final float spawnX;
    private final float spawnY;
    private float spawnTimer = 0;
    private float invulnerableTimer = 0;
    private float maxHeight = 0f;

    public Player(int id, float spawnX, float spawnY) {
        this(id, spawnX, spawnY, 3);
    }

    public Player(int id, float spawnX, float spawnY, int initialLives) {
        this.id = id;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.lives = initialLives;
        this.score = 0;
    }

    public void spawnNewBlock(World world) {
        float scale = isWeighted() ? 2.0f : 1.0f;
        currentBlock = BlockFactory.getInstance().spawnBlock(world, spawnX, spawnY, id, scale);
        if (isFrosted()) {
            currentBlock.setFrosted(true);
            for (com.badlogic.gdx.physics.box2d.Fixture f : currentBlock.body.getFixtureList()) {
                f.setFriction(0.02f);
            }
        }
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

    public void addScore(int points) {
        score += points;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getId() {
        return id;
    }

    /**
     * Update the maximum tower height for this player.
     * Only stores the highest value ever recorded.
     */
    public void updateMaxHeight(float height) {
        if (height > maxHeight) {
            maxHeight = height;
        }
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    // ─── Spell effect flags ────────────────────────────────────────
    private boolean frosted = false;
    private boolean weighted = false;
    private boolean spedUp = false;

    public boolean isFrosted() { return frosted; }
    public void setFrosted(boolean frosted) { this.frosted = frosted; }

    public boolean isWeighted() { return weighted; }
    public void setWeighted(boolean weighted) { this.weighted = weighted; }

    public boolean isSpedUp() { return spedUp; }
    public void setSpedUp(boolean spedUp) { this.spedUp = spedUp; }
}
