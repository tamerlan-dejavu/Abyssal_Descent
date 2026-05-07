package com.abyssaldescent.ui.screen;

import com.abyssaldescent.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public final class GameOverScreen implements Screen {

    private final GameOverStats stats;

    private SpriteBatch batch;

    private Texture backgroundTexture;
    private Texture tierReachedLabelTexture;
    private Texture tierNameTexture;
    private Texture buttonOffTexture;
    private Texture buttonOnTexture;

    private float buttonX;
    private float buttonY;
    private float buttonW = 500f;
    private float buttonH = 150f;

    private float tierLabelX;
    private float tierLabelY;
    private float tierNameX;
    private float tierNameY;

    private boolean buttonHovered = false;

    public GameOverScreen(GameOverStats stats) {
        this.stats = stats;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        batch = new SpriteBatch();

        loadTextures();
        layoutElements();
    }

    private void loadTextures() {
        backgroundTexture = loadTexture("ui/backgrounds/death-screen.jpg");
        tierReachedLabelTexture = loadTexture("ui/buttons/tier_reached.png");
        tierNameTexture = loadTierNameTexture();
        buttonOffTexture = loadTexture("ui/buttons/back_to_menu_off.png");
        buttonOnTexture = loadTexture("ui/buttons/back_to_menu_on.png");
    }

    private Texture loadTierNameTexture() {
        String tierPath = getTierTexturePath(stats.floorReached);
        return loadTexture(tierPath);
    }

    private String getTierTexturePath(int floor) {
        switch (floor) {
            case 1: return "ui/buttons/upper_ruins.png";
            case 2: return "ui/buttons/flooded_catacombs.png";
            case 3: return "ui/buttons/maltarions_abyss.png";
            default: return "ui/buttons/upper_ruins.png";
        }
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

    private void layoutElements() {
        float sw = Gdx.graphics.getWidth();

        buttonW = 500f;
        buttonH = 150f;
        buttonX = (sw - buttonW) * 0.5f;
        buttonY = 80f;

        float tierLabelW = 300f;
        float tierLabelH = 80f;
        tierLabelX = (sw - tierLabelW) * 0.5f;
        tierLabelY = buttonY + buttonH + 50f;

        float tierNameW = 400f;
        tierNameX = (sw - tierNameW) * 0.5f;
        tierNameY = tierLabelY + tierLabelH;
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

        drawTierLabel();
        drawTierName();
        drawButton();

        batch.end();
    }

    private void drawTierLabel() {
        if (tierReachedLabelTexture != null) {
            batch.draw(tierReachedLabelTexture, tierLabelX, tierLabelY, 300f, 80f);
        }
    }

    private void drawTierName() {
        if (tierNameTexture != null) {
            batch.draw(tierNameTexture, tierNameX, tierNameY, 400f, 100f);
        }
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

    @Override
    public void resize(int w, int h) {
        layoutElements();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (tierReachedLabelTexture != null) tierReachedLabelTexture.dispose();
        if (tierNameTexture != null) tierNameTexture.dispose();
        if (buttonOffTexture != null) buttonOffTexture.dispose();
        if (buttonOnTexture != null) buttonOnTexture.dispose();
    }
}
