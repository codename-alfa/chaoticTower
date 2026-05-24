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
import com.badlogic.gdx.audio.Sound;
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
import com.alfa.chaotictower.strategy.PuzzleStrategy;
import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellManager;
import com.alfa.chaotictower.magic.light.CementSpell;
import com.alfa.chaotictower.magic.light.IvySpell;
import com.alfa.chaotictower.magic.light.LightningSpell;
import com.alfa.chaotictower.magic.dark.FrostSpell;
import com.alfa.chaotictower.magic.dark.WeightSpell;
import com.alfa.chaotictower.magic.dark.SpeedUpSpell;

public class PlayingScreen extends ScreenAdapter {

    private final Main game;
    private final int playerCount;
    private final GameModeStrategy strategy;

    private World world;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private final Array<Block> activeBlocks = new Array<>();
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

    
    private static final float COUNTDOWN_DURATION = 3.0f;
    private float countdownTimer = COUNTDOWN_DURATION;
    private boolean countdownDone = false;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    
    private SpellManager spellManager;
    private Sound spellCastSound;
    private static final int[] SPELL_CAST_KEYS = { Input.Keys.F, Input.Keys.NUMPAD_0 };

    
    private static final float CAMERA_LERP_SPEED = 2.5f;
    private static final float MIN_CAMERA_Y = 15f; 
    private boolean transitioning = false;

    
    private static final float WORLD_GRAVITY = -6.0f;
    private static final int STEP_VEL_ITERATIONS = 10;
    private static final int STEP_POS_ITERATIONS = 8;
    private static final float LANDING_NORMAL_THRESHOLD = 0.4f;
    private static final float PEDESTAL_Y = 2f;
    private static final float PEDESTAL_HALF = 1f;

    
    private static final float RENDER_TILE = Block.TILE_SIZE;
    private static final float RENDER_HALF = RENDER_TILE / 2f;

    private static final Color CARD_BG = new Color(0.10f, 0.10f, 0.20f, 0.85f);
    private static final Color CARD_SEL = new Color(0.18f, 0.15f, 0.35f, 0.95f);
    private static final Color ACCENT = new Color(0.45f, 0.35f, 0.85f, 1);

    private int pauseSelectedIndex = 0;
    private static final String[] PAUSE_OPTS = { "Resume", "Menu", "Restart" };
    private static final String[] PAUSE_DESC = { "", "", "" };

    private static final Color[] BLOCK_COLORS = {
            new Color(0.95f, 0.90f, 0.20f, 1), 
            new Color(0.20f, 0.90f, 0.95f, 1), 
            new Color(0.70f, 0.25f, 0.95f, 1), 
            new Color(0.95f, 0.60f, 0.15f, 1), 
            new Color(0.20f, 0.35f, 0.95f, 1), 
            new Color(0.25f, 0.90f, 0.30f, 1), 
            new Color(0.95f, 0.20f, 0.25f, 1), 
    };

    private static final Color BG_BOTTOM = new Color(0.03f, 0.03f, 0.06f, 1);
    private static final Color BG_TOP = new Color(0.06f, 0.06f, 0.16f, 1);
    private static final Color PEDESTAL_C = new Color(0.30f, 0.32f, 0.38f, 1);
    private static final Color PEDESTAL_T = new Color(0.42f, 0.44f, 0.50f, 1);
    private static final Color WALL_COLOR = new Color(0.18f, 0.20f, 0.28f, 1);
    private static final Color DIVIDER_C = new Color(0.15f, 0.20f, 0.35f, 0.8f);
    private static final Color GRID_COLOR = new Color(1f, 1f, 1f, 0.03f);
    private static final Color OUTLINE_C = new Color(0f, 0f, 0f, 0.6f);

    private final Color tempColor = new Color();

    
    public PlayingScreen(Main game) {
        this(game, 2, new com.alfa.chaotictower.strategy.SurvivalStrategy());
    }

    public PlayingScreen(Main game, int playerCount, GameModeStrategy strategy) {
        this.game = game;
        this.playerCount = Math.max(1, Math.min(2, playerCount));
        this.strategy = strategy;
    }

    
    @Override
    public void show() {
        Box2D.init();
        world = new World(new Vector2(0, WORLD_GRAVITY), true);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
        camera = new OrthographicCamera();

        float vw = (playerCount == 1) ? 20 : 40;
        viewport = new FitViewport(vw, 30, camera);

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

        GameAssetManager assets = GameAssetManager.getInstance();
        hudFont = assets.getFont(GameAssetManager.FONT_HUD);
        smallFont = assets.getFont(GameAssetManager.FONT_SMALL);
        menuFont = assets.getFont(GameAssetManager.FONT_MENU);
        titleFont = assets.getFont(GameAssetManager.FONT_TITLE);

        int lives = strategy.getInitialLives();
        if (playerCount == 1) {
            players = new Player[] { new Player(1, 10, 28, lives) };
            inputHandlers = new InputHandler[] {
                    new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.S, Input.Keys.W)
            };
        } else {
            players = new Player[] {
                    new Player(1, 10, 28, lives),
                    new Player(2, 30, 28, lives)
            };
            inputHandlers = new InputHandler[] {
                    new InputHandler(Input.Keys.A, Input.Keys.D, Input.Keys.S, Input.Keys.W),
                    new InputHandler(Input.Keys.LEFT, Input.Keys.RIGHT, Input.Keys.DOWN, Input.Keys.UP)
            };
        }

