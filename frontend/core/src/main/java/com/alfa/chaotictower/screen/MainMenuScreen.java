package com.alfa.chaotictower.screen;

import com.alfa.chaotictower.Main;
import com.alfa.chaotictower.GameAssetManager;
import com.alfa.chaotictower.network.ApiClient;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.JsonValue;

public class MainMenuScreen extends ScreenAdapter {

    private final Main game;
    private ShapeRenderer shapes;
    private Texture leaderboardIcon;
    private String typedName = "";
    private String typedPassword = "";
    private int activeFieldIndex = 0; 
    private boolean isRegisterMode = false;
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
        leaderboardIcon = new Texture(Gdx.files.internal("leaderboard.png"));
        GameAssetManager assets = GameAssetManager.getInstance();
        titleFont = assets.getFont(GameAssetManager.FONT_TITLE);
        menuFont  = assets.getFont(GameAssetManager.FONT_MENU);
        smallFont = assets.getFont(GameAssetManager.FONT_SMALL);

        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float w = Gdx.graphics.getWidth();
                float h = Gdx.graphics.getHeight();

                
                float clickX = screenX;
                float clickY = h - screenY;

                
                float btnLeaderX = w - 72 - 30;
                float btnLeaderY = h - 64 - 30;
                if (clickX >= btnLeaderX && clickX <= btnLeaderX + 72 && clickY >= btnLeaderY && clickY <= btnLeaderY + 64) {
                    game.setScreen(new LeaderboardScreen(game));
                    return true;
                }

                if (isConnecting) return false;

                float cx = w / 2f;

                
                float boxW = 520, boxH = 50;
                float uBoxX = cx - boxW / 2, uBoxY = h / 2f + 10;
                float pBoxX = cx - boxW / 2, pBoxY = h / 2f - 60;

                float btnW = 240, btnH = 45;
                float lBtnX = cx - btnW - 20, lBtnY = h / 2f - 135;
                float rBtnX = cx + 20, rBtnY = h / 2f - 135;

