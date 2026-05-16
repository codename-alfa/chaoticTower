package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.network.ApiClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

public class GameOverScreen extends ScreenAdapter {

    private final Main game;
    private final Long playerId;
    private final int score;
    private final double timeRecord;

    private SpriteBatch batch;
    private BitmapFont font;
    private String statusMessage = "Menyimpan skor ke server...";
    private boolean isProcessDone = false;

    public GameOverScreen(Main game, Long playerId, int score, double timeRecord) {
        this.game = game;
        this.playerId = playerId;
        this.score = score;
        this.timeRecord = timeRecord;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();

        ApiClient.submitScore(playerId, "CLASSIC", score, timeRecord, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JsonValue result) {
                Gdx.app.postRunnable(() -> {
                    statusMessage = "Skor berhasil disimpan di Leaderboard!";
                    isProcessDone = true;
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    statusMessage = "Koneksi gagal: Skor tidak tersimpan.";
                    isProcessDone = true;
                });
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.6f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        font.getData().setScale(3f);
        font.draw(batch, "GAME OVER", 50, Gdx.graphics.getHeight() - 100);

        font.getData().setScale(1.5f);
        font.draw(batch, "Skor Akhir: " + score, 50, Gdx.graphics.getHeight() - 200);
        font.draw(batch, "Waktu Bertahan: " + String.format("%.1f", timeRecord) + " detik", 50, Gdx.graphics.getHeight() - 250);

        font.draw(batch, "Status: " + statusMessage, 50, 150);

        if (isProcessDone) {
            font.draw(batch, "> Tekan SPASI untuk kembali ke Menu Utama", 50, 80);
        }

        batch.end();

        if (isProcessDone && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
