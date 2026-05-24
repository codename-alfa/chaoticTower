package com.alfa.chaotictower;
 
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.alfa.chaotictower.screen.*;
 
public class Main extends Game {
    public SpriteBatch batch;
    public Music backgroundMusic;
 
    @Override
    public void create() {
        batch = new SpriteBatch();
        GameAssetManager.getInstance().loadAssets();
        
        
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("tetris.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.35f); 
            backgroundMusic.play();
        } catch (Exception e) {
            Gdx.app.error("Main", "Could not load or play background music 'tetris.mp3': " + e.getMessage());
        }
        
        setScreen(new MainMenuScreen(this));
    }
 
    @Override
    public void render() {
        super.render();
    }
 
    @Override
    public void pause() {
        super.pause();
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }
 
    @Override
    public void resume() {
        super.resume();
        if (backgroundMusic != null) {
            backgroundMusic.play();
        }
    }
 
    @Override
    public void dispose() {
        batch.dispose();
        GameAssetManager.getInstance().dispose();
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
    }
}
