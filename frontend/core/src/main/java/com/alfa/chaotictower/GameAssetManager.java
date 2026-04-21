package com.alfa.chaotictower;

import com.badlogic.gdx.assets.AssetManager;

public class GameAssetManager {
    private static GameAssetManager instance;
    public final AssetManager manager = new AssetManager();

    private GameAssetManager() {
    }

    public static GameAssetManager getInstance() {
        if (instance == null) {
            instance = new GameAssetManager();
        }
        return instance;
    }

    public void loadAssets() {
    }

    public void dispose() {
        manager.dispose();
    }
}
