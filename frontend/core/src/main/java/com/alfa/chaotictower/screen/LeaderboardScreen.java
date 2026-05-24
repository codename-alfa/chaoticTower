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

import java.util.ArrayList;
import java.util.List;

public class LeaderboardScreen extends ScreenAdapter {

    private final Main game;
    private ShapeRenderer shapes;
    private BitmapFont titleFont, menuFont, smallFont, hudFont;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    private int activeModeIdx = 0; 
    private static final String[] MODES = { "SURVIVAL", "RACE", "PUZZLE" };
    private static final String[] MODE_LABELS = { "Survival", "Race Mode", "Puzzle" };

    private List<LeaderboardEntry> scoresList = new ArrayList<>();
    private boolean loading = true;
    private String errorMessage = "";
    private float time = 0;

    private static final Color BG_BOT = new Color(0.03f, 0.02f, 0.06f, 1);
    private static final Color BG_TOP = new Color(0.08f, 0.05f, 0.18f, 1);
    private static final Color PANEL = new Color(0.08f, 0.08f, 0.16f, 0.90f);
    private static final Color CARD_BG = new Color(0.12f, 0.12f, 0.22f, 0.85f);
    private static final Color CARD_SEL = new Color(0.20f, 0.16f, 0.38f, 0.95f);
    private static final Color ACCENT = new Color(0.45f, 0.35f, 0.85f, 1);
    private Long returnPlayerId = null;

    private static class LeaderboardEntry {
        String username;
        int score;
        double timeRecord;
        double maxHeight;
    }

    public LeaderboardScreen(Main game) {
        this.game = game;
    }

    public LeaderboardScreen(Main game, Long returnPlayerId) {
        this.game = game;
        this.returnPlayerId = returnPlayerId;
    }

    @Override
    public void show() {
        shapes = new ShapeRenderer();
        GameAssetManager assets = GameAssetManager.getInstance();
        titleFont = assets.getFont(GameAssetManager.FONT_TITLE);
        menuFont  = assets.getFont(GameAssetManager.FONT_MENU);
        hudFont   = assets.getFont(GameAssetManager.FONT_HUD);
        smallFont = assets.getFont(GameAssetManager.FONT_SMALL);

        fetchScores();
    }

