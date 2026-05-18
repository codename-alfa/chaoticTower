package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.GameAssetManager;
import com.alfa.chaotictower.strategy.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ModeSelectScreen extends ScreenAdapter {

    private final Main game;
    private final Long loggedInPlayerId;

    private ShapeRenderer shapes;
    private BitmapFont titleFont, menuFont, smallFont;
    private final GlyphLayout glyph = new GlyphLayout();

    private int playerCount = 1;
    private int modeIndex = 0;
    private int selectionStep = 0;
    private boolean transitioning = false;
    private float time = 0;

    private static final String[] SP_MODES = {"Survival", "Time Attack"};
    private static final String[] MP_MODES = {"Survival", "Race"};
    private static final String[] SP_DESC = {
        "Build as high as you can with 3 lives!",
        "Reach 20m height within 2 minutes!"
    };
    private static final String[] MP_DESC = {
        "Last player standing wins!",
        "First to reach the top wins!"
    };

    private static final Color BG_BOT = new Color(0.03f, 0.02f, 0.06f, 1);
    private static final Color BG_TOP = new Color(0.06f, 0.05f, 0.16f, 1);
    private static final Color CARD_BG = new Color(0.10f, 0.10f, 0.20f, 0.85f);
    private static final Color CARD_SEL = new Color(0.18f, 0.15f, 0.35f, 0.95f);
    private static final Color ACCENT = new Color(0.45f, 0.35f, 0.85f, 1);

    public ModeSelectScreen(Main game, Long playerId) {
        this.game = game;
        this.loggedInPlayerId = playerId;
    }

    @Override
    public void show() {
        shapes = new ShapeRenderer();
        GameAssetManager assets = GameAssetManager.getInstance();
        titleFont = assets.getFont(GameAssetManager.FONT_TITLE);
        menuFont  = assets.getFont(GameAssetManager.FONT_MENU);
        smallFont = assets.getFont(GameAssetManager.FONT_SMALL);
    }

    @Override
    public void render(float delta) {
        if (transitioning) return;
        time += delta;

        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float cx = w / 2f;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0, 0, w, h, BG_BOT, BG_BOT, BG_TOP, BG_TOP);

        if (selectionStep == 0) {
            drawPlayerCards(cx, h);
        } else {
            drawModeCards(cx, h);
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        game.batch.begin();
        glyph.setText(titleFont, "SELECT MODE");
        titleFont.draw(game.batch, "SELECT MODE", cx - glyph.width / 2, h - 80);

        if (selectionStep == 0) {
            drawPlayerText(cx, h);
        } else {
            drawModeText(cx, h);
        }

        glyph.setText(smallFont, "[ENTER] Select     [ESC] Back");
        smallFont.draw(game.batch, "[ENTER] Select     [ESC] Back", cx - glyph.width / 2, 50);
        game.batch.end();

        handleInput();
    }

    // ── Player count cards ─────────────────────────────────
    private void drawPlayerCards(float cx, float h) {
        float cardW = 360, cardH = 100, gap = 30;
        float totalH = cardH * 2 + gap;
        float startY = h / 2f + totalH / 2f - 60;

        for (int i = 0; i < 2; i++) {
            float y = startY - i * (cardH + gap);
            boolean sel = (i == 0 && playerCount == 1) || (i == 1 && playerCount == 2);
            shapes.setColor(sel ? CARD_SEL : CARD_BG);
            shapes.rect(cx - cardW / 2, y, cardW, cardH);
            if (sel) {
                shapes.setColor(ACCENT);
                shapes.rect(cx - cardW / 2, y, 5, cardH);
            }
        }
    }

    private void drawPlayerText(float cx, float h) {
        float cardH = 100, gap = 30;
        float totalH = cardH * 2 + gap;
        float startY = h / 2f + totalH / 2f - 60;

        glyph.setText(smallFont, "Choose Player Count");
        smallFont.draw(game.batch, "Choose Player Count", cx - glyph.width / 2, startY + cardH + 35);

        String[] labels = {"1 PLAYER", "2 PLAYERS"};
        String[] descs = {"Single player challenge", "Local multiplayer"};
        for (int i = 0; i < 2; i++) {
            float y = startY - i * (cardH + gap);
            boolean sel = (i == 0 && playerCount == 1) || (i == 1 && playerCount == 2);
            if (sel) menuFont.setColor(Color.WHITE);
            else menuFont.setColor(0.6f, 0.6f, 0.7f, 1);
            menuFont.draw(game.batch, labels[i], cx - 140, y + cardH - 25);
            smallFont.setColor(0.5f, 0.5f, 0.6f, 1);
            smallFont.draw(game.batch, descs[i], cx - 140, y + 30);
        }
        menuFont.setColor(Color.WHITE);
        smallFont.setColor(Color.WHITE);
    }

    // ── Game mode cards ────────────────────────────────────
    private void drawModeCards(float cx, float h) {
        String[] modes = (playerCount == 1) ? SP_MODES : MP_MODES;
        float cardW = 420, cardH = 110, gap = 25;
        float totalH = modes.length * cardH + (modes.length - 1) * gap;
        float startY = h / 2f + totalH / 2f - 50;

        for (int i = 0; i < modes.length; i++) {
            float y = startY - i * (cardH + gap);
            boolean sel = (i == modeIndex);
            shapes.setColor(sel ? CARD_SEL : CARD_BG);
            shapes.rect(cx - cardW / 2, y, cardW, cardH);
            if (sel) {
                shapes.setColor(ACCENT);
                shapes.rect(cx - cardW / 2, y, 5, cardH);
            }
        }
    }

    private void drawModeText(float cx, float h) {
        String[] modes = (playerCount == 1) ? SP_MODES : MP_MODES;
        String[] descs = (playerCount == 1) ? SP_DESC : MP_DESC;
        float cardH = 110, gap = 25;
        float totalH = modes.length * cardH + (modes.length - 1) * gap;
        float startY = h / 2f + totalH / 2f - 50;

        glyph.setText(smallFont, playerCount + "P - Choose Game Mode");
        smallFont.draw(game.batch, playerCount + "P - Choose Game Mode", cx - glyph.width / 2, startY + cardH + 35);

        for (int i = 0; i < modes.length; i++) {
            float y = startY - i * (cardH + gap);
            boolean sel = (i == modeIndex);
            if (sel) menuFont.setColor(Color.WHITE);
            else menuFont.setColor(0.6f, 0.6f, 0.7f, 1);
            menuFont.draw(game.batch, modes[i], cx - 180, y + cardH - 25);
            smallFont.setColor(0.5f, 0.5f, 0.6f, 1);
            smallFont.draw(game.batch, descs[i], cx - 180, y + 35);
        }
        menuFont.setColor(Color.WHITE);
        smallFont.setColor(Color.WHITE);
    }

    // ── Input ──────────────────────────────────────────────
    private void handleInput() {
        if (selectionStep == 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) playerCount = 1;
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) playerCount = 2;
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) { selectionStep = 1; modeIndex = 0; }
        } else {
            String[] modes = (playerCount == 1) ? SP_MODES : MP_MODES;
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) modeIndex = Math.max(0, modeIndex - 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) modeIndex = Math.min(modes.length - 1, modeIndex + 1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) launchGame(modes[modeIndex]);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (selectionStep > 0) selectionStep--;
            else { transitioning = true; game.setScreen(new MainMenuScreen(game)); }
        }
    }

    private void launchGame(String name) {
        GameModeStrategy s;
        switch (name) {
            case "Race": s = new RaceStrategy(); break;
            case "Time Attack": s = new TimeAttackStrategy(); break;
            default: s = new SurvivalStrategy(); break;
        }
        PlayingScreen ps = new PlayingScreen(game, playerCount, s);
        ps.setPlayerId(loggedInPlayerId);
        transitioning = true;
        game.setScreen(ps);
    }

    @Override public void hide() { dispose(); }
    @Override public void dispose() { if (shapes != null) shapes.dispose(); }
}
