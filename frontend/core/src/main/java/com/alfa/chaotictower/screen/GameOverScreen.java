package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.GameAssetManager;
import com.alfa.chaotictower.network.ApiClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.JsonValue;

public class GameOverScreen extends ScreenAdapter {

    private final Main game;
    private final Long playerId;
    private final int score;
    private final double timeRecord;
    private final float maxHeight;
    private final String gameMode;
    private final String resultText;

    private ShapeRenderer shapes;
    private BitmapFont titleFont, menuFont, hudFont, smallFont;
    private final GlyphLayout glyph = new GlyphLayout();
    private String statusMessage = "Saving score to server...";
    private boolean isProcessDone = false;
    private float time = 0;

    private static final Color BG_BOT = new Color(0.08f, 0.02f, 0.02f, 1);
    private static final Color BG_TOP = new Color(0.18f, 0.05f, 0.08f, 1);
    private static final Color PANEL  = new Color(0.10f, 0.06f, 0.06f, 0.80f);

    public GameOverScreen(Main game, Long playerId, int score, double timeRecord,
                          float maxHeight, String gameMode, String resultText) {
        this.game = game;
        this.playerId = playerId;
        this.score = score;
        this.timeRecord = timeRecord;
        this.maxHeight = maxHeight;
        this.gameMode = gameMode;
        this.resultText = resultText;
    }

    public GameOverScreen(Main game, Long playerId, int score, double timeRecord) {
        this(game, playerId, score, timeRecord, 0f, "CLASSIC", "Game Over");
    }

    @Override
    public void show() {
        shapes = new ShapeRenderer();
        GameAssetManager assets = GameAssetManager.getInstance();
        titleFont = assets.getFont(GameAssetManager.FONT_TITLE);
        menuFont  = assets.getFont(GameAssetManager.FONT_MENU);
        hudFont   = assets.getFont(GameAssetManager.FONT_HUD);
        smallFont = assets.getFont(GameAssetManager.FONT_SMALL);

        if (playerId != null) {
            ApiClient.submitScore(playerId, gameMode, score, timeRecord, maxHeight, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JsonValue result) {
                    Gdx.app.postRunnable(() -> { statusMessage = "Score saved to Leaderboard!"; isProcessDone = true; });
                }
                @Override
                public void onFailure(Throwable t) {
                    Gdx.app.postRunnable(() -> { statusMessage = "Connection failed: Score not saved."; isProcessDone = true; });
                }
            });
        } else {
            statusMessage = "Not logged in - score not submitted.";
            isProcessDone = true;
        }
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

        // Stats panel (resized to fit elegantly and prevent any overlaps)
        float panelW = 560, panelH = 260;
        float panelX = cx - panelW / 2, panelY = h / 2f - 110;
        shapes.setColor(PANEL);
        shapes.rect(panelX, panelY, panelW, panelH);
        // Panel accent top edge
        shapes.setColor(0.70f, 0.20f, 0.25f, 0.8f);
        shapes.rect(panelX, panelY + panelH - 4, panelW, 4);
        shapes.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Text
        game.batch.begin();

        // GAME OVER title (scaled down slightly for sleek, modern appearance)
        float bob = 4f * (float) Math.sin(time * 2.0);
        titleFont.getData().setScale(0.8f);
        glyph.setText(titleFont, "GAME OVER");
        titleFont.setColor(0.95f, 0.25f, 0.25f, 1);
        titleFont.draw(game.batch, "GAME OVER", cx - glyph.width / 2, h - 75 + bob);
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(1.0f);

        // Result text (scaled menuFont with precise offset)
        menuFont.getData().setScale(0.8f);
        String[] lines = resultText.split("\n");
        for (int i = 0; i < lines.length; i++) {
            glyph.setText(menuFont, lines[i]);
            menuFont.draw(game.batch, lines[i], cx - glyph.width / 2, h - 130 - i * 35);
        }
        menuFont.getData().setScale(1.0f);

        // Stats inside panel
        float sy = panelY + panelH - 35;
        float sx = panelX + 40;
        float vx = panelX + panelW - 40;

        smallFont.getData().setScale(0.85f);
        smallFont.setColor(0.7f, 0.7f, 0.8f, 1);
        smallFont.draw(game.batch, "Mode", sx, sy + 3);
        smallFont.draw(game.batch, "Score", sx, sy - 45 + 3);
        smallFont.draw(game.batch, "Time", sx, sy - 90 + 3);
        smallFont.draw(game.batch, "Max Height", sx, sy - 135 + 3);
        smallFont.draw(game.batch, "Status", sx, sy - 185 + 3);

        hudFont.getData().setScale(0.8f);
        hudFont.setColor(Color.WHITE);
        glyph.setText(hudFont, gameMode);
        hudFont.draw(game.batch, gameMode, vx - glyph.width, sy);
        glyph.setText(hudFont, String.valueOf(score));
        hudFont.draw(game.batch, String.valueOf(score), vx - glyph.width, sy - 45);
        glyph.setText(hudFont, String.format("%.1fs", timeRecord));
        hudFont.draw(game.batch, String.format("%.1fs", timeRecord), vx - glyph.width, sy - 90);
        glyph.setText(hudFont, String.format("%.1fm", maxHeight));
        hudFont.draw(game.batch, String.format("%.1fm", maxHeight), vx - glyph.width, sy - 135);
        hudFont.getData().setScale(1.0f);

        if (statusMessage.contains("saved")) smallFont.setColor(0.3f, 0.9f, 0.4f, 1);
        else if (statusMessage.contains("failed")) smallFont.setColor(1f, 0.4f, 0.4f, 1);
        else smallFont.setColor(0.8f, 0.8f, 0.5f, 1);
        glyph.setText(smallFont, statusMessage);
        smallFont.draw(game.batch, statusMessage, vx - glyph.width, sy - 185);
        smallFont.setColor(Color.WHITE);

        // Continue prompt
        if (isProcessDone) {
            float blink = ((int)(time * 2) % 2 == 0) ? 1f : 0.5f;
            smallFont.setColor(1, 1, 1, blink);
            glyph.setText(smallFont, "Press SPACE to return to Main Menu");
            smallFont.draw(game.batch, "Press SPACE to return to Main Menu", cx - glyph.width / 2, panelY - 35);
            smallFont.setColor(Color.WHITE);
        }
        smallFont.getData().setScale(1.0f);

        game.batch.end();

        if (isProcessDone && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override public void hide() { dispose(); }
    @Override public void dispose() { if (shapes != null) shapes.dispose(); }
}
