package com.abyssaldescent.ui.screen;

import com.abyssaldescent.config.DifficultySettings;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Arrays;
import java.util.List;

/**
 * Full-screen difficulty selection overlay.
 *
 * <p>The overlay fills the entire screen. MainMenuScreen draws the background
 * texture directly — this class only owns button layout and input.
 *
 * <h3>Asset slots  (all 500×150 px, all optional)</h3>
 * <pre>
 *   ui/overlays/difficulty_bg.png     — full-screen background (any resolution)
 *
 *   ui/overlays/easy_idle.png         — Easy   button  idle state
 *   ui/overlays/easy_glow.png         — Easy   button  active/hover state
 *
 *   ui/overlays/normal_idle.png       — Normal button  idle state
 *   ui/overlays/normal_glow.png       — Normal button  active/hover state
 *
 *   ui/overlays/hard_idle.png         — Hard   button  idle state
 *   ui/overlays/hard_glow.png         — Hard   button  active/hover state
 *
 *   ui/overlays/back_idle.png         — Back   button  idle state  (bottom-right)
 *   ui/overlays/back_glow.png         — Back   button  active/hover state
 * </pre>
 *
 * <p>Legacy names {@code _off} / {@code _on} are tried as fallbacks.
 * If neither exists the button is shape-rendered automatically.
 *
 * <h3>Button layout (screen-relative)</h3>
 * <ul>
 *   <li>Easy / Normal / Hard — centred horizontally, at 42 % screen height.</li>
 *   <li>Back — bottom-right corner, 40 px margin from screen edges.</li>
 * </ul>
 */
class DifficultyOverlay extends MenuOverlay {

    static final float BTN_W    = 500f;
    static final float BTN_H    = 150f;
    static final float BTN_GAP  = 60f;
    static final float MARGIN   = 40f;

    private final Texture easyIdle,   easyGlow;
    private final Texture normalIdle, normalGlow;
    private final Texture hardIdle,   hardGlow;
    private final Texture backIdle,   backGlow;

    private List<MenuButton> diffBtns;
    private MenuButton       backBtn;

    DifficultyOverlay(Texture panelTex,
                      Texture easyIdle,   Texture easyGlow,
                      Texture normalIdle, Texture normalGlow,
                      Texture hardIdle,   Texture hardGlow,
                      Texture backIdle,   Texture backGlow,
                      Runnable onClose) {
        super(panelTex, onClose);
        this.easyIdle   = easyIdle;   this.easyGlow   = easyGlow;
        this.normalIdle = normalIdle; this.normalGlow = normalGlow;
        this.hardIdle   = hardIdle;   this.hardGlow   = hardGlow;
        this.backIdle   = backIdle;   this.backGlow   = backGlow;
    }

    // ── layout ────────────────────────────────────────────────────────────────

    /**
     * Call with {@code (0, 0, screenW, screenH)} — the overlay is full-screen.
     */
    @Override
    void rebuildLayout(float px, float py, float pw, float ph) {
        panelX = px; panelY = py; panelW = pw; panelH = ph;

        // ── Difficulty buttons — horizontal row, centred, at 42 % screen height ──
        float totalW = 3 * BTN_W + 2 * BTN_GAP;
        float startX = px + (pw - totalW) / 2f;
        float btnY   = py + ph * 0.42f - BTN_H / 2f;

        MenuButton easyBtn = new MenuButton(
                "Easy", startX, btnY, BTN_W, BTN_H,
                () -> startGame(DifficultySettings.EASY));
        if (easyIdle != null) easyBtn.setTextures(easyIdle, easyGlow);

        MenuButton normalBtn = new MenuButton(
                "Normal", startX + BTN_W + BTN_GAP, btnY, BTN_W, BTN_H,
                () -> startGame(DifficultySettings.NORMAL));
        if (normalIdle != null) normalBtn.setTextures(normalIdle, normalGlow);

        MenuButton hardBtn = new MenuButton(
                "Hard", startX + 2 * (BTN_W + BTN_GAP), btnY, BTN_W, BTN_H,
                () -> startGame(DifficultySettings.HARD));
        if (hardIdle != null) hardBtn.setTextures(hardIdle, hardGlow);

        // ── Back button — bottom-right corner ─────────────────────────────────
        float backX = px + pw - BTN_W - MARGIN;
        float backY = py + MARGIN;
        backBtn = new MenuButton("Back", backX, backY, BTN_W, BTN_H, onClose);
        if (backIdle != null) backBtn.setTextures(backIdle, backGlow);

        diffBtns = Arrays.asList(easyBtn, normalBtn, hardBtn);
    }

    // ── input ─────────────────────────────────────────────────────────────────

    @Override
    void update(float mx, float my) {
        if (diffBtns == null) return;
        for (MenuButton btn : diffBtns) btn.update(mx, my);
        backBtn.update(mx, my);
    }

    @Override
    boolean handleClick(float wx, float wy) {
        if (diffBtns == null) return false;
        if (backBtn.handleClick(wx, wy)) return true;
        for (MenuButton btn : diffBtns) {
            if (btn.handleClick(wx, wy)) return true;
        }
        return false;
    }

    // ── render ────────────────────────────────────────────────────────────────

    /**
     * Background is drawn by MainMenuScreen directly — nothing to do here.
     */
    @Override
    void renderFallbackBackground(ShapeRenderer shapes) {}

    @Override
    void renderPanelTexture(SpriteBatch batch) {}

    @Override
    void renderInteractiveShapes(ShapeRenderer shapes) {
        if (diffBtns == null) return;
        for (MenuButton btn : diffBtns) btn.renderBackground(shapes);
        backBtn.renderBackground(shapes);
    }

    @Override
    void renderLabels(SpriteBatch batch, BitmapFont font) {
        if (diffBtns == null) return;

        font.getData().setScale(3f);
        for (MenuButton btn : diffBtns) {
            btn.renderTexture(batch);
            btn.renderLabel(batch, font);
        }

        font.getData().setScale(3f);
        backBtn.renderTexture(batch);
        backBtn.renderLabel(batch, font);

        // Fallback title shown only when no background texture was loaded
        if (panelTex == null) {
            font.setColor(0.85f, 0.75f, 1f, 1f);
            String title = "SELECT DIFFICULTY";
            GlyphLayout gl = new GlyphLayout(font, title);
            font.draw(batch, title,
                    panelX + (panelW - gl.width) / 2f,
                    panelY + panelH * 0.88f);
        }
        font.getData().setScale(3f);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private void startGame(DifficultySettings diff) {
        onClose.run();
        UiManager.getInstance().startNewGame(diff);
    }
}