                if (clickX >= uBoxX && clickX <= uBoxX + boxW && clickY >= uBoxY && clickY <= uBoxY + boxH) {
                    activeFieldIndex = 0;
                    return true;
                } else if (clickX >= pBoxX && clickX <= pBoxX + boxW && clickY >= pBoxY && clickY <= pBoxY + boxH) {
                    activeFieldIndex = 1;
                    return true;
                } else if (clickX >= lBtnX && clickX <= lBtnX + btnW && clickY >= lBtnY && clickY <= lBtnY + btnH) {
                    activeFieldIndex = 2;
                    isRegisterMode = false;
                    if (typedName.trim().length() > 0 && typedPassword.length() > 0) {
                        processSubmit();
                    }
                    return true;
                } else if (clickX >= rBtnX && clickX <= rBtnX + btnW && clickY >= rBtnY && clickY <= rBtnY + btnH) {
                    activeFieldIndex = 2;
                    isRegisterMode = true;
                    if (typedName.trim().length() > 0 && typedPassword.length() > 0) {
                        processSubmit();
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean keyDown(int keycode) {
                if (isConnecting) return false;
                if (keycode == com.badlogic.gdx.Input.Keys.TAB) {
                    activeFieldIndex = (activeFieldIndex + 1) % 3;
                    return true;
                } else if (keycode == com.badlogic.gdx.Input.Keys.UP) {
                    activeFieldIndex = (activeFieldIndex + 2) % 3;
                    return true;
                } else if (keycode == com.badlogic.gdx.Input.Keys.DOWN) {
                    activeFieldIndex = (activeFieldIndex + 1) % 3;
                    return true;
                } else if (activeFieldIndex == 2) {
                    if (keycode == com.badlogic.gdx.Input.Keys.LEFT || keycode == com.badlogic.gdx.Input.Keys.RIGHT) {
                        isRegisterMode = !isRegisterMode;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean keyTyped(char c) {
                if (isConnecting) return false;
                if (c == '\b') {
                    if (activeFieldIndex == 0) {
                        if (typedName.length() > 0) {
                            typedName = typedName.substring(0, typedName.length() - 1);
                        }
                    } else if (activeFieldIndex == 1) {
                        if (typedPassword.length() > 0) {
                            typedPassword = typedPassword.substring(0, typedPassword.length() - 1);
                        }
                    }
                } else if (c == '\r' || c == '\n') {
                    if (activeFieldIndex == 0) {
                        if (typedName.trim().length() > 0) activeFieldIndex = 1;
                    } else if (activeFieldIndex == 1) {
                        if (typedPassword.length() > 0) activeFieldIndex = 2;
                    } else if (activeFieldIndex == 2) {
                        if (typedName.trim().length() > 0 && typedPassword.length() > 0) {
                            processSubmit();
                        }
                    }
                } else if (c == '\t') {
                    
                } else if (c >= 32 && c <= 126) {
                    if (activeFieldIndex == 0) {
                        if (typedName.length() < 20) typedName += c;
                    } else if (activeFieldIndex == 1) {
                        if (typedPassword.length() < 20) typedPassword += c;
                    }
                }
                return true;
            }
        });
    }

    private void processSubmit() {
        isConnecting = true;
        errorMessage = "";

        ApiClient.ApiCallback callback = new ApiClient.ApiCallback() {
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
                        errorMessage = t.getMessage();
                    } else {
                        errorMessage = "Connection failed. Is the server running?";
                    }
                });
            }
        };

        if (isRegisterMode) {
            ApiClient.register(typedName.trim(), typedPassword, callback);
        } else {
            ApiClient.login(typedName.trim(), typedPassword, callback);
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

        
        titleFont.getData().setScale(0.8f);
        menuFont.getData().setScale(0.65f);
        smallFont.getData().setScale(0.7f);

        
        float boxW = 520, boxH = 50;
        float uBoxX = cx - boxW / 2, uBoxY = h / 2f + 10;
        float pBoxX = cx - boxW / 2, pBoxY = h / 2f - 60;

        float btnW = 240, btnH = 45;
        float lBtnX = cx - btnW - 20, lBtnY = h / 2f - 135;
        float rBtnX = cx + 20, rBtnY = h / 2f - 135;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(BG_BOT.r, BG_BOT.g, BG_BOT.b, 1.0f); 
        shapes.rect(0, 0, w, h, BG_BOT, BG_BOT, BG_TOP, BG_TOP);

        
        drawDecoBlocks(w, h);

        
        shapes.setColor(0.16f, 0.16f, 0.28f, 0.9f);
        shapes.rect(uBoxX, uBoxY, boxW, boxH);
        
        if (activeFieldIndex == 0) {
            shapes.setColor(0.3f, 0.9f, 1.0f, 0.8f);
        } else {
            shapes.setColor(0.25f, 0.20f, 0.45f, 0.5f);
        }
        shapes.rect(uBoxX, uBoxY, boxW, 2); 
        shapes.rect(uBoxX, uBoxY + boxH - 2, boxW, 2); 
        shapes.rect(uBoxX, uBoxY, 2, boxH); 
        shapes.rect(uBoxX + boxW - 2, uBoxY, 2, boxH); 

        
        shapes.setColor(0.16f, 0.16f, 0.28f, 0.9f);
        shapes.rect(pBoxX, pBoxY, boxW, boxH);
        
        if (activeFieldIndex == 1) {
            shapes.setColor(0.3f, 0.9f, 1.0f, 0.8f);
        } else {
            shapes.setColor(0.25f, 0.20f, 0.45f, 0.5f);
        }
        shapes.rect(pBoxX, pBoxY, boxW, 2); 
        shapes.rect(pBoxX, pBoxY + boxH - 2, boxW, 2); 
        shapes.rect(pBoxX, pBoxY, 2, boxH); 
        shapes.rect(pBoxX + boxW - 2, pBoxY, 2, boxH); 

        
        shapes.setColor(0.16f, 0.16f, 0.28f, 0.9f);
        shapes.rect(lBtnX, lBtnY, btnW, btnH);
        if (!isRegisterMode) {
            if (activeFieldIndex == 2) {
                shapes.setColor(0.3f, 0.9f, 1.0f, 0.8f); 
            } else {
                shapes.setColor(0.35f, 0.30f, 0.65f, 0.9f); 
            }
        } else {
            shapes.setColor(0.20f, 0.20f, 0.30f, 0.3f); 
        }
        shapes.rect(lBtnX, lBtnY, btnW, 2);
        shapes.rect(lBtnX, lBtnY + btnH - 2, btnW, 2);
        shapes.rect(lBtnX, lBtnY, 2, btnH);
        shapes.rect(lBtnX + btnW - 2, lBtnY, 2, btnH);

        
        shapes.setColor(0.16f, 0.16f, 0.28f, 0.9f);
        shapes.rect(rBtnX, rBtnY, btnW, btnH);
        if (isRegisterMode) {
            if (activeFieldIndex == 2) {
                shapes.setColor(0.3f, 0.9f, 1.0f, 0.8f); 
            } else {
                shapes.setColor(0.35f, 0.30f, 0.65f, 0.9f); 
            }
        } else {
            shapes.setColor(0.20f, 0.20f, 0.30f, 0.3f); 
        }
        shapes.rect(rBtnX, rBtnY, btnW, 2);
        shapes.rect(rBtnX, rBtnY + btnH - 2, btnW, 2);
        shapes.rect(rBtnX, rBtnY, 2, btnH);
        shapes.rect(rBtnX + btnW - 2, rBtnY, 2, btnH);

        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        
        game.batch.begin();

        
        float btnLeaderX = w - 72 - 30;
        float btnLeaderY = h - 64 - 30;
        float mouseX = Gdx.input.getX();
        float mouseY = h - Gdx.input.getY();
        boolean hover = (mouseX >= btnLeaderX && mouseX <= btnLeaderX + 72 && mouseY >= btnLeaderY && mouseY <= btnLeaderY + 64);

        if (hover) {
            game.batch.setColor(0.85f, 0.7f, 1.0f, 1.0f); 
        } else {
            game.batch.setColor(1.0f, 1.0f, 1.0f, 0.85f); 
        }
        game.batch.draw(leaderboardIcon, btnLeaderX, btnLeaderY, 72, 64);
        game.batch.setColor(Color.WHITE); 

        
        float bob = 6f * (float) Math.sin(time * 1.5);
        glyphLayout.setText(titleFont, "CHAOTIC TOWER");
        titleFont.draw(game.batch, "CHAOTIC TOWER", cx - glyphLayout.width / 2, h - 140 + bob);

        if (isConnecting) {
            glyphLayout.setText(menuFont, isRegisterMode ? "Registering account..." : "Logging in to server...");
            menuFont.draw(game.batch, isRegisterMode ? "Registering account..." : "Logging in to server...", cx - glyphLayout.width / 2, h / 2f + 95);
        } else {
            
            String uCursor = (activeFieldIndex == 0 && (int)(time * 2) % 2 == 0) ? "_" : "";
            String uText = "User: " + typedName + uCursor;
            glyphLayout.setText(menuFont, uText);
            menuFont.draw(game.batch, uText, uBoxX + 15, uBoxY + boxH / 2f + glyphLayout.height / 2f - 2f);

            
            String pCursor = (activeFieldIndex == 1 && (int)(time * 2) % 2 == 0) ? "_" : "";
            String masked = "*".repeat(typedPassword.length());
            String pText = "Pass: " + masked + pCursor;
            glyphLayout.setText(menuFont, pText);
            menuFont.draw(game.batch, pText, pBoxX + 15, pBoxY + boxH / 2f + glyphLayout.height / 2f - 2f);

            
            String lBtnText = "LOGIN";
            glyphLayout.setText(menuFont, lBtnText);
            if (!isRegisterMode) {
                menuFont.setColor(0.3f, 0.9f, 1.0f, 1f);
            } else {
                menuFont.setColor(0.5f, 0.5f, 0.6f, 0.7f);
            }
            menuFont.draw(game.batch, lBtnText, lBtnX + btnW / 2f - glyphLayout.width / 2f, lBtnY + btnH / 2f + glyphLayout.height / 2f - 2f);
            menuFont.setColor(Color.WHITE);

            
            String rBtnText = "REGISTER";
            glyphLayout.setText(menuFont, rBtnText);
            if (isRegisterMode) {
                menuFont.setColor(0.3f, 0.9f, 1.0f, 1f);
            } else {
                menuFont.setColor(0.5f, 0.5f, 0.6f, 0.7f);
            }
            menuFont.draw(game.batch, rBtnText, rBtnX + btnW / 2f - glyphLayout.width / 2f, rBtnY + btnH / 2f + glyphLayout.height / 2f - 2f);
            menuFont.setColor(Color.WHITE);

            
            if (errorMessage.length() > 0) {
                smallFont.setColor(1f, 0.4f, 0.4f, 1);
                glyphLayout.setText(smallFont, errorMessage);
                smallFont.draw(game.batch, errorMessage, cx - glyphLayout.width / 2, lBtnY - 30);
                smallFont.setColor(Color.WHITE);
            }
        }

        game.batch.end();

        
        titleFont.getData().setScale(1.0f);
        menuFont.getData().setScale(1.0f);
        smallFont.getData().setScale(1.0f);
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
    public void hide() {
        Gdx.input.setInputProcessor(null);
        dispose();
    }

    @Override
    public void dispose() {
        if (shapes != null) {
            shapes.dispose();
            shapes = null;
        }
        if (leaderboardIcon != null) {
            leaderboardIcon.dispose();
            leaderboardIcon = null;
        }
    }
}
