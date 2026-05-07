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
    private Music       loseMusic;

    private Texture backgroundTexture;
    private Texture tierNameTexture;
    private Texture buttonOffTexture;
    private Texture buttonOnTexture;

    private float   buttonX, buttonY;
    private float   buttonW = 500f;
    private float   buttonH = 150f;
    private float   tierNameX, tierNameY, tierNameW, tierNameH;

    private boolean buttonHovered = false;
    private boolean navigating    = false;

    public GameOverScreen(GameOverStats stats) {
        this.stats = stats;
    }

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void show() {
        batch = new SpriteBatch();
        backgroundTexture = loadTexture("ui/backgrounds/death-screen.jpg");
        tierNameTexture   = loadTexture(tierPath(stats.floorReached));
        buttonOffTexture  = loadTexture("ui/buttons/back_to_menu_off.png");
        buttonOnTexture   = loadTexture("ui/buttons/back_to_menu_on.png");
        loadMusic();
        layout();
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (navigating) return;

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();

        buttonHovered = mx >= buttonX && mx <= buttonX + buttonW
                     && my >= buttonY && my <= buttonY + buttonH;

        boolean clicked = (Gdx.input.justTouched() && buttonHovered)
                       || Gdx.input.isKeyJustPressed(Input.Keys.ENTER);

        if (clicked) {
            navigating = true;
            stopMusic();
            GameStateManager.getInstance().resetForNewRun();
            UiManager.getInstance().showMainMenu();
            return;
        }

        ScreenUtils.clear(0, 0, 0, 1f);
        batch.begin();
        if (backgroundTexture != null) batch.draw(backgroundTexture, 0, 0, sw, sh);
        if (tierNameTexture   != null) batch.draw(tierNameTexture,   tierNameX, tierNameY, tierNameW, tierNameH);
        Texture btn = buttonHovered && buttonOnTexture != null ? buttonOnTexture : buttonOffTexture;
        if (btn != null) batch.draw(btn, buttonX, buttonY, buttonW, buttonH);
        batch.end();
    }

    @Override
    public void resize(int w, int h) { layout(); }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   { stopMusic(); }

    @Override
    public void dispose() {
        stopMusic();
        if (loseMusic         != null) { loseMusic.dispose();         loseMusic         = null; }
        if (batch             != null) { batch.dispose();             batch             = null; }
        if (backgroundTexture != null) { backgroundTexture.dispose(); backgroundTexture = null; }
        if (tierNameTexture   != null) { tierNameTexture.dispose();   tierNameTexture   = null; }
        if (buttonOffTexture  != null) { buttonOffTexture.dispose();  buttonOffTexture  = null; }
        if (buttonOnTexture   != null) { buttonOnTexture.dispose();   buttonOnTexture   = null; }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void layout() {
        float sw = Gdx.graphics.getWidth();
        buttonW = 500f; buttonH = 150f;
        buttonX = (sw - buttonW) * 0.5f;
        buttonY = 80f;
        tierNameW = 750f; tierNameH = 175f;
        tierNameX = (sw - tierNameW) * 0.5f;
        tierNameY = buttonY + buttonH + 60f;
    }

    private void stopMusic() {
        if (loseMusic != null && loseMusic.isPlaying()) loseMusic.stop();
    }

    private void loadMusic() {
        try {
            if (Gdx.files.internal("ui/sounds/lose.mp3").exists()) {
                loseMusic = Gdx.audio.newMusic(Gdx.files.internal("ui/sounds/lose.mp3"));
                loseMusic.setLooping(true);
                loseMusic.play();
            }
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "music load failed", e);
        }
    }

    private Texture loadTexture(String path) {
        try {
            if (Gdx.files.internal(path).exists()) return new Texture(path);
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "texture load failed: " + path, e);
        }
        return null;
    }

    private static String tierPath(int floor) {
        switch (floor) {
            case 2:  return "ui/buttons/flooded_catacombs.png";
            case 3:  return "ui/buttons/maltarions_abyss.png";
            default: return "ui/buttons/upper_ruins.png";
        }
    }
}
