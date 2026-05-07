package com.abyssaldescent.ui.screen;

import com.abyssaldescent.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public final class GameOverScreen implements Screen {

    private final GameOverStats stats;

    private SpriteBatch batch;
    private Music loseMusic;

    private Texture backgroundTexture;
    private Texture tierNameTexture;
    private Texture buttonOffTexture;
    private Texture buttonOnTexture;

    private float buttonX;
    private float buttonY;
    private float buttonW = 500f;
    private float buttonH = 150f;

    private float tierNameX;
    private float tierNameY;
    private float tierNameW;
    private float tierNameH;

    private boolean buttonHovered = false;

    public GameOverScreen(GameOverStats stats) {
        this.stats = stats;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        batch = new SpriteBatch();

        loadTextures();
        loadMusic();
        layoutElements();
    }

    private void loadTextures() {
        backgroundTexture = loadTexture("ui/backgrounds/death-screen.jpg");
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

    private void loadMusic() {
        try {
            if (Gdx.files.internal("ui/sounds/lose.mp3").exists()) {
                loseMusic = Gdx.audio.newMusic(Gdx.files.internal("ui/sounds/lose.mp3"));
                loseMusic.setLooping(true);
                loseMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Failed to load lose.mp3", e);
        }
    }

    private void layoutElements() {
        float sw = Gdx.graphics.getWidth();

        buttonW = 500f;
        buttonH = 150f;
        buttonX = (sw - buttonW) * 0.5f;
        buttonY = 80f;

        float baseW = 400f;
        float baseH = 100f;
        tierNameW = baseW * 1.75f;
        tierNameH = baseH * 1.75f;
        tierNameX = (sw - tierNameW) * 0.5f;
        tierNameY = buttonY + buttonH + 50f;
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

        drawTierName();
        drawButton();

        batch.end();
    }

    private void drawTierName() {
        if (tierNameTexture != null) {
            batch.draw(tierNameTexture, tierNameX, tierNameY, tierNameW, tierNameH);
        }
    }

    private void drawButton() {
        Texture buttonTex = buttonHovered ? buttonOnTexture : buttonOffTexture;
        if (buttonTex != null) {
            batch.draw(buttonTex, buttonX, buttonY, buttonW, buttonH);
        }
    }

    private void onReturnClicked() {
        try {
            Gdx.app.log("GameOverScreen", "onReturnClicked() START");
            if (loseMusic != null) {
                Gdx.app.log("GameOverScreen", "stopping music");
                loseMusic.stop();
                Gdx.app.log("GameOverScreen", "music stopped");
            }
            Gdx.app.log("GameOverScreen", "resetting game state");
            GameStateManager.getInstance().resetForNewRun();
            Gdx.app.log("GameOverScreen", "game state reset, calling showMainMenu");
            UiManager.getInstance().showMainMenu();
            Gdx.app.log("GameOverScreen", "showMainMenu() completed");
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Error returning to menu", e);
            e.printStackTrace();
        }
    }

    @Override
    public void resize(int w, int h) {
        layoutElements();
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.app.log("GameOverScreen", "hide() called");
        if (loseMusic != null) {
            loseMusic.stop();
        }
    }

    @Override
    public void dispose() {
        if (loseMusic != null) {
            loseMusic.stop();
            loseMusic.dispose();
        }
        if (batch != null) batch.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (tierNameTexture != null) tierNameTexture.dispose();
        if (buttonOffTexture != null) buttonOffTexture.dispose();
        if (buttonOnTexture != null) buttonOnTexture.dispose();
    }
}
