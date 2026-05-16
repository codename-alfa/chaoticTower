package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.network.ApiClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

public class MainMenuScreen extends ScreenAdapter {

    private final Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private String typedName = "";
    private boolean isConnecting = false;

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char character) {
                if (isConnecting) return false;

                if (character == '\b' && typedName.length() > 0) {
                    typedName = typedName.substring(0, typedName.length() - 1);
                } else if (character == '\r' || character == '\n') {
                    if (typedName.trim().length() > 0) {
                        processLogin();
                    }
                } else if (Character.isLetterOrDigit(character)) {
                    typedName += character;
                }
                return true;
            }
        });
    }

    private void processLogin() {
        isConnecting = true;
        ApiClient.login(typedName.trim(), new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JsonValue result) {
                long playerId = result.getLong("id");
                Gdx.app.postRunnable(() -> {
                    PlayingScreen playingScreen = new PlayingScreen(game);
                    playingScreen.setPlayerId(playerId);
                    game.setScreen(playingScreen);
                });
            }

            @Override
            public void onFailure(Throwable t) {
                Gdx.app.postRunnable(() -> isConnecting = false);
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        if (isConnecting) {
            font.draw(batch, "Menghubungkan ke server...", 50, Gdx.graphics.getHeight() / 2f);
        } else {
            font.draw(batch, "Ketik Username Anda & Tekan Enter:", 50, Gdx.graphics.getHeight() / 2f + 50);
            font.draw(batch, "> " + typedName, 50, Gdx.graphics.getHeight() / 2f);
        }
        batch.end();
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