    private void fetchScores() {
        loading = true;
        errorMessage = "";
        scoresList.clear();

        ApiClient.getTop10Scores(MODES[activeModeIdx], new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JsonValue result) {
                List<LeaderboardEntry> temp = new ArrayList<>();
                if (result != null && result.isArray()) {
                    for (int i = 0; i < result.size; i++) {
                        JsonValue row = result.get(i);
                        LeaderboardEntry entry = new LeaderboardEntry();
                        JsonValue p = row.get("player");
                        entry.username = p != null ? p.getString("username") : "Unknown";
                        entry.score = row.getInt("score");
                        entry.timeRecord = row.getDouble("timeRecord");
                        entry.maxHeight = row.getDouble("maxHeight");
                        temp.add(entry);
                    }
                }
                Gdx.app.postRunnable(() -> {
                    scoresList = temp;
                    loading = false;
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    errorMessage = "Failed to load leaderboard data.";
                    loading = false;
                });
            }
        });
    }

    @Override
    public void render(float delta) {
        time += delta;
        float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();
        float cx = w / 2f;

        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (returnPlayerId != null) {
                game.setScreen(new ModeSelectScreen(game, returnPlayerId));
            } else {
                game.setScreen(new MainMenuScreen(game));
            }
            return;
        }

        float mouseX = Gdx.input.getX();
        float mouseY = h - Gdx.input.getY();

        float panelW = 1100, panelH = 510;
        float panelX = cx - panelW / 2, panelY = h / 2f - 250;

        float btnW = 200, btnH = 42, gap = 30;
        float startX = cx - (3 * btnW + 2 * gap) / 2f;
        float btnY = panelY + panelH - 75;

        
        for (int i = 0; i < 3; i++) {
            float bx = startX + i * (btnW + gap);
            if (mouseX >= bx && mouseX <= bx + btnW && mouseY >= btnY && mouseY <= btnY + btnH) {
                if (Gdx.input.justTouched() && activeModeIdx != i) {
                    activeModeIdx = i;
                    fetchScores();
                    return;
                }
            }
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.rect(0, 0, w, h, BG_BOT, BG_BOT, BG_TOP, BG_TOP);

        
        shapes.setColor(PANEL);
        shapes.rect(panelX, panelY, panelW, panelH);
        
        
        shapes.setColor(ACCENT);
        shapes.rect(panelX, panelY + panelH - 4, panelW, 4);

        
        for (int i = 0; i < 3; i++) {
            float bx = startX + i * (btnW + gap);
            boolean isSelected = (i == activeModeIdx);
            boolean isHovered = (mouseX >= bx && mouseX <= bx + btnW && mouseY >= btnY && mouseY <= btnY + btnH);

            shapes.setColor(isSelected ? CARD_SEL : (isHovered ? new Color(0.16f, 0.14f, 0.28f, 0.85f) : CARD_BG));
            shapes.rect(bx, btnY, btnW, btnH);

            if (isSelected) {
                shapes.setColor(ACCENT);
                shapes.rect(bx, btnY + btnH - 3, btnW, 3);
            }
        }
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        
        game.batch.begin();

        
        titleFont.getData().setScale(0.8f);
        glyphLayout.setText(titleFont, "LEADERBOARDS");
        titleFont.draw(game.batch, "LEADERBOARDS", cx - glyphLayout.width / 2, h - 35);
        titleFont.getData().setScale(1.0f);

        
        smallFont.getData().setScale(0.9f);
        for (int i = 0; i < 3; i++) {
            float bx = startX + i * (btnW + gap);
            glyphLayout.setText(smallFont, MODE_LABELS[i]);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(game.batch, MODE_LABELS[i], bx + btnW / 2 - glyphLayout.width / 2, btnY + btnH / 2 + glyphLayout.height / 2 + 1);
        }
        smallFont.getData().setScale(1.0f);

        
        float headerY = btnY - 30;
        smallFont.getData().setScale(0.85f);
        smallFont.setColor(0.7f, 0.7f, 0.8f, 0.9f);

        String currentMode = MODES[activeModeIdx];

        if (currentMode.equals("SURVIVAL")) {
            smallFont.draw(game.batch, "Rank", panelX + 50, headerY);
            smallFont.draw(game.batch, "Player", panelX + 160, headerY);
            smallFont.draw(game.batch, "Score", panelX + 440, headerY);
            smallFont.draw(game.batch, "Height", panelX + 650, headerY);
            smallFont.draw(game.batch, "Time Record", panelX + 830, headerY);
        } else if (currentMode.equals("RACE")) {
            smallFont.draw(game.batch, "Rank", panelX + 100, headerY);
            smallFont.draw(game.batch, "Player", panelX + 300, headerY);
            smallFont.draw(game.batch, "Time Record", panelX + 750, headerY);
        } else if (currentMode.equals("PUZZLE")) {
            smallFont.draw(game.batch, "Rank", panelX + 100, headerY);
            smallFont.draw(game.batch, "Player", panelX + 300, headerY);
            smallFont.draw(game.batch, "Blocks Placed", panelX + 750, headerY);
        }

        smallFont.setColor(Color.WHITE);
        smallFont.getData().setScale(1.0f);

        
        game.batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.45f, 0.35f, 0.85f, 0.25f);
        shapes.rect(panelX + 30, headerY - 12, panelW - 60, 2);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        game.batch.begin();

        
        if (loading) {
            smallFont.getData().setScale(0.95f);
            glyphLayout.setText(smallFont, "Loading leaderboard data...");
            smallFont.draw(game.batch, "Loading leaderboard data...", cx - glyphLayout.width / 2, panelY + panelH / 2 - 30);
            smallFont.getData().setScale(1.0f);
        } else if (!errorMessage.isEmpty()) {
            smallFont.getData().setScale(0.95f);
            smallFont.setColor(0.95f, 0.35f, 0.35f, 1);
            glyphLayout.setText(smallFont, errorMessage);
            smallFont.draw(game.batch, errorMessage, cx - glyphLayout.width / 2, panelY + panelH / 2 - 30);
            smallFont.setColor(Color.WHITE);
            smallFont.getData().setScale(1.0f);
        } else if (scoresList.isEmpty()) {
            smallFont.getData().setScale(0.95f);
            smallFont.setColor(0.6f, 0.6f, 0.7f, 1);
            glyphLayout.setText(smallFont, "No records found for this mode.");
            smallFont.draw(game.batch, "No records found for this mode.", cx - glyphLayout.width / 2, panelY + panelH / 2 - 30);
            smallFont.setColor(Color.WHITE);
            smallFont.getData().setScale(1.0f);
        } else {
            float startRowY = headerY - 45;
            float rowGap = 32;
            hudFont.getData().setScale(0.75f);

            for (int i = 0; i < scoresList.size(); i++) {
                float ry = startRowY - i * rowGap;
                LeaderboardEntry entry = scoresList.get(i);

                
                Color rankColor = Color.WHITE;
                if (i == 0) rankColor = new Color(1f, 0.82f, 0.15f, 1f);      
                else if (i == 1) rankColor = new Color(0.78f, 0.82f, 0.88f, 1f); 
                else if (i == 2) rankColor = new Color(0.85f, 0.55f, 0.35f, 1f); 

                if (currentMode.equals("SURVIVAL")) {
                    hudFont.setColor(rankColor);
                    hudFont.draw(game.batch, "#" + (i + 1), panelX + 50, ry);
                    hudFont.setColor(Color.WHITE);

                    hudFont.draw(game.batch, entry.username, panelX + 160, ry);
                    hudFont.draw(game.batch, String.valueOf(entry.score), panelX + 440, ry);
                    hudFont.draw(game.batch, String.format("%.1fm", entry.maxHeight), panelX + 650, ry);
                    hudFont.draw(game.batch, String.format("%.1fs", entry.timeRecord), panelX + 830, ry);
                } else if (currentMode.equals("RACE")) {
                    hudFont.setColor(rankColor);
                    hudFont.draw(game.batch, "#" + (i + 1), panelX + 100, ry);
                    hudFont.setColor(Color.WHITE);

                    hudFont.draw(game.batch, entry.username, panelX + 300, ry);

                    
                    double recordSec = entry.score / 1000.0;
                    hudFont.setColor(0.3f, 0.9f, 0.4f, 1f); 
                    hudFont.draw(game.batch, String.format("%.2fs", recordSec), panelX + 750, ry);
                    hudFont.setColor(Color.WHITE);
                } else if (currentMode.equals("PUZZLE")) {
                    hudFont.setColor(rankColor);
                    hudFont.draw(game.batch, "#" + (i + 1), panelX + 100, ry);
                    hudFont.setColor(Color.WHITE);

                    hudFont.draw(game.batch, entry.username, panelX + 300, ry);

                    
                    int blocksPlaced = entry.score / 10;
                    hudFont.setColor(0.3f, 0.8f, 1.0f, 1f); 
                    hudFont.draw(game.batch, blocksPlaced + " blocks", panelX + 750, ry);
                    hudFont.setColor(Color.WHITE);
                }
            }
            hudFont.getData().setScale(1.0f);
        }

        
        float blink = ((int)(time * 2) % 2 == 0) ? 1f : 0.6f;
        smallFont.setColor(1f, 1f, 1f, blink);
        String returnPrompt = (returnPlayerId != null) ? "[ESC] Return to Select Mode" : "[ESC] Return to Main Menu";
        glyphLayout.setText(smallFont, returnPrompt);
        smallFont.draw(game.batch, returnPrompt, cx - glyphLayout.width / 2, panelY - 30);
        smallFont.setColor(Color.WHITE);

        game.batch.end();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (shapes != null) {
            shapes.dispose();
            shapes = null;
        }
    }
}
