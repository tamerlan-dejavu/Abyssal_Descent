package com.abyssaldescent.ui.screen;

import com.abyssaldescent.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public final class GameOverScreen implements Screen {

    private final GameOverStats stats;

    private SpriteBatch   batch;
    private BitmapFont    fontTier;
    private GlyphLayout   layout;

    private Texture backgroundTexture;
    private Texture buttonOffTexture;
    private Texture buttonOnTexture;

    private float buttonX;
    private float buttonY;
    private float buttonW;
    private float buttonH;

    private boolean buttonHovered = false;

    public GameOverScreen(GameOverStats stats) {
        this.stats = stats;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        batch  = new SpriteBatch();
        layout = new GlyphLayout();

        fontTier = new BitmapFont();
        fontTier.getData().setScale(2.0f);

        float sw = Gdx.graphics.getWidth();

        loadTextures();

        buttonW = 400f;
        buttonH = 120f;
        buttonX = (sw - buttonW) * 0.5f;
        buttonY = 80f;
    }

    private void loadTextures() {
        backgroundTexture = loadTexture("ui/backgrounds/death-screen.jpg");
        buttonOffTexture  = loadTexture("ui/buttons/back_to_menu_off.png");
        buttonOnTexture   = loadTexture("ui/buttons/back_to_menu_on.png");
    }

    private Texture loadTexture(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                return new Texture(path);
            }
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Failed to load " + path, e);
        }
        return null;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1f);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();

        buttonHovered = mx >= buttonX && mx <= buttonX + buttonW &&
                       my >= buttonY && my <= buttonY + buttonH;

        if (Gdx.input.justTouched()) {
            if (buttonHovered) {
                onReturnClicked();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            onReturnClicked();
            return;
        }

        batch.begin();

        if (backgroundTexture != null) {
            batch.draw(backgroundTexture, 0, 0, sw, sh);
        }

        drawTierText(sw, sh);

        drawButton();

        batch.end();
    }

    private void drawTierText(float sw, float sh) {
        String tierText = "Tier reached: " + tierName(stats.floorReached);
        fontTier.setColor(Color.WHITE);
        layout.setText(fontTier, tierText);
        float textX = (sw - layout.width) * 0.5f;
        float textY = sh - 120f;
        fontTier.draw(batch, layout, textX, textY);
    }

    private void drawButton() {
        Texture buttonTex = buttonHovered ? buttonOnTexture : buttonOffTexture;
        if (buttonTex != null) {
            batch.draw(buttonTex, buttonX, buttonY, buttonW, buttonH);
        }
    }

    private void onReturnClicked() {
        GameStateManager.getInstance().resetForNewRun();
        UiManager.getInstance().showMainMenu();
    }

    private static String tierName(int floor) {
        switch (floor) {
            case 1:  return "Upper Ruins";
            case 2:  return "Sunken Crypts";
            case 3:  return "Void Core";
            default: return "Floor " + floor;
        }
    }

    @Override
    public void resize(int w, int h) {
        buttonW = 400f;
        buttonH = 120f;
        buttonX = (w - buttonW) * 0.5f;
        buttonY = 80f;
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (fontTier != null) fontTier.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (buttonOffTexture != null) buttonOffTexture.dispose();
        if (buttonOnTexture != null) buttonOnTexture.dispose();
    }
}
