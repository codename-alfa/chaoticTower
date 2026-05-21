package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.GameAssetManager;
import com.alfa.chaotictower.network.ApiClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.JsonValue;

public class MainMenuScreen extends ScreenAdapter {

    private final Main game;
    private ShapeRenderer shapes;
    private String typedName = "";
    private boolean isConnecting = false;
    private String errorMessage = "";
    private float time = 0;

    private BitmapFont titleFont, menuFont, smallFont;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private static final Color BG_BOT = new Color(0.03f, 0.02f, 0.06f, 1);
    private static final Color BG_TOP = new Color(0.08f, 0.05f, 0.18f, 1);

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        shapes = new ShapeRenderer();
        GameAssetManager assets = GameAssetManager.getInstance();
        titleFont = assets.getFont(GameAssetManager.FONT_TITLE);
        menuFont  = assets.getFont(GameAssetManager.FONT_MENU);
        smallFont = assets.getFont(GameAssetManager.FONT_SMALL);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char c) {
                if (isConnecting) return false;
                if (c == '\b' && typedName.length() > 0) {
                    typedName = typedName.substring(0, typedName.length() - 1);
                } else if (c == '\r' || c == '\n') {
                    if (typedName.trim().length() > 0) processLogin();
                } else if (Character.isLetterOrDigit(c) && typedName.length() < 20) {
                    typedName += c;
                }
                return true;
            }
        });
    }

    private void processLogin() {
        isConnecting = true;
        errorMessage = "";
        ApiClient.login(typedName.trim(), new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JsonValue result) {
                long id = result.getLong("id");
                Gdx.app.postRunnable(() -> game.setScreen(new ModeSelectScreen(game, id)));
            }
            @Override
            public void onFailure(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    isConnecting = false;
                    if (t.getMessage() != null && !t.getMessage().isEmpty()) {
                        errorMessage = "Error: " + t.getMessage();
                    } else {
                        errorMessage = "Connection failed. Is the server running?";
                    }
                });
            }
        });
    }

    @Override
    public void render(float delta) {
        time += delta;
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float cx = w / 2f;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Background gradient
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0, 0, w, h, BG_BOT, BG_BOT, BG_TOP, BG_TOP);

        // Decorative floating blocks
        drawDecoBlocks(w, h);

        // Input field box
        float boxW = 500, boxH = 55;
        float boxX = cx - boxW / 2, boxY = h / 2f - 50;
        shapes.setColor(0.12f, 0.12f, 0.22f, 0.85f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.setColor(0.35f, 0.30f, 0.65f, 0.7f);
        shapes.rect(boxX, boxY, boxW, 3); // bottom edge
        shapes.rect(boxX, boxY + boxH - 2, boxW, 2); // top edge

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Text
        game.batch.begin();

        // Title with subtle vertical bob
        float bob = 6f * (float) Math.sin(time * 1.5);
        glyphLayout.setText(titleFont, "CHAOTIC TOWER");
        titleFont.draw(game.batch, "CHAOTIC TOWER", cx - glyphLayout.width / 2, h - 180 + bob);

        if (isConnecting) {
            glyphLayout.setText(menuFont, "Connecting to server...");
            menuFont.draw(game.batch, "Connecting to server...", cx - glyphLayout.width / 2, h / 2f + 50);
        } else {
            glyphLayout.setText(smallFont, "Enter your Username & Press Enter");
            smallFont.draw(game.batch, "Enter your Username & Press Enter", cx - glyphLayout.width / 2, h / 2f + 40);

            // Cursor blink
            String cursor = ((int)(time * 2) % 2 == 0) ? "_" : "";
            menuFont.draw(game.batch, "> " + typedName + cursor, boxX + 15, boxY + boxH - 12);

            if (errorMessage.length() > 0) {
                smallFont.setColor(1f, 0.4f, 0.4f, 1);
                glyphLayout.setText(smallFont, errorMessage);
                smallFont.draw(game.batch, errorMessage, cx - glyphLayout.width / 2, boxY - 25);
                smallFont.setColor(Color.WHITE);
            }
        }

        game.batch.end();
    }

    private void drawDecoBlocks(float w, float h) {
        Color[] colors = {
            new Color(0.95f, 0.90f, 0.20f, 0.08f),
            new Color(0.20f, 0.90f, 0.95f, 0.08f),
            new Color(0.70f, 0.25f, 0.95f, 0.08f),
            new Color(0.95f, 0.60f, 0.15f, 0.08f),
        };
        for (int i = 0; i < 8; i++) {
            float bx = (float)(Math.sin(time * 0.3 + i * 1.7) * 0.5 + 0.5) * w;
            float by = ((time * 20 + i * 150) % (h + 80)) - 40;
            float size = 30 + i * 8;
            float angle = time * 15 + i * 45;
            shapes.setColor(colors[i % colors.length]);
            shapes.rect(bx, by, size / 2, size / 2, size, size, 1, 1, angle);
        }
    }

    @Override
    public void hide() { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() { if (shapes != null) shapes.dispose(); }
}
