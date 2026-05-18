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

    public static final String FONT_TITLE = "title_font.ttf";
    public static final String FONT_MENU  = "menu_font.ttf";
    public static final String FONT_HUD   = "hud_font.ttf";
    public static final String FONT_SMALL = "small_font.ttf";

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
        loadFont(FONT_TITLE, 72, Color.WHITE, 3, Color.BLACK);
        loadFont(FONT_MENU,  40, Color.WHITE, 2, Color.BLACK);
        loadFont(FONT_HUD,   32, Color.WHITE, 2, Color.BLACK);
        loadFont(FONT_SMALL, 22, Color.WHITE, 1, Color.BLACK);
        manager.finishLoading();
    }

    private void loadFont(String name, int size, Color color, float borderWidth, Color borderColor) {
        FreetypeFontLoader.FreeTypeFontLoaderParameter p = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        p.fontFileName = "pixel.ttf";
        p.fontParameters.size = size;
        p.fontParameters.color = color;
        p.fontParameters.borderWidth = borderWidth;
        p.fontParameters.borderColor = borderColor;
        manager.load(name, BitmapFont.class, p);
    }

    public BitmapFont getFont(String key) {
        return manager.get(key, BitmapFont.class);
    }

    public void dispose() {
        manager.dispose();
    }
}
