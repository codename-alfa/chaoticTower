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
    private final Array<Block> activeBlocks = new Array<>();

    private Player player1;
    private Player player2;

    // HUD Variables
    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;

    public PlayingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Box2D.init();
        world = new World(new Vector2(0, -25f), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(40, 30, camera);

        // Setup HUD Camera & Font
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudFont = GameAssetManager.getInstance().manager.get(GameAssetManager.FONT_HUD, BitmapFont.class);

        player1 = new Player(1, 10, 28);
        player2 = new Player(2, 30, 28);

        createEnvironment();

        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                Fixture fa = contact.getFixtureA();
                Fixture fb = contact.getFixtureB();
                checkLanding(fa);
                checkLanding(fb);
            }
            @Override public void endContact(Contact contact) {}
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });

        spawnForPlayer(player1);
        spawnForPlayer(player2);
    }

    private void createEnvironment() {
        BodyDef groundDef = new BodyDef();
        groundDef.position.set(20, 1);
        Body groundBody = world.createBody(groundDef);
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(20, 1);
        groundBody.createFixture(groundShape, 0);
        groundShape.dispose();

        BodyDef dividerDef = new BodyDef();
        dividerDef.position.set(20, 15);
        Body dividerBody = world.createBody(dividerDef);
        PolygonShape dividerShape = new PolygonShape();
        dividerShape.setAsBox(0.2f, 15);
        dividerBody.createFixture(dividerShape, 0);
        dividerShape.dispose();
    }

    private void spawnForPlayer(Player player) {
        player.spawnNewBlock(world);
        activeBlocks.add(player.getCurrentBlock());
    }

    private void checkLanding(Fixture fixture) {
        checkPlayerLanding(player1, fixture);
        checkPlayerLanding(player2, fixture);
    }

    private void checkPlayerLanding(Player player, Fixture fixture) {
        Block block = player.getCurrentBlock();
        if (block != null && fixture.getBody() == block.body) {
            block.setControlled(false);
            player.clearCurrentBlock();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.4f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        world.step(1 / 60f, 10, 8); // Iterasi fisika tinggi agar stabil

        checkOutOfBounds();

        updatePlayer(player1, delta);
        updatePlayer(player2, delta);

        // 1. Render Dunia Box2D
        viewport.apply();
        debugRenderer.render(world, camera.combined);

        // 2. Render HUD UI (Teks Nyawa)
        hudCamera.update();
        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();
        hudFont.draw(game.batch, "Player 1: " + player1.getLives(), 50, Gdx.graphics.getHeight() - 50);
        hudFont.draw(game.batch, "Player 2: " + player2.getLives(), Gdx.graphics.getWidth() - 300, Gdx.graphics.getHeight() - 50);
        game.batch.end();
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

                BlockFactory.getInstance().freeBlock(block);
                activeBlocks.removeIndex(i);

                if (player1.getLives() <= 0 || player2.getLives() <= 0) {
                    game.setScreen(new GameOverScreen(game));
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hudCamera.setToOrtho(false, width, height); // Update HUD Camera
    }

    private void handleInput() {
        handlePlayerInput(player1, Input.Keys.A, Input.Keys.D, Input.Keys.S, Input.Keys.W);
        handlePlayerInput(player2, Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.UP);
    }

    private void handlePlayerInput(Player player, int left, int right, int down, int rotate) {
        Block block = player.getCurrentBlock();
        if (block == null || !block.isControlled()) return;

        Vector2 pos = block.body.getPosition();

        float normalFallSpeed = -2.0f;
        float fastFallSpeed = -10.0f;
        float currentFallSpeed = normalFallSpeed;

        if (Gdx.input.isKeyPressed(down)) {
            currentFallSpeed = fastFallSpeed;
        }

        block.body.setLinearVelocity(0, currentFallSpeed);

        if (Gdx.input.isKeyJustPressed(left)) {
            block.body.setTransform(pos.x - 1.0f, pos.y, block.body.getAngle());
        } else if (Gdx.input.isKeyJustPressed(right)) {
            block.body.setTransform(pos.x + 1.0f, pos.y, block.body.getAngle());
        }

        if (Gdx.input.isKeyJustPressed(rotate)) {
            block.body.setTransform(pos.x, pos.y, block.body.getAngle() + (float) Math.PI / 2);
        }
    }

    @Override
    public void hide() {
        for (Block block : activeBlocks) BlockFactory.getInstance().freeBlock(block);
        activeBlocks.clear();
        world.dispose();
        debugRenderer.dispose();
    }
}
