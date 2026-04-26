package com.alfa.chaotictower;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

public class GameAssetManager {
    private static GameAssetManager instance;
    public final AssetManager manager;

    public static final String FONT_HUD = "hud_font.ttf";

    private GameAssetManager() {
        manager = new AssetManager();

        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
    }

    public static GameAssetManager getInstance() {
        if (instance == null) {
            instance = new GameAssetManager();
        }
        return instance;
    }

    public void loadAssets() {
        FreetypeFontLoader.FreeTypeFontLoaderParameter fontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        fontParams.fontFileName = "pixel.ttf";
        fontParams.fontParameters.size = 32;
        fontParams.fontParameters.color = Color.WHITE;
        fontParams.fontParameters.borderWidth = 2;
        fontParams.fontParameters.borderColor = Color.BLACK;

        manager.load(FONT_HUD, BitmapFont.class, fontParams);
        manager.finishLoading();
    }

    public void dispose() {
        manager.dispose();
    }
}
