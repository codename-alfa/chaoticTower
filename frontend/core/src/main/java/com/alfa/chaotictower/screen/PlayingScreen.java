package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.factory.BlockFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
    private Block currentBlock;
    private float spawnTimer = 0;

    public PlayingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        Box2D.init();
        world = new World(new Vector2(0, -25f), true);
        debugRenderer = new Box2DDebugRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 30, camera);

        createGround();

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

        spawnNewBlock();
    }

    private void createGround() {
        BodyDef groundDef = new BodyDef();
        groundDef.position.set(10, 1);
        Body groundBody = world.createBody(groundDef);
        PolygonShape groundShape = new PolygonShape();
        groundShape.setAsBox(10, 1);
        groundBody.createFixture(groundShape, 0);
        groundShape.dispose();
    }

    private void spawnNewBlock() {
        currentBlock = BlockFactory.getInstance().spawnBlock(world, 10, 28);
        activeBlocks.add(currentBlock);
    }

    private void checkLanding(Fixture fixture) {
        if (currentBlock != null && fixture.getBody() == currentBlock.body) {
            currentBlock.setControlled(false);
            currentBlock = null;
            spawnTimer = 0.5f;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.4f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput(delta);
        world.step(1 / 60f, 6, 2);

        if (currentBlock == null) {
            spawnTimer -= delta;
            if (spawnTimer <= 0) spawnNewBlock();
        }

        viewport.apply();
        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void handleInput(float delta) {
        if (currentBlock == null || !currentBlock.isControlled()) return;

        Vector2 pos = currentBlock.body.getPosition();
        float fallSpeed = -5f;

        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            fallSpeed = -20f;
        }

        currentBlock.body.setLinearVelocity(0, fallSpeed);

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            currentBlock.body.setTransform(pos.x - 1.0f, pos.y, currentBlock.body.getAngle());
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            currentBlock.body.setTransform(pos.x + 1.0f, pos.y, currentBlock.body.getAngle());
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            currentBlock.body.setTransform(pos.x, pos.y, currentBlock.body.getAngle() + (float) Math.PI / 2);
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