        createEnvironment();
        setupContactListener();

        
        BlockFactory.getInstance().resetBags();

        
        spellManager = new SpellManager(playerCount);
        spellManager.registerLightSpell(new CementSpell());
        spellManager.registerLightSpell(new IvySpell());
        spellManager.registerLightSpell(new LightningSpell());
        spellManager.registerDarkSpell(new FrostSpell());
        spellManager.registerDarkSpell(new WeightSpell());
        spellManager.registerDarkSpell(new SpeedUpSpell());

        try {
            spellCastSound = Gdx.audio.newSound(Gdx.files.internal("Re_Zero.mp3"));
        } catch (Exception e) {
            Gdx.app.error("PlayingScreen", "Could not load magic SFX 'Re_Zero.mp3': " + e.getMessage());
        }

        
        countdownTimer = COUNTDOWN_DURATION;
        countdownDone = false;
    }

    
    private void setupContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                checkLanding(contact);
            }

            @Override
            public void endContact(Contact contact) {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                Fixture fa = contact.getFixtureA();
                Fixture fb = contact.getFixtureB();
                Block controlled = getControlledBlock(fa);
                if (controlled == null)
                    controlled = getControlledBlock(fb);

                if (controlled != null) {
                    if (controlled.isFrosted()) {
                        contact.setFriction(0.02f);
                    } else {
                        Vector2 normal = contact.getWorldManifold().getNormal();
                        contact.setFriction(Math.abs(normal.y) < 0.5f ? 0f : 1.0f);
                    }
                }
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    
    private void createEnvironment() {
        if (playerCount == 1) {
            createStaticBox(10, PEDESTAL_Y, 2.5f, PEDESTAL_HALF, 1.0f);
            createStaticBox(2.5f, 15, 0.1f, 15, 0f);
            createStaticBox(17.5f, 15, 0.1f, 15, 0f);
        } else {
            createStaticBox(10, PEDESTAL_Y, 2.5f, PEDESTAL_HALF, 1.0f);
            createStaticBox(30, PEDESTAL_Y, 2.5f, PEDESTAL_HALF, 1.0f);

            BodyDef dd = new BodyDef();
            dd.position.set(20, 15);
            Body db = world.createBody(dd);
            PolygonShape ds = new PolygonShape();
            ds.setAsBox(0.1f, 15);
            FixtureDef df = new FixtureDef();
            df.shape = ds;
            df.friction = 0f;
            db.createFixture(df);
            ds.dispose();

            PolygonShape ws = new PolygonShape();
            ws.setAsBox(0.1f, 15);
            createStaticBoxShape(2.5f, 15, ws);
            createStaticBoxShape(17.5f, 15, ws);
            createStaticBoxShape(22.5f, 15, ws);
            createStaticBoxShape(37.5f, 15, ws);
            ws.dispose();
        }
    }

    private void createStaticBox(float x, float y, float hw, float hh, float friction) {
        BodyDef bd = new BodyDef();
        bd.position.set(x, y);
        Body b = world.createBody(bd);
        PolygonShape s = new PolygonShape();
        s.setAsBox(hw, hh);
        FixtureDef fd = new FixtureDef();
        fd.shape = s;
        fd.friction = friction;
        b.createFixture(fd);
        s.dispose();
    }

    private void createStaticBoxShape(float x, float y, PolygonShape shape) {
        BodyDef bd = new BodyDef();
        bd.position.set(x, y);
        world.createBody(bd).createFixture(shape, 0f);
    }

    private void spawnForPlayer(Player player) {
        boolean isPuzzle = (strategy instanceof PuzzleStrategy);
        player.spawnNewBlock(world, isPuzzle);
        activeBlocks.add(player.getCurrentBlock());
    }

    
    private boolean isFixtureControlled(Fixture f) {
        for (Player p : players) {
            Block b = p.getCurrentBlock();
            if (b != null && f.getBody() == b.body)
                return true;
        }
        return false;
    }

    private Block getControlledBlock(Fixture f) {
        for (Player p : players) {
            Block b = p.getCurrentBlock();
            if (b != null && f.getBody() == b.body)
                return b;
        }
        return null;
    }

    private void checkLanding(Contact contact) {
        Fixture fa = contact.getFixtureA(), fb = contact.getFixtureB();
        if (!isFixtureControlled(fa) && !isFixtureControlled(fb))
            return;
        Vector2 normal = contact.getWorldManifold().getNormal();
        for (Player p : players)
            checkPlayerLanding(p, fa, fb, normal);
    }

    private void checkPlayerLanding(Player player, Fixture fa, Fixture fb, Vector2 normal) {
        Block block = player.getCurrentBlock();
        if (block == null)
            return;
        boolean isFa = (fa.getBody() == block.body);
        boolean isFb = (fb.getBody() == block.body);
        if (!isFa && !isFb)
            return;
        float ny = isFa ? -normal.y : normal.y;
        if (ny > LANDING_NORMAL_THRESHOLD && !blocksToSettle.contains(block, true)) {
            blocksToSettle.add(block);
        }
    }

    
    @Override
    public void render(float delta) {
        if (gameOver || transitioning)
            return;

        
        if (!countdownDone) {
            countdownTimer -= delta;
            if (countdownTimer <= 0) {
                countdownDone = true;
                
                for (Player p : players) {
                    spawnForPlayer(p);
                }
            }
        }

        if (countdownDone && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            pauseSelectedIndex = 0;
        }

        if (countdownDone && !paused) {
            elapsedTime += delta;

            for (int i = 0; i < players.length; i++)
                inputHandlers[i].handleInput(players[i]);

            
            
            float[] savedControlledX = new float[players.length];
            boolean[] hasControlledBlock = new boolean[players.length];
            for (int i = 0; i < players.length; i++) {
                Block b = players[i].getCurrentBlock();
                if (b != null && b.isControlled() && b.body != null) {
                    savedControlledX[i] = b.body.getPosition().x;
                    hasControlledBlock[i] = true;
                }
            }

            world.step(1 / 60f, STEP_VEL_ITERATIONS, STEP_POS_ITERATIONS);

            
            for (int i = 0; i < players.length; i++) {
                if (hasControlledBlock[i]) {
                    Block b = players[i].getCurrentBlock();
                    if (b != null && b.isControlled() && b.body != null) {
                        Vector2 pos = b.body.getPosition();
                        b.body.setTransform(savedControlledX[i], pos.y, b.body.getAngle());
                        Vector2 vel = b.body.getLinearVelocity();
                        b.body.setLinearVelocity(0, vel.y);
                    }
                }
            }

            processSettleQueue();
            updateMaxHeights();
            checkOutOfBounds();
            if (gameOver || transitioning)
                return;

            float[] mh = getMaxHeightsArray();
            if (strategy.checkWinCondition(players, elapsedTime, mh) ||
                    strategy.checkLoseCondition(players, elapsedTime)) {
                triggerGameOver();
                return;
            }

            for (Player p : players)
                updatePlayer(p, delta);

            
            float[] spellMh = getMaxHeightsArray();
            spellManager.update(delta, players, spellMh, world, activeBlocks);

            
            for (int i = 0; i < players.length; i++) {
                if (i < SPELL_CAST_KEYS.length && Gdx.input.isKeyJustPressed(SPELL_CAST_KEYS[i])) {
                    Spell casted = spellManager.castSpell(i, players, world, activeBlocks);
                    if (casted != null && spellCastSound != null) {
                        spellCastSound.play();
                    }
                }
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        
        updateCameraFollow(delta);
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

        
        
        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);

        if (countdownDone) {
            drawHudBackgrounds();
        }

        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();
        if (countdownDone) {
            drawHudText();
        }
        game.batch.end();

        
        if (!countdownDone) {
            drawCountdownOverlay();
        }

        if (paused) {
            drawPauseOverlay();

            
            float w = screenWidth, h = screenHeight;
            float cx = w / 2f;
            float mouseX = Gdx.input.getX();
            float mouseY = h - Gdx.input.getY();

            float cardW = 360, cardH = 90, gap = 20;
            float totalH = 3 * cardH + 2 * gap;
            float startY = h / 2f + totalH / 2f - 40;
            float cardsStartY = startY - 45f;

            for (int i = 0; i < 3; i++) {
                float y = cardsStartY - i * (cardH + gap) - 15f;
                float x = cx - cardW / 2;

                if (mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + cardH) {
                    pauseSelectedIndex = i;
                    if (Gdx.input.justTouched()) {
                        if (pauseSelectedIndex == 0) {
                            paused = false;
                        } else if (pauseSelectedIndex == 1) {
                            paused = false;
                            gameOver = true;
                            game.setScreen(new ModeSelectScreen(game, loggedInPlayerId));
                            return;
                        } else {
                            paused = false;
                            GameModeStrategy freshStrategy;
                            if (strategy instanceof PuzzleStrategy) {
                                freshStrategy = new PuzzleStrategy(playerCount);
                            } else if (strategy instanceof RaceStrategy) {
                                freshStrategy = new RaceStrategy();
                            } else if (strategy instanceof TimeAttackStrategy) {
                                freshStrategy = new TimeAttackStrategy();
                            } else {
                                freshStrategy = new com.alfa.chaotictower.strategy.SurvivalStrategy();
                            }
                            hide();
                            PlayingScreen ps = new PlayingScreen(game, playerCount, freshStrategy);
                            ps.setPlayerId(loggedInPlayerId);
                            game.setScreen(ps);
                            return;
                        }
                    }
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                pauseSelectedIndex = Math.max(0, pauseSelectedIndex - 1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                pauseSelectedIndex = Math.min(2, pauseSelectedIndex + 1);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                if (pauseSelectedIndex == 0) {
                    paused = false;
                } else if (pauseSelectedIndex == 1) {
                    paused = false;
                    gameOver = true;
                    game.setScreen(new ModeSelectScreen(game, loggedInPlayerId));
                    return;
                } else {
                    paused = false;
                    GameModeStrategy freshStrategy;
                    if (strategy instanceof PuzzleStrategy) {
                        freshStrategy = new PuzzleStrategy(playerCount);
                    } else if (strategy instanceof RaceStrategy) {
                        freshStrategy = new RaceStrategy();
                    } else if (strategy instanceof TimeAttackStrategy) {
                        freshStrategy = new TimeAttackStrategy();
                    } else {
                        freshStrategy = new com.alfa.chaotictower.strategy.SurvivalStrategy();
                    }
                    hide();
                    PlayingScreen ps = new PlayingScreen(game, playerCount, freshStrategy);
                    ps.setPlayerId(loggedInPlayerId);
                    game.setScreen(ps);
                    return;
                }
            }
        }
    }

    
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
        float totalH = 3 * cardH + 2 * gap;
        float startY = h / 2f + totalH / 2f - 40;
        
        
        float cardsStartY = startY - 45f;

        for (int i = 0; i < 3; i++) {
            float y = cardsStartY - i * (cardH + gap);
            boolean sel = (i == pauseSelectedIndex);

            shapeRenderer.setColor(sel ? CARD_SEL : CARD_BG);
            
            shapeRenderer.rect(cx - cardW / 2, y - 15f, cardW, cardH);

            if (sel) {
                shapeRenderer.setColor(ACCENT);
                shapeRenderer.rect(cx - cardW / 2, y - 15f, 5, cardH);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();

        glyphLayout.setText(titleFont, "PAUSED");
        titleFont.draw(game.batch, "PAUSED", cx - glyphLayout.width / 2, startY + cardH + 80);

        for (int i = 0; i < 3; i++) {
            float y = cardsStartY - i * (cardH + gap);
            boolean sel = (i == pauseSelectedIndex);

            if (sel)
                menuFont.setColor(Color.WHITE);
            else
                menuFont.setColor(0.6f, 0.6f, 0.7f, 1);
            menuFont.draw(game.batch, PAUSE_OPTS[i], cx - 140, y + 45);

            smallFont.setColor(0.5f, 0.5f, 0.6f, 1);
            smallFont.draw(game.batch, PAUSE_DESC[i], cx - 140, y + 17);
        }

        menuFont.setColor(Color.WHITE);
        smallFont.setColor(Color.WHITE);

        glyphLayout.setText(smallFont, "[ENTER] Select     [ESC] Resume");
        smallFont.draw(game.batch, "[ENTER] Select     [ESC] Resume", cx - glyphLayout.width / 2, 50);

        game.batch.end();
    }

    
    private void drawBackground() {
        float vw = viewport.getWorldWidth();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, vw, 30, BG_BOTTOM, BG_BOTTOM, BG_TOP, BG_TOP);
        shapeRenderer.end();
    }

    
    private void drawGrid() {
        float vw = viewport.getWorldWidth();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(GRID_COLOR);
        for (float x = 0; x <= vw; x += 1f)
            shapeRenderer.line(x, 0, x, 30);
        for (float y = 0; y <= 30; y += 1f)
            shapeRenderer.line(0, y, vw, y);
        shapeRenderer.end();
    }

    
    private void drawEnvironment() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        
        drawPedestal(10);
        if (playerCount == 2)
            drawPedestal(30);

        
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

        
        if (playerCount == 2) {
            shapeRenderer.setColor(DIVIDER_C);
            shapeRenderer.rect(20f - 0.12f, 0, 0.24f, 30);
        }

        shapeRenderer.end();
    }

    private void drawPedestal(float cx) {
        float x = cx - 2.5f, y = PEDESTAL_Y - PEDESTAL_HALF;
        float w = 5f, h = 2f;
        
        shapeRenderer.rect(x, y, w, h,
                PEDESTAL_C, PEDESTAL_C, PEDESTAL_T, PEDESTAL_T);
        
        tempColor.set(1, 1, 1, 0.12f);
        shapeRenderer.setColor(tempColor);
        shapeRenderer.rect(x, y + h - 0.08f, w, 0.08f);
    }

    
    private void drawBlocks() {
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < activeBlocks.size; i++) {
            Block block = activeBlocks.get(i);
            if (block.body == null)
                continue;
            drawBlockFilled(block);
        }
        shapeRenderer.end();

        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(OUTLINE_C);
        for (int i = 0; i < activeBlocks.size; i++) {
            Block block = activeBlocks.get(i);
            if (block.body == null)
                continue;
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

        
        if (block.isCemented()) {
            base = new Color(0.6f, 0.6f, 0.6f, 1f); 
        } else if (block.isIvied()) {
            base = new Color(
                    base.r * 0.5f, Math.min(1f, base.g * 0.8f + 0.4f), base.b * 0.3f, 1f); 
        } else if (block.isFrosted()) {
            base = new Color(0.5f, 0.8f, 1f, 0.85f); 
        }

        
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

        float scale = block.getScale();
        float tileSize = Block.TILE_SIZE * scale;
        float halfTile = tileSize / 2f;

        for (Vector2 local : block.getLocalTilePositions()) {
            float wx = pos.x + local.x * cos - local.y * sin;
            float wy = pos.y + local.x * sin + local.y * cos;
            shapeRenderer.rect(
                    wx - halfTile, wy - halfTile,
                    halfTile, halfTile,
                    tileSize, tileSize,
                    1, 1, deg);
        }

        
        tempColor.set(1, 1, 1, 0.15f);
        shapeRenderer.setColor(tempColor);
        float highlightH = tileSize * 0.18f;
        for (Vector2 local : block.getLocalTilePositions()) {
            float wx = pos.x + local.x * cos - local.y * sin;
            float wy = pos.y + local.x * sin + local.y * cos;
            shapeRenderer.rect(
                    wx - halfTile, wy + halfTile - highlightH,
                    halfTile, halfTile,
                    tileSize, highlightH,
                    1, 1, deg);
        }
    }

    private void drawBlockOutline(Block block) {
        Vector2 pos = block.body.getPosition();
        float angle = block.body.getAngle();
        float cos = MathUtils.cos(angle), sin = MathUtils.sin(angle);
        float deg = angle * MathUtils.radiansToDegrees;

        float scale = block.getScale();
        float tileSize = Block.TILE_SIZE * scale;
        float halfTile = tileSize / 2f;

        for (Vector2 local : block.getLocalTilePositions()) {
            float wx = pos.x + local.x * cos - local.y * sin;
            float wy = pos.y + local.x * sin + local.y * cos;
            shapeRenderer.rect(
                    wx - halfTile, wy - halfTile,
                    halfTile, halfTile,
                    tileSize, tileSize,
                    1, 1, deg);
        }
    }

    
    private void drawTargetLine() {
        float th = -1;
        if (strategy instanceof RaceStrategy)
            th = ((RaceStrategy) strategy).getTargetHeight();
        else if (strategy instanceof TimeAttackStrategy)
            th = ((TimeAttackStrategy) strategy).getTargetHeight();

        
        if (strategy instanceof PuzzleStrategy) {
            PuzzleStrategy puzzle = (PuzzleStrategy) strategy;
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float dashAlpha = 0.55f + 0.2f * (float) Math.sin(elapsedTime * 4.0);

            for (int i = 0; i < players.length; i++) {
                float laserH = puzzle.getEffectiveLaserHeight(i);
                float worldY = PEDESTAL_Y + PEDESTAL_HALF + laserH;

                
                tempColor.set(1f, 0.3f, 0.1f, dashAlpha);
                shapeRenderer.setColor(tempColor);
                if (playerCount == 1) {
                    shapeRenderer.rect(2.5f, worldY - 0.06f, 15f, 0.12f);
                } else {
                    float wallX = (i == 0) ? 2.5f : 22.5f;
                    shapeRenderer.rect(wallX, worldY - 0.06f, 15f, 0.12f);
                }
            }
            shapeRenderer.end();
            return;
        }

        if (th <= 0)
            return;

        float worldY = PEDESTAL_Y + PEDESTAL_HALF + th;
        if (strategy instanceof RaceStrategy) {
            float squareSize = 0.5f;
            float totalH = 2 * squareSize;
            float startY = worldY - totalH / 2f;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            for (int pIdx = 0; pIdx < playerCount; pIdx++) {
                float startX = (pIdx == 0) ? 2.5f : 22.5f;
                for (int col = 0; col < 30; col++) {
                    float x = startX + col * squareSize;
                    for (int row = 0; row < 2; row++) {
                        float y = startY + row * squareSize;
                        if ((col + row) % 2 == 0) {
                            shapeRenderer.setColor(1f, 1f, 1f, 0.5f); 
                        } else {
                            shapeRenderer.setColor(0f, 0f, 0f, 0.5f); 
                        }
                        shapeRenderer.rect(x, y, squareSize, squareSize);
                    }
                }
            }
            shapeRenderer.end();
        } else {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
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
    }

    
    private void drawHudBackgrounds() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float cardW = (playerCount == 2) ? 170f : 350f;
        float cardH = (playerCount == 2) ? 250f : 175f;
        float playLeft = (screenWidth - viewport.getScreenWidth()) / 2f;

        if (playerCount == 1) {
            float p1X = Math.max(20f, (playLeft - cardW) / 2f);
            drawCardBackground(p1X, screenHeight - 195, cardW, cardH, players[0]);
        } else {
            float p1X = 20f;
            float p2X = screenWidth - cardW - 20f;
            drawCardBackground(p1X, screenHeight - 270, cardW, cardH, players[0]);
            drawCardBackground(p2X, screenHeight - 270, cardW, cardH, players[1]);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawCardBackground(float x, float y, float w, float h, Player p) {
        
        shapeRenderer.setColor(0.45f, 0.35f, 0.85f, 0.5f);
        shapeRenderer.rect(x - 2, y - 2, w + 4, h + 4);

        
        shapeRenderer.setColor(0.10f, 0.10f, 0.20f, 0.85f);
        shapeRenderer.rect(x, y, w, h);

        
        float slotX, slotY, slotW, slotH;
        if (playerCount == 2) {
            slotW = 90;
            slotH = 80;
            slotX = x + (w - slotW) / 2f;
            slotY = y + 15;
        } else {
            slotX = x + 245;
            slotY = y + 15;
            slotW = 90;
            slotH = 115;
        }

        shapeRenderer.setColor(0.45f, 0.35f, 0.85f, 0.3f);
        shapeRenderer.rect(slotX - 1, slotY - 1, slotW + 2, slotH + 2);

        shapeRenderer.setColor(0.06f, 0.06f, 0.12f, 0.9f);
        shapeRenderer.rect(slotX, slotY, slotW, slotH);

        
        BlockFactory factory = BlockFactory.getInstance();
        boolean isPuzzle = (strategy instanceof PuzzleStrategy);
        int nextType = factory.peekNextType(p.getId(), isPuzzle, p.getBlocksSpawnedCount());
        Vector2[] offsets = factory.getShapeOffsets(nextType);
        Color color = BLOCK_COLORS[nextType];

        float tilePixel = (playerCount == 2) ? 16f : 20f;
        float centerX = slotX + slotW / 2f;
        float centerY = slotY + slotH / 2f;

        
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (Vector2 off : offsets) {
            if (off.x < minX)
                minX = off.x;
            if (off.x > maxX)
                maxX = off.x;
            if (off.y < minY)
                minY = off.y;
            if (off.y > maxY)
                maxY = off.y;
        }
        float shapeCenterX = (minX + maxX) / 2f;
        float shapeCenterY = (minY + maxY) / 2f;

        shapeRenderer.setColor(color);
        for (Vector2 off : offsets) {
            float tx = centerX + (off.x - shapeCenterX) * tilePixel;
            float ty = centerY + (off.y - shapeCenterY) * tilePixel;
            shapeRenderer.rect(tx - tilePixel / 2f + 1, ty - tilePixel / 2f + 1, tilePixel - 2, tilePixel - 2);
        }

        
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        for (Vector2 off : offsets) {
            float tx = centerX + (off.x - shapeCenterX) * tilePixel;
            float ty = centerY + (off.y - shapeCenterY) * tilePixel;
            shapeRenderer.rect(tx - tilePixel / 2f + 1, ty - tilePixel / 2f + 1, tilePixel - 2, tilePixel - 2);
        }
        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    }

    private void drawHudText() {
        float cx = screenWidth / 2f;

        
        menuFont.getData().setScale(0.8f);
        glyphLayout.setText(menuFont, strategy.getModeName());
        menuFont.draw(game.batch, strategy.getModeName(), cx - glyphLayout.width / 2f, screenHeight - 25);
        menuFont.getData().setScale(1.0f);

        
        hudBuilder.setLength(0);
        if (strategy instanceof TimeAttackStrategy) {
            double remaining = Math.max(0, ((TimeAttackStrategy) strategy).getTimeLimit() - elapsedTime);
            hudBuilder.append(String.format("%.1f", remaining)).append("s");
        } else {
            hudBuilder.append(String.format("%.1f", elapsedTime)).append("s");
        }
        smallFont.getData().setScale(0.9f);
        glyphLayout.setText(smallFont, hudBuilder);
        smallFont.draw(game.batch, hudBuilder, cx - glyphLayout.width / 2f, screenHeight - 60);
        smallFont.getData().setScale(1.0f);

        float cardW = (playerCount == 2) ? 170f : 350f;
        float playLeft = (screenWidth - viewport.getScreenWidth()) / 2f;

        
        if (playerCount == 1) {
            float p1X = Math.max(20f, (playLeft - cardW) / 2f);
            drawPlayerHudText(players[0], p1X, screenHeight - 195);
        } else {
            float p1X = 20f;
            float p2X = screenWidth - cardW - 20f;
            drawPlayerHudText(players[0], p1X, screenHeight - 270);
            drawPlayerHudText(players[1], p2X, screenHeight - 270);
        }
    }

    private void drawPlayerHudText(Player p, float cardX, float cardY) {
        float sx = (playerCount == 2) ? cardX + 10 : cardX + 15;

        if (playerCount == 2) {
            float baseScale = 0.55f;
            float hudScale = 0.60f;

            
            hudBuilder.setLength(0);
            hudBuilder.append("P").append(p.getId());
            hudFont.getData().setScale(hudScale);
            hudFont.draw(game.batch, hudBuilder, sx, cardY + 226);
            hudFont.getData().setScale(1.0f);

            
            hudBuilder.setLength(0);
            hudBuilder.append("Score: ").append(p.getScore());
            smallFont.getData().setScale(baseScale);
            smallFont.draw(game.batch, hudBuilder, sx, cardY + 204);

            
            hudBuilder.setLength(0);
            hudBuilder.append("Height: ").append(String.format("%.1f", p.getMaxHeight())).append("m");
            smallFont.draw(game.batch, hudBuilder, sx, cardY + 182);

            
            hudBuilder.setLength(0);
            hudBuilder.append("Lives: ").append(p.getLives());
            smallFont.draw(game.batch, hudBuilder, sx, cardY + 160);

            
            int playerIndex = p.getId() - 1;
            float cooldown = spellManager.getSpellCooldown(playerIndex);
            if (cooldown > 0) {
                hudBuilder.setLength(0);
                hudBuilder.append("COOLDOWN: ").append(String.format("%.1f", cooldown)).append("s");
                smallFont.setColor(0.7f, 0.7f, 0.7f, 0.8f);
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 138);
                smallFont.setColor(Color.WHITE);
            } else {
                Spell available = spellManager.getAvailableSpell(playerIndex);
                if (available != null) {
                    hudBuilder.setLength(0);
                    smallFont.setColor(0.3f, 0.9f, 1.0f, 1f); 
                    hudBuilder.append("MAGIC READY");
                    smallFont.draw(game.batch, hudBuilder, sx, cardY + 138);
                    smallFont.setColor(Color.WHITE);
                } else {
                    hudBuilder.setLength(0);
                    hudBuilder.append("NO SPELL");
                    smallFont.setColor(0.5f, 0.5f, 0.5f, 0.6f);
                    smallFont.draw(game.batch, hudBuilder, sx, cardY + 138);
                    smallFont.setColor(Color.WHITE);
                }
            }

            
            hudBuilder.setLength(0);
            if (p.isFrosted())
                hudBuilder.append("ICE ");
            if (p.isSpedUp())
                hudBuilder.append("FAST ");
            if (p.isWeighted())
                hudBuilder.append("HEAVY ");
            if (hudBuilder.length() > 0) {
                smallFont.setColor(0.6f, 0.8f, 1f, 1f);
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 118);
                smallFont.setColor(Color.WHITE);
            }

            
            float slotX = cardX + 40f;
            smallFont.getData().setScale(baseScale * 0.94f);
            glyphLayout.setText(smallFont, "NEXT");
            smallFont.draw(game.batch, "NEXT", slotX + 45f - glyphLayout.width / 2f, cardY + 102);
            smallFont.getData().setScale(1.0f);

        } else {
            float baseScale = 0.55f;
            float hudScale = 0.60f;

            if (strategy instanceof RaceStrategy) {
                
                hudBuilder.setLength(0);
                hudBuilder.append("P").append(p.getId());
                hudFont.getData().setScale(hudScale);
                hudFont.draw(game.batch, hudBuilder, sx, cardY + 145);
                hudFont.getData().setScale(1.0f);

                
                hudBuilder.setLength(0);
                hudBuilder.append("Height: ").append(String.format("%.1f", p.getMaxHeight())).append("m");
                smallFont.getData().setScale(baseScale);
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 115);

                
                hudBuilder.setLength(0);
                float target = ((RaceStrategy) strategy).getTargetHeight();
                float dist = Math.max(0f, target - p.getMaxHeight());
                hudBuilder.append("To Go: ").append(String.format("%.1f", dist)).append("m");
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 88);
            } else {
                
                hudBuilder.setLength(0);
                hudBuilder.append("P").append(p.getId());
                hudFont.getData().setScale(hudScale);
                hudFont.draw(game.batch, hudBuilder, sx, cardY + 145);
                hudFont.getData().setScale(1.0f);

                
                hudBuilder.setLength(0);
                hudBuilder.append("Score: ").append(p.getScore());
                smallFont.getData().setScale(baseScale);
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 118);

                
                hudBuilder.setLength(0);
                hudBuilder.append("Height: ").append(String.format("%.1f", p.getMaxHeight())).append("m");
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 91);

                
                hudBuilder.setLength(0);
                hudBuilder.append("Lives: ").append(p.getLives());
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 64);
            }

            
            hudBuilder.setLength(0);
            if (p.isFrosted())
                hudBuilder.append("ICE ");
            if (p.isSpedUp())
                hudBuilder.append("FAST ");
            if (p.isWeighted())
                hudBuilder.append("HEAVY ");
            if (hudBuilder.length() > 0) {
                smallFont.setColor(0.6f, 0.8f, 1f, 1f);
                smallFont.draw(game.batch, hudBuilder, sx, cardY + 37);
                smallFont.setColor(Color.WHITE);
            }

            
            float slotX = cardX + 245;
            smallFont.getData().setScale(baseScale * 0.94f);
            glyphLayout.setText(smallFont, "NEXT");
            smallFont.draw(game.batch, "NEXT", slotX + 45f - glyphLayout.width / 2f, cardY + 148);
            smallFont.getData().setScale(1.0f);
        }
    }

    
    private void drawCountdownOverlay() {
        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;

        String text;
        int sec = (int) Math.ceil(countdownTimer);
        if (sec >= 3)
            text = "3";
        else if (sec == 2)
            text = "2";
        else if (sec == 1)
            text = "1";
        else
            text = "GO!";

        
        float pulse = 1.0f + 0.15f * (float) Math.sin(countdownTimer * 12.0);

        game.batch.setProjectionMatrix(hudCamera.combined);
        game.batch.begin();
        titleFont.getData().setScale(pulse);
        glyphLayout.setText(titleFont, text);
        titleFont.draw(game.batch, text, cx - glyphLayout.width / 2, cy + glyphLayout.height / 2);
        titleFont.getData().setScale(1f);
        game.batch.end();
    }

    
    private void processSettleQueue() {
        if (blocksToSettle.size == 0)
            return;
        for (int i = 0; i < blocksToSettle.size; i++) {
            Block block = blocksToSettle.get(i);
            block.setControlled(false);
            for (Player p : players) {
                if (p.getCurrentBlock() == block) {
                    p.addScore(10);
                    p.clearCurrentBlock();
                    
                    if (strategy instanceof PuzzleStrategy) {
                        ((PuzzleStrategy) strategy).onBlockPlaced(p.getId() - 1);
                    }
                }
            }
        }
        blocksToSettle.clear();

        
        if (strategy instanceof PuzzleStrategy) {
            PuzzleStrategy puzzle = (PuzzleStrategy) strategy;
            for (int pi = 0; pi < players.length; pi++) {
                if (puzzle.isAboveLaser(pi, players[pi].getMaxHeight())) {
                    triggerGameOver();
                    return;
                }
            }
        }
    }

    private void updateMaxHeights() {
        for (int i = 0; i < activeBlocks.size; i++) {
            Block block = activeBlocks.get(i);
            if (!block.isControlled() && block.body != null) {
                float rel = block.body.getPosition().y - (PEDESTAL_Y + PEDESTAL_HALF);
                for (Player p : players) {
                    if (block.ownerId == p.getId())
                        p.updateMaxHeight(rel);
                }
            }
        }
    }

    private float[] getMaxHeightsArray() {
        float[] arr = new float[players.length];
        for (int i = 0; i < players.length; i++)
            arr[i] = players[i].getMaxHeight();
        return arr;
    }

    
    private void updateCameraFollow(float delta) {
        float maxTowerWorldY = PEDESTAL_Y + PEDESTAL_HALF;
        for (Player p : players) {
            float towerTop = PEDESTAL_Y + PEDESTAL_HALF + p.getMaxHeight();
            if (towerTop > maxTowerWorldY)
                maxTowerWorldY = towerTop;
        }

        
        
        
        float targetY = Math.max(MIN_CAMERA_Y, maxTowerWorldY - 10f);
        camera.position.y = MathUtils.lerp(camera.position.y, targetY, CAMERA_LERP_SPEED * delta);
        camera.update();
    }

    private void updatePlayer(Player p, float delta) {
        p.update(delta);
        if (p.canSpawn())
            spawnForPlayer(p);
    }

    
    private void checkOutOfBounds() {
        for (int i = activeBlocks.size - 1; i >= 0; i--) {
            Block block = activeBlocks.get(i);
            if (block.body.getPosition().y < -2f) {
                for (Player p : players) {
                    if (block.ownerId == p.getId()) {
                        
                        if (strategy instanceof PuzzleStrategy) {
                            ((PuzzleStrategy) strategy).onBlockLost(p.getId() - 1);
                        } else if (strategy instanceof RaceStrategy) {
                            
                        } else {
                            p.loseLife();
                        }
                    }
                    if (block == p.getCurrentBlock())
                        p.clearCurrentBlock();
                }
                destroyAndFreeBlock(block);
                activeBlocks.removeIndex(i);
                if (strategy.checkLoseCondition(players, elapsedTime)) {
                    triggerGameOver();
                    return;
                }
            }
        }
    }

    
    private void triggerGameOver() {
        if (gameOver)
            return;
        gameOver = true;
        float[] mh = getMaxHeightsArray();
        String result = strategy.getResultText(players, elapsedTime, mh);
        int bestScore = 0;
        float bestH = 0;
        for (Player p : players) {
            if (p.getScore() > bestScore)
                bestScore = p.getScore();
            if (p.getMaxHeight() > bestH)
                bestH = p.getMaxHeight();
        }
        if (strategy instanceof RaceStrategy) {
            bestScore = (int) (elapsedTime * 1000);
        }
        game.setScreen(new GameOverScreen(game, loggedInPlayerId,
                bestScore, elapsedTime, bestH, strategy.getBackendModeKey(), result));
    }

    private void destroyAndFreeBlock(Block block) {
        if (block.body != null && world != null) {
            world.destroyBody(block.body);
            block.body = null;
        }
        BlockFactory.getInstance().freeBlock(block);
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, true);
        screenWidth = w;
        screenHeight = h;
        hudCamera.setToOrtho(false, w, h);
        hudCamera.update();
    }

    @Override
    public void hide() {
        transitioning = true;
        blocksToSettle.clear();
        for (int i = 0; i < activeBlocks.size; i++)
            destroyAndFreeBlock(activeBlocks.get(i));
        activeBlocks.clear();
        if (world != null) {
            world.dispose();
            world = null;
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }
        if (spellCastSound != null) {
            spellCastSound.dispose();
            spellCastSound = null;
        }
    }

    public void setPlayerId(Long id) {
        this.loggedInPlayerId = id;
    }

    public Long getPlayerId() {
        return loggedInPlayerId;
    }
}
