package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.GameAssetManager;
import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.entity.Player;
import com.alfa.chaotictower.factory.BlockFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PlayingScreen extends ScreenAdapter {

    private final Main game;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private final Array<Block> activeBlocks   = new Array<>();
    private final Array<Block> blocksToSettle = new Array<>();

    private Player player1;
    private Player player2;

    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;
    private final StringBuilder hudBuilder = new StringBuilder(32);

    private int screenWidth;
    private int screenHeight;
    private boolean gameOver = false;

    private static final float WORLD_GRAVITY           = -15f;
    private static final int   STEP_VEL_ITERATIONS     = 10;
    private static final int   STEP_POS_ITERATIONS     = 8;
    private static final float LANDING_NORMAL_THRESHOLD = 0.4f;
    private static final float MOVE_STEP               = 0.5f;
    private static final float FALL_SPEED_NORMAL       = -2.5f;
    private static final float FALL_SPEED_FAST         = -12.0f;

    public PlayingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Box2D.init();
        world = new World(new Vector2(0, WORLD_GRAVITY), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(40, 30, camera);

        screenWidth  = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

        hudFont = GameAssetManager.getInstance().manager.get(GameAssetManager.FONT_HUD, BitmapFont.class);

        player1 = new Player(1, 10, 28);
        player2 = new Player(2, 30, 28);

        createEnvironment();
        setupContactListener();

        spawnForPlayer(player1);
        spawnForPlayer(player2);
    }

    private void setupContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                checkLanding(contact);
            }

            @Override
            public void endContact(Contact contact) {}

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                Fixture fa = contact.getFixtureA();
                Fixture fb = contact.getFixtureB();
                if (isFixtureControlled(fa) || isFixtureControlled(fb)) {
                    Vector2 normal = contact.getWorldManifold().getNormal();
                    if (Math.abs(normal.y) < 0.5f) {
                        contact.setFriction(0f);
                    } else {
                        contact.setFriction(1.0f);
                    }
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    private void createEnvironment() {
        BodyDef p1Def = new BodyDef();
        p1Def.position.set(10, 2);
        Body p1Body = world.createBody(p1Def);
        PolygonShape p1Shape = new PolygonShape();
        p1Shape.setAsBox(2.5f, 1f);
        FixtureDef p1FixtureDef = new FixtureDef();
        p1FixtureDef.shape = p1Shape;
        p1FixtureDef.friction = 1.0f;
        p1Body.createFixture(p1FixtureDef);
        p1Shape.dispose();

        BodyDef p2Def = new BodyDef();
        p2Def.position.set(30, 2);
        Body p2Body = world.createBody(p2Def);
        PolygonShape p2Shape = new PolygonShape();
        p2Shape.setAsBox(2.5f, 1f);
        FixtureDef p2FixtureDef = new FixtureDef();
        p2FixtureDef.shape = p2Shape;
        p2FixtureDef.friction = 1.0f;
        p2Body.createFixture(p2FixtureDef);
        p2Shape.dispose();

        BodyDef dividerDef = new BodyDef();
        dividerDef.position.set(20, 15);
        Body dividerBody = world.createBody(dividerDef);
        PolygonShape dividerShape = new PolygonShape();
        dividerShape.setAsBox(0.1f, 15);
        FixtureDef dividerFixtureDef = new FixtureDef();
        dividerFixtureDef.shape = dividerShape;
        dividerFixtureDef.friction = 0f;
        dividerBody.createFixture(dividerFixtureDef);
        dividerShape.dispose();
    }

    private void spawnForPlayer(Player player) {
        player.spawnNewBlock(world);
        activeBlocks.add(player.getCurrentBlock());
    }

    private boolean isFixtureControlled(Fixture fixture) {
        Block b1 = player1.getCurrentBlock();
        Block b2 = player2.getCurrentBlock();
        return (b1 != null && fixture.getBody() == b1.body) ||
            (b2 != null && fixture.getBody() == b2.body);
    }

    private void checkLanding(Contact contact) {
        Fixture fa = contact.getFixtureA();
        Fixture fb = contact.getFixtureB();
        if (!isFixtureControlled(fa) && !isFixtureControlled(fb)) return;

        Vector2 normal = contact.getWorldManifold().getNormal();
        checkPlayerLanding(player1, fa, fb, normal);
        checkPlayerLanding(player2, fa, fb, normal);
    }

    private void checkPlayerLanding(Player player, Fixture fa, Fixture fb, Vector2 normal) {
        Block block = player.getCurrentBlock();
        if (block == null) return;

        boolean isFaPlayer = (fa.getBody() == block.body);
        boolean isFbPlayer = (fb.getBody() == block.body);
        if (!isFaPlayer && !isFbPlayer) return;

        // ── FIX UTAMA ─────────────────────────────────────────────────────────
        // Box2D mendefinisikan WorldManifold.getNormal() mengarah DARI A KE B.
        //
        // Kita ingin tahu: apakah blok berada DI ATAS permukaan kontak?
        // Jika ya, komponen Y dari vektor "menuju blok" harus positif (ke atas).
        //
        // • Blok = fixture A (isFaPlayer):
        //     normal mengarah A→B = dari blok ke island = ke bawah → normal.y < 0
        //     "menuju blok" = kebalikannya = -normal.y → positif ✓
        //
        // • Blok = fixture B (isFbPlayer):
        //     normal mengarah A→B = dari island ke blok = ke atas → normal.y > 0
        //     "menuju blok" = normal.y langsung → positif ✓
        //
        // KODE LAMA (salah): isFaPlayer ? normal.y : -normal.y
        //   → kedua kasus menghasilkan nilai NEGATIF → threshold tidak pernah terpenuhi
        //   → blok tidak pernah settle meski sudah mendarat
        //
        // KODE BARU (benar): isFaPlayer ? -normal.y : normal.y
        //   → kedua kasus menghasilkan nilai POSITIF ≈ 1.0 → threshold terpenuhi ✓
        // ─────────────────────────────────────────────────────────────────────
        float ny = isFaPlayer ? -normal.y : normal.y;

        if (ny > LANDING_NORMAL_THRESHOLD) {
            if (!blocksToSettle.contains(block, true)) {
                blocksToSettle.add(block);
            }
        }
    }

    @Override
    public void render(float delta) {
        if (gameOver) return;

        Gdx.gl.glClearColor(0.1f, 0.4f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        world.step(1 / 60f, STEP_VEL_ITERATIONS, STEP_POS_ITERATIONS);
        processSettleQueue();
        checkOutOfBounds();

        if (gameOver) return;

        updatePlayer(player1, delta);
        updatePlayer(player2, delta);

        viewport.apply();
        debugRenderer.render(world, camera.combined);

        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();
        drawHud();
        game.batch.end();
    }

    private void processSettleQueue() {
        if (blocksToSettle.size == 0) return;
        for (int i = 0; i < blocksToSettle.size; i++) {
            Block block = blocksToSettle.get(i);
            block.setControlled(false);
            if (player1.getCurrentBlock() == block) player1.clearCurrentBlock();
            if (player2.getCurrentBlock() == block) player2.clearCurrentBlock();
        }
        blocksToSettle.clear();
    }

    private void drawHud() {
        hudBuilder.setLength(0);
        hudBuilder.append("P1 Lives: ").append(player1.getLives());
        hudFont.draw(game.batch, hudBuilder, 50, screenHeight - 50);

        hudBuilder.setLength(0);
        hudBuilder.append("P2 Lives: ").append(player2.getLives());
        hudFont.draw(game.batch, hudBuilder, screenWidth - 250, screenHeight - 50);
    }

    private void updatePlayer(Player player, float delta) {
        player.update(delta);
        if (player.canSpawn()) {
            spawnForPlayer(player);
        }
    }

    private void checkOutOfBounds() {
        for (int i = activeBlocks.size - 1; i >= 0; i--) {
            Block block = activeBlocks.get(i);

            if (block.body.getPosition().y < -2f) {
                if (block.ownerId == 1) player1.loseLife();
                else if (block.ownerId == 2) player2.loseLife();

                if (block == player1.getCurrentBlock()) player1.clearCurrentBlock();
                if (block == player2.getCurrentBlock()) player2.clearCurrentBlock();

                destroyAndFreeBlock(block);
                activeBlocks.removeIndex(i);

                if (player1.getLives() <= 0 || player2.getLives() <= 0) {
                    gameOver = true;
                    game.setScreen(new GameOverScreen(game));
                    return;
                }
            } else if (!block.isControlled() && block.body.getPosition().y >= 26.5f) {
                gameOver = true;
                game.setScreen(new GameOverScreen(game));
                return;
            }
        }
    }

    private void destroyAndFreeBlock(Block block) {
        if (block.body != null && world != null) {
            world.destroyBody(block.body);
            block.body = null;
        }
        BlockFactory.getInstance().freeBlock(block);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        screenWidth  = width;
        screenHeight = height;
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
    }

    private void handleInput() {
        handlePlayerInput(player1, Input.Keys.A, Input.Keys.D, Input.Keys.S, Input.Keys.W);
        handlePlayerInput(player2, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.UP);
    }

    private void handlePlayerInput(Player player, int left, int right, int down, int rotate) {
        Block block = player.getCurrentBlock();
        if (block == null || !block.isControlled()) return;

        float currentFallSpeed = Gdx.input.isKeyPressed(down) ? FALL_SPEED_FAST : FALL_SPEED_NORMAL;
        block.body.setLinearVelocity(0, currentFallSpeed);

        Vector2 pos = block.body.getPosition();
        if (Gdx.input.isKeyJustPressed(left)) {
            block.body.setTransform(pos.x - MOVE_STEP, pos.y, block.body.getAngle());
        } else if (Gdx.input.isKeyJustPressed(right)) {
            block.body.setTransform(pos.x + MOVE_STEP, pos.y, block.body.getAngle());
        }
        if (Gdx.input.isKeyJustPressed(rotate)) {
            block.body.setTransform(pos.x, pos.y, block.body.getAngle() + (float) Math.PI / 2);
        }
    }

    @Override
    public void hide() {
        blocksToSettle.clear();

        for (int i = 0; i < activeBlocks.size; i++) {
            destroyAndFreeBlock(activeBlocks.get(i));
        }
        activeBlocks.clear();

        if (world != null) {
            world.dispose();
            world = null;
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
            debugRenderer = null;
        }

        gameOver = false;
    }
}
