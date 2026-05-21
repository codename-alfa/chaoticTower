package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.GameAssetManager;
import com.alfa.chaotictower.command.InputHandler;
import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.entity.Player;
import com.alfa.chaotictower.factory.BlockFactory;
import com.alfa.chaotictower.strategy.GameModeStrategy;
import com.alfa.chaotictower.strategy.RaceStrategy;
import com.alfa.chaotictower.strategy.TimeAttackStrategy;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class PlayingScreen extends ScreenAdapter {

    private final Main game;
    private final int playerCount;
    private final GameModeStrategy strategy;

    private World world;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private final Array<Block> activeBlocks   = new Array<>();
    private final Array<Block> blocksToSettle = new Array<>();

    private Player[] players;
    private InputHandler[] inputHandlers;

    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;
    private BitmapFont smallFont;
    private BitmapFont menuFont;
    private final StringBuilder hudBuilder = new StringBuilder(64);

    private int screenWidth;
    private int screenHeight;
    private boolean gameOver = false;
    private boolean paused = false;
    private Long loggedInPlayerId;
    private double elapsedTime = 0.0;
    private BitmapFont titleFont;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    // ─── Physics constants ──────────────────────────────────────────
    private static final float WORLD_GRAVITY           = -15f;
    private static final int   STEP_VEL_ITERATIONS     = 10;
    private static final int   STEP_POS_ITERATIONS     = 8;
    private static final float LANDING_NORMAL_THRESHOLD = 0.4f;
    private static final float PEDESTAL_Y    = 2f;
    private static final float PEDESTAL_HALF = 1f;

    // ─── Render constants ───────────────────────────────────────────
    private static final float RENDER_TILE = Block.TILE_SIZE;
    private static final float RENDER_HALF = RENDER_TILE / 2f;

    private static final Color CARD_BG = new Color(0.10f, 0.10f, 0.20f, 0.85f);
    private static final Color CARD_SEL = new Color(0.18f, 0.15f, 0.35f, 0.95f);
    private static final Color ACCENT = new Color(0.45f, 0.35f, 0.85f, 1);
    
    private int pauseSelectedIndex = 0;
    private static final String[] PAUSE_OPTS = {"Resume Game", "Quit to Menu"};
    private static final String[] PAUSE_DESC = {"Continue playing", "Return to mode selection"};

    private static final Color[] BLOCK_COLORS = {
        new Color(0.95f, 0.90f, 0.20f, 1),  // O — Yellow
        new Color(0.20f, 0.90f, 0.95f, 1),  // I — Cyan
        new Color(0.70f, 0.25f, 0.95f, 1),  // T — Purple
        new Color(0.95f, 0.60f, 0.15f, 1),  // L — Orange
        new Color(0.20f, 0.35f, 0.95f, 1),  // J — Blue
        new Color(0.25f, 0.90f, 0.30f, 1),  // S — Green
        new Color(0.95f, 0.20f, 0.25f, 1),  // Z — Red
    };

    private static final Color BG_BOTTOM   = new Color(0.03f, 0.03f, 0.06f, 1);
    private static final Color BG_TOP      = new Color(0.06f, 0.06f, 0.16f, 1);
    private static final Color PEDESTAL_C  = new Color(0.30f, 0.32f, 0.38f, 1);
    private static final Color PEDESTAL_T  = new Color(0.42f, 0.44f, 0.50f, 1);
    private static final Color WALL_COLOR  = new Color(0.18f, 0.20f, 0.28f, 1);
    private static final Color DIVIDER_C   = new Color(0.15f, 0.20f, 0.35f, 0.8f);
    private static final Color GRID_COLOR  = new Color(1f, 1f, 1f, 0.03f);
    private static final Color OUTLINE_C   = new Color(0f, 0f, 0f, 0.6f);

    private final Color tempColor = new Color();

    // ─── Constructors ───────────────────────────────────────────────
    public PlayingScreen(Main game) {
        this(game, 2, new com.alfa.chaotictower.strategy.SurvivalStrategy());
    }

    public PlayingScreen(Main game, int playerCount, GameModeStrategy strategy) {
        this.game = game;
        this.playerCount = Math.max(1, Math.min(2, playerCount));
        this.strategy = strategy;
    }

    // ─── Lifecycle ──────────────────────────────────────────────────
    @Override
    public void show() {
        Box2D.init();
        world = new World(new Vector2(0, WORLD_GRAVITY), true);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        camera = new OrthographicCamera();

        float vw = (playerCount == 1) ? 20 : 40;
        viewport = new FitViewport(vw, 30, camera);

        screenWidth  = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

        GameAssetManager assets = GameAssetManager.getInstance();
        hudFont   = assets.getFont(GameAssetManager.FONT_HUD);
        smallFont = assets.getFont(GameAssetManager.FONT_SMALL);
        menuFont  = assets.getFont(GameAssetManager.FONT_MENU);
        titleFont = assets.getFont(GameAssetManager.FONT_TITLE);

        int lives = strategy.getInitialLives();
        if (playerCount == 1) {
            players = new Player[]{new Player(1, 10, 28, lives)};
            inputHandlers = new InputHandler[]{
                new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.S, Input.Keys.W)
            };
        } else {
            players = new Player[]{
                new Player(1, 10, 28, lives),
                new Player(2, 30, 28, lives)
            };
            inputHandlers = new InputHandler[]{
                new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.S, Input.Keys.W),
                new InputHandler(Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.UP)
            };
        }

        createEnvironment();
        setupContactListener();

        for (Player p : players) {
            spawnForPlayer(p);
        }
    }

    // ─── Contact Listener ───────────────────────────────────────────
    private void setupContactListener() {
        world.setContactListener(new ContactListener() {
            @Override public void beginContact(Contact contact) { checkLanding(contact); }
            @Override public void endContact(Contact contact) {}
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                Fixture fa = contact.getFixtureA();
                Fixture fb = contact.getFixtureB();
                if (isFixtureControlled(fa) || isFixtureControlled(fb)) {
                    Vector2 normal = contact.getWorldManifold().getNormal();
                    contact.setFriction(Math.abs(normal.y) < 0.5f ? 0f : 1.0f);
                }
            }
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
    }

    // ─── Environment ────────────────────────────────────────────────
    private void createEnvironment() {
        if (playerCount == 1) {
            createStaticBox(10, PEDESTAL_Y, 2.5f, PEDESTAL_HALF, 1.0f);
            createStaticBox(2.5f, 15, 0.1f, 15, 0f);
            createStaticBox(17.5f, 15, 0.1f, 15, 0f);
        } else {
            createStaticBox(10, PEDESTAL_Y, 2.5f, PEDESTAL_HALF, 1.0f);
            createStaticBox(30, PEDESTAL_Y, 2.5f, PEDESTAL_HALF, 1.0f);

            BodyDef dd = new BodyDef(); dd.position.set(20, 15);
            Body db = world.createBody(dd);
            PolygonShape ds = new PolygonShape(); ds.setAsBox(0.1f, 15);
            FixtureDef df = new FixtureDef(); df.shape = ds; df.friction = 0f;
            db.createFixture(df); ds.dispose();

            PolygonShape ws = new PolygonShape(); ws.setAsBox(0.1f, 15);
            createStaticBoxShape(2.5f, 15, ws);
            createStaticBoxShape(17.5f, 15, ws);
            createStaticBoxShape(22.5f, 15, ws);
            createStaticBoxShape(37.5f, 15, ws);
            ws.dispose();
        }
    }

    private void createStaticBox(float x, float y, float hw, float hh, float friction) {
        BodyDef bd = new BodyDef(); bd.position.set(x, y);
        Body b = world.createBody(bd);
        PolygonShape s = new PolygonShape(); s.setAsBox(hw, hh);
        FixtureDef fd = new FixtureDef(); fd.shape = s; fd.friction = friction;
        b.createFixture(fd); s.dispose();
    }

    private void createStaticBoxShape(float x, float y, PolygonShape shape) {
        BodyDef bd = new BodyDef(); bd.position.set(x, y);
        world.createBody(bd).createFixture(shape, 0f);
    }

    private void spawnForPlayer(Player player) {
        player.spawnNewBlock(world);
        activeBlocks.add(player.getCurrentBlock());
    }

    // ─── Collision helpers ──────────────────────────────────────────
    private boolean isFixtureControlled(Fixture f) {
        for (Player p : players) {
            Block b = p.getCurrentBlock();
            if (b != null && f.getBody() == b.body) return true;
        }
        return false;
    }

    private void checkLanding(Contact contact) {
        Fixture fa = contact.getFixtureA(), fb = contact.getFixtureB();
        if (!isFixtureControlled(fa) && !isFixtureControlled(fb)) return;
        Vector2 normal = contact.getWorldManifold().getNormal();
        for (Player p : players) checkPlayerLanding(p, fa, fb, normal);
    }

    private void checkPlayerLanding(Player player, Fixture fa, Fixture fb, Vector2 normal) {
        Block block = player.getCurrentBlock();
        if (block == null) return;
        boolean isFa = (fa.getBody() == block.body);
        boolean isFb = (fb.getBody() == block.body);
        if (!isFa && !isFb) return;
        float ny = isFa ? -normal.y : normal.y;
        if (ny > LANDING_NORMAL_THRESHOLD && !blocksToSettle.contains(block, true)) {
            blocksToSettle.add(block);
        }
    }

    // ─── Main render loop ───────────────────────────────────────────
    @Override
    public void render(float delta) {
        if (gameOver) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            pauseSelectedIndex = 0;
        }

        if (!paused) {
            elapsedTime += delta;

            for (int i = 0; i < players.length; i++) inputHandlers[i].handleInput(players[i]);

            world.step(1 / 60f, STEP_VEL_ITERATIONS, STEP_POS_ITERATIONS);
            processSettleQueue();
            updateMaxHeights();
            checkOutOfBounds();
            if (gameOver) return;

            float[] mh = getMaxHeightsArray();
            if (strategy.checkWinCondition(players, elapsedTime, mh) ||
                strategy.checkLoseCondition(players, elapsedTime)) {
                triggerGameOver();
                return;
            }

            for (Player p : players) updatePlayer(p, delta);
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        shapeRenderer.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawBackground();
        drawGrid();
        drawEnvironment();
        drawBlocks();
        drawTargetLine();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();
        drawHud();
        game.batch.end();

        if (paused) {
            drawPauseOverlay();

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                pauseSelectedIndex = Math.max(0, pauseSelectedIndex - 1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                pauseSelectedIndex = Math.min(1, pauseSelectedIndex + 1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (pauseSelectedIndex == 0) {
                    paused = false;
                } else {
                    paused = false;
                    gameOver = true;
                    game.setScreen(new ModeSelectScreen(game, loggedInPlayerId));
                }
            }
        }
    }

    // ─── Pause overlay ──────────────────────────────────────────────
    private void drawPauseOverlay() {
        float w = screenWidth, h = screenHeight;
        float cx = w / 2f;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer.rect(0, 0, w, h);

        float cardW = 360, cardH = 90, gap = 20;
        float totalH = 2 * cardH + gap;
        float startY = h / 2f + totalH / 2f - 40;

        for (int i = 0; i < 2; i++) {
            float y = startY - i * (cardH + gap);
            boolean sel = (i == pauseSelectedIndex);
            
            shapeRenderer.setColor(sel ? CARD_SEL : CARD_BG);
            shapeRenderer.rect(cx - cardW / 2, y, cardW, cardH);
            
            if (sel) {
                shapeRenderer.setColor(ACCENT);
                shapeRenderer.rect(cx - cardW / 2, y, 5, cardH);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();

        glyphLayout.setText(titleFont, "PAUSED");
        titleFont.draw(game.batch, "PAUSED", cx - glyphLayout.width / 2, startY + cardH + 80);

        for (int i = 0; i < 2; i++) {
            float y = startY - i * (cardH + gap);
            boolean sel = (i == pauseSelectedIndex);
            
            if (sel) menuFont.setColor(Color.WHITE);
            else menuFont.setColor(0.6f, 0.6f, 0.7f, 1);
            menuFont.draw(game.batch, PAUSE_OPTS[i], cx - 140, y + cardH - 20);

            smallFont.setColor(0.5f, 0.5f, 0.6f, 1);
            smallFont.draw(game.batch, PAUSE_DESC[i], cx - 140, y + 30);
        }

        menuFont.setColor(Color.WHITE);
        smallFont.setColor(Color.WHITE);

        glyphLayout.setText(smallFont, "[ENTER] Select     [ESC] Resume");
        smallFont.draw(game.batch, "[ENTER] Select     [ESC] Resume", cx - glyphLayout.width / 2, 50);

        game.batch.end();
    }

    // ─── Background gradient ────────────────────────────────────────
    private void drawBackground() {
        float vw = viewport.getWorldWidth();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, vw, 30, BG_BOTTOM, BG_BOTTOM, BG_TOP, BG_TOP);
        shapeRenderer.end();
    }

    // ─── Subtle grid ────────────────────────────────────────────────
    private void drawGrid() {
        float vw = viewport.getWorldWidth();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(GRID_COLOR);
        for (float x = 0; x <= vw; x += 1f) shapeRenderer.line(x, 0, x, 30);
        for (float y = 0; y <= 30; y += 1f) shapeRenderer.line(0, y, vw, y);
        shapeRenderer.end();
    }

    // ─── Pedestal, walls, divider ───────────────────────────────────
    private void drawEnvironment() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Pedestals with gradient (darker bottom, lighter top)
        drawPedestal(10);
        if (playerCount == 2) drawPedestal(30);

        // Walls
        shapeRenderer.setColor(WALL_COLOR);
        if (playerCount == 1) {
            shapeRenderer.rect(2.5f - 0.15f, 0, 0.3f, 30);
            shapeRenderer.rect(17.5f - 0.15f, 0, 0.3f, 30);
        } else {
            shapeRenderer.rect(2.5f - 0.15f, 0, 0.3f, 30);
            shapeRenderer.rect(17.5f - 0.15f, 0, 0.3f, 30);
            shapeRenderer.rect(22.5f - 0.15f, 0, 0.3f, 30);
            shapeRenderer.rect(37.5f - 0.15f, 0, 0.3f, 30);
        }

        // Divider (2P only)
        if (playerCount == 2) {
            shapeRenderer.setColor(DIVIDER_C);
            shapeRenderer.rect(20f - 0.12f, 0, 0.24f, 30);
        }

        shapeRenderer.end();
    }

    private void drawPedestal(float cx) {
        float x = cx - 2.5f, y = PEDESTAL_Y - PEDESTAL_HALF;
        float w = 5f, h = 2f;
        // Bottom half darker, top half lighter
        shapeRenderer.rect(x, y, w, h,
            PEDESTAL_C, PEDESTAL_C, PEDESTAL_T, PEDESTAL_T);
        // Top edge highlight
        tempColor.set(1, 1, 1, 0.12f);
        shapeRenderer.setColor(tempColor);
        shapeRenderer.rect(x, y + h - 0.08f, w, 0.08f);
    }

    // ─── Block rendering ────────────────────────────────────────────
    private void drawBlocks() {
        // Pass 1: filled tiles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < activeBlocks.size; i++) {
            Block block = activeBlocks.get(i);
            if (block.body == null) continue;
            drawBlockFilled(block);
        }
        shapeRenderer.end();

        // Pass 2: outlines
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(OUTLINE_C);
        for (int i = 0; i < activeBlocks.size; i++) {
            Block block = activeBlocks.get(i);
            if (block.body == null) continue;
            drawBlockOutline(block);
        }
        shapeRenderer.end();
    }

    private void drawBlockFilled(Block block) {
        Color base = BLOCK_COLORS[block.getTetrominoType()];
        Vector2 pos = block.body.getPosition();
        float angle = block.body.getAngle();
        float cos = MathUtils.cos(angle), sin = MathUtils.sin(angle);
        float deg = angle * MathUtils.radiansToDegrees;

        // Controlled block pulses slightly
        if (block.isControlled()) {
            float pulse = 0.12f * (float) Math.sin(elapsedTime * 6.0);
            tempColor.set(
                Math.min(1, base.r + pulse),
                Math.min(1, base.g + pulse),
                Math.min(1, base.b + pulse), 1);
            shapeRenderer.setColor(tempColor);
        } else {
            shapeRenderer.setColor(base);
        }

        for (Vector2 local : block.getLocalTilePositions()) {
            float wx = pos.x + local.x * cos - local.y * sin;
            float wy = pos.y + local.x * sin + local.y * cos;
            shapeRenderer.rect(
                wx - RENDER_HALF, wy - RENDER_HALF,
                RENDER_HALF, RENDER_HALF,
                RENDER_TILE, RENDER_TILE,
                1, 1, deg);
        }

        // Inner highlight (lighter strip at top of each tile)
        tempColor.set(1, 1, 1, 0.15f);
        shapeRenderer.setColor(tempColor);
        float highlightH = RENDER_TILE * 0.18f;
        for (Vector2 local : block.getLocalTilePositions()) {
            float wx = pos.x + local.x * cos - local.y * sin;
            float wy = pos.y + local.x * sin + local.y * cos;
            shapeRenderer.rect(
                wx - RENDER_HALF, wy + RENDER_HALF - highlightH,
                RENDER_HALF, RENDER_HALF,
                RENDER_TILE, highlightH,
                1, 1, deg);
        }
    }

    private void drawBlockOutline(Block block) {
        Vector2 pos = block.body.getPosition();
        float angle = block.body.getAngle();
        float cos = MathUtils.cos(angle), sin = MathUtils.sin(angle);
        float deg = angle * MathUtils.radiansToDegrees;

        for (Vector2 local : block.getLocalTilePositions()) {
            float wx = pos.x + local.x * cos - local.y * sin;
            float wy = pos.y + local.x * sin + local.y * cos;
            shapeRenderer.rect(
                wx - RENDER_HALF, wy - RENDER_HALF,
                RENDER_HALF, RENDER_HALF,
                RENDER_TILE, RENDER_TILE,
                1, 1, deg);
        }
    }

    // ─── Target line ────────────────────────────────────────────────
    private void drawTargetLine() {
        float th = -1;
        if (strategy instanceof RaceStrategy) th = ((RaceStrategy) strategy).getTargetHeight();
        else if (strategy instanceof TimeAttackStrategy) th = ((TimeAttackStrategy) strategy).getTargetHeight();
        if (th <= 0) return;

        float worldY = PEDESTAL_Y + PEDESTAL_HALF + th;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Dashed line effect
        float dashAlpha = 0.45f + 0.15f * (float) Math.sin(elapsedTime * 3.0);
        tempColor.set(1f, 0.85f, 0.1f, dashAlpha);
        shapeRenderer.setColor(tempColor);
        if (playerCount == 1) {
            shapeRenderer.rect(2.5f, worldY - 0.04f, 15f, 0.08f);
        } else {
            shapeRenderer.rect(2.5f, worldY - 0.04f, 15f, 0.08f);
            shapeRenderer.rect(22.5f, worldY - 0.04f, 15f, 0.08f);
        }
        shapeRenderer.end();
    }

    // ─── HUD ────────────────────────────────────────────────────────
    private void drawHud() {
        float cx = screenWidth / 2f;

        // Mode name
        menuFont.draw(game.batch, strategy.getModeName(), cx - 80, screenHeight - 20);

        // Timer
        hudBuilder.setLength(0);
        if (strategy instanceof TimeAttackStrategy) {
            double remaining = Math.max(0, ((TimeAttackStrategy) strategy).getTimeLimit() - elapsedTime);
            hudBuilder.append(String.format("%.1f", remaining)).append("s");
        } else {
            hudBuilder.append(String.format("%.1f", elapsedTime)).append("s");
        }
        smallFont.draw(game.batch, hudBuilder, cx - 30, screenHeight - 60);

        // Player panels
        if (playerCount == 1) {
            drawPlayerHud(players[0], 40, screenHeight - 20);
        } else {
            drawPlayerHud(players[0], 40, screenHeight - 20);
            drawPlayerHud(players[1], screenWidth - 320, screenHeight - 20);
        }
    }

    private void drawPlayerHud(Player p, float x, float y) {
        // Lives as hearts
        hudBuilder.setLength(0);
        hudBuilder.append("P").append(p.getId()).append("  ");
        for (int i = 0; i < p.getLives(); i++) hudBuilder.append("\u2665 ");
        hudFont.draw(game.batch, hudBuilder, x, y);

        hudBuilder.setLength(0);
        hudBuilder.append("Score: ").append(p.getScore());
        smallFont.draw(game.batch, hudBuilder, x, y - 38);

        hudBuilder.setLength(0);
        hudBuilder.append("Height: ").append(String.format("%.1f", p.getMaxHeight())).append("m");
        smallFont.draw(game.batch, hudBuilder, x, y - 66);
    }

    // ─── Settle queue ───────────────────────────────────────────────
    private void processSettleQueue() {
        if (blocksToSettle.size == 0) return;
        for (int i = 0; i < blocksToSettle.size; i++) {
            Block block = blocksToSettle.get(i);
            block.setControlled(false);
            for (Player p : players) {
                if (p.getCurrentBlock() == block) { p.addScore(10); p.clearCurrentBlock(); }
            }
        }
        blocksToSettle.clear();
    }

    private void updateMaxHeights() {
        for (int i = 0; i < activeBlocks.size; i++) {
            Block block = activeBlocks.get(i);
            if (!block.isControlled() && block.body != null) {
                float rel = block.body.getPosition().y - (PEDESTAL_Y + PEDESTAL_HALF);
                for (Player p : players) {
                    if (block.ownerId == p.getId()) p.updateMaxHeight(rel);
                }
            }
        }
    }

    private float[] getMaxHeightsArray() {
        float[] arr = new float[players.length];
        for (int i = 0; i < players.length; i++) arr[i] = players[i].getMaxHeight();
        return arr;
    }

    private void updatePlayer(Player p, float delta) {
        p.update(delta);
        if (p.canSpawn()) spawnForPlayer(p);
    }

    // ─── Out-of-bounds ──────────────────────────────────────────────
    private void checkOutOfBounds() {
        for (int i = activeBlocks.size - 1; i >= 0; i--) {
            Block block = activeBlocks.get(i);
            if (block.body.getPosition().y < -2f) {
                for (Player p : players) {
                    if (block.ownerId == p.getId()) p.loseLife();
                    if (block == p.getCurrentBlock()) p.clearCurrentBlock();
                }
                destroyAndFreeBlock(block);
                activeBlocks.removeIndex(i);
                if (strategy.checkLoseCondition(players, elapsedTime)) { triggerGameOver(); return; }
            }
        }
    }

    // ─── Game over ──────────────────────────────────────────────────
    private void triggerGameOver() {
        if (gameOver) return;
        gameOver = true;
        float[] mh = getMaxHeightsArray();
        String result = strategy.getResultText(players, elapsedTime, mh);
        int bestScore = 0; float bestH = 0;
        for (Player p : players) {
            if (p.getScore() > bestScore) bestScore = p.getScore();
            if (p.getMaxHeight() > bestH) bestH = p.getMaxHeight();
        }
        game.setScreen(new GameOverScreen(game, loggedInPlayerId,
            bestScore, elapsedTime, bestH, strategy.getBackendModeKey(), result));
    }

    private void destroyAndFreeBlock(Block block) {
        if (block.body != null && world != null) { world.destroyBody(block.body); block.body = null; }
        BlockFactory.getInstance().freeBlock(block);
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        screenWidth = w; screenHeight = h;
        hudCamera.setToOrtho(false, w, h); hudCamera.update();
    }

    @Override
    public void hide() {
        blocksToSettle.clear();
        for (int i = 0; i < activeBlocks.size; i++) destroyAndFreeBlock(activeBlocks.get(i));
        activeBlocks.clear();
        if (world != null) { world.dispose(); world = null; }
        if (shapeRenderer != null) { shapeRenderer.dispose(); shapeRenderer = null; }
    }

    public void setPlayerId(Long id) { this.loggedInPlayerId = id; }
    public Long getPlayerId() { return loggedInPlayerId; }
}
