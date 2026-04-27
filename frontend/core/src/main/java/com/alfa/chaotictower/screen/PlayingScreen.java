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
    private final Array<Block> blocksToSettle = new Array<>();

    private Player player1;
    private Player player2;

    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;

    // StringBuilder di-cache agar tidak membuat String baru 60x/detik
    private final StringBuilder hudBuilder = new StringBuilder(32);

    // Cache ukuran layar — hanya berubah saat resize()
    private int screenWidth;
    private int screenHeight;

    // FIX BUG 1: Flag ini mencegah dua hal:
    // (a) render() melanjutkan menggunakan world/debugRenderer yang sudah di-dispose
    //     setelah game.setScreen() dipanggil dari dalam checkOutOfBounds()
    // (b) transisi layar ganda jika dua blok jatuh di frame yang sama
    private boolean gameOver = false;

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

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update(); // Dipanggil sekali di sini dan di resize() — tidak di render()

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
                Fixture fa = contact.getFixtureA();
                Fixture fb = contact.getFixtureB();
                checkLandingFast(fa, fb);
            }
            @Override public void endContact(Contact contact) {}
            @Override public void preSolve(Contact contact, Manifold oldManifold) {}
            @Override public void postSolve(Contact contact, ContactImpulse impulse) {}
        });
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

    private void checkLandingFast(Fixture fa, Fixture fb) {
        checkPlayerLandingFast(player1, fa, fb);
        checkPlayerLandingFast(player2, fa, fb);
    }

    private void checkPlayerLandingFast(Player player, Fixture fa, Fixture fb) {
        Block block = player.getCurrentBlock();
        // Guard cepat: hanya proses jika block sedang dikontrol
        if (block == null || !block.isControlled()) return;

        Body blockBody = block.body;
        if (fa.getBody() == blockBody || fb.getBody() == blockBody) {
            if (!blocksToSettle.contains(block, true)) {
                blocksToSettle.add(block);
            }
        }
    }

    @Override
    public void render(float delta) {
        // FIX BUG 1 (bagian awal): Jika game sudah selesai (dari frame sebelumnya),
        // hentikan render segera. LibGDX memanggil render() satu frame lagi
        // setelah setScreen() sebelum benar-benar pindah layar.
        if (gameOver) return;

        Gdx.gl.glClearColor(0.1f, 0.4f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();
        world.step(1 / 60f, 10, 8);
        processSettleQueue();
        checkOutOfBounds();

        // FIX BUG 1 (bagian kritis): Guard INI yang menyelamatkan.
        //
        // Skenario crash tanpa guard ini:
        // 1. checkOutOfBounds() mendeteksi lives <= 0
        // 2. game.setScreen(new GameOverScreen()) dipanggil
        // 3. LibGDX langsung memanggil hide() → world.dispose() + debugRenderer.dispose()
        // 4. Eksekusi kembali ke render() setelah checkOutOfBounds() return
        // 5. TANPA GUARD: debugRenderer.render(world, ...) → CRASH karena keduanya disposed
        // 6. DENGAN GUARD: langsung return, tidak ada yang menyentuh world/debugRenderer
        if (gameOver) return;

        updatePlayer(player1, delta);
        updatePlayer(player2, delta);

        viewport.apply();
        debugRenderer.render(world, camera.combined);

        // hudCamera tidak perlu di-update setiap frame — sudah dilakukan di show()/resize()
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

    /**
     * Cara BENAR untuk melepaskan block kembali ke pool:
     * 1. Hancurkan body di world (agar tidak accumulate di Box2D)
     * 2. null-kan referensi body (Block.reset() juga null-kan, tapi eksplisit lebih aman)
     * 3. Kembalikan Java object ke pool untuk di-reuse
     *
     * Pola ini memutus ikatan antara Box2D body dan Object Pool,
     * sehingga aman lintas sesi game.
     */
    private void destroyAndFreeBlock(Block block) {
        if (block.body != null && world != null) {
            world.destroyBody(block.body);
            block.body = null; // Hindari double-free jika reset() dipanggil ulang
        }
        BlockFactory.getInstance().freeBlock(block);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        screenWidth = width;
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

        float currentFallSpeed = Gdx.input.isKeyPressed(down) ? -10.0f : -2.0f;
        block.body.setLinearVelocity(0, currentFallSpeed);

        Vector2 pos = block.body.getPosition();
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
        // Bersihkan antrean settle terlebih dahulu (tidak perlu diproses)
        blocksToSettle.clear();

        // Hancurkan semua body dan kembalikan block ke pool
        for (int i = 0; i < activeBlocks.size; i++) {
            destroyAndFreeBlock(activeBlocks.get(i));
        }
        activeBlocks.clear();

        // Dispose resource Box2D — setelah ini world tidak boleh diakses lagi
        if (world != null) {
            world.dispose();
            world = null;
        }
        if (debugRenderer != null) {
            debugRenderer.dispose();
            debugRenderer = null;
        }

        gameOver = false; // Reset untuk sesi berikutnya jika PlayingScreen di-reuse
    }
}
