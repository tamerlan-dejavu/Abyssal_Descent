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
 * Difficulty selection panel overlay.
 *
 * <p>Asset slots (all optional — shape fallback used when absent):
 * <pre>
 *   ui/overlays/difficulty_panel.png — 1800×900 panel background
 *   ui/overlays/easy_off.png         — Easy button idle    (500×150 px)
 *   ui/overlays/easy_on.png          — Easy button hovered (500×150 px)
 *   ui/overlays/normal_off.png       — Normal button idle
 *   ui/overlays/normal_on.png        — Normal button hovered
 *   ui/overlays/hard_off.png         — Hard button idle
 *   ui/overlays/hard_on.png          — Hard button hovered
 * </pre>
 */
class DifficultyOverlay extends MenuOverlay {

    private static final float DIFF_BTN_W = 500f;
    private static final float DIFF_BTN_H = 150f;
    private static final float DIFF_GAP   = 40f;

    private final Texture easyOff, easyOn;
    private final Texture normalOff, normalOn;
    private final Texture hardOff, hardOn;

    private List<MenuButton> diffBtns;
    private MenuButton       backBtn;

    DifficultyOverlay(Texture panelTex,
                      Texture easyOff,   Texture easyOn,
                      Texture normalOff, Texture normalOn,
                      Texture hardOff,   Texture hardOn,
                      Runnable onClose) {
        super(panelTex, onClose);
        this.easyOff   = easyOff;   this.easyOn   = easyOn;
        this.normalOff = normalOff; this.normalOn = normalOn;
        this.hardOff   = hardOff;   this.hardOn   = hardOn;
    }

    // ── layout ────────────────────────────────────────────────────────────────

    @Override
    void rebuildLayout(float px, float py, float pw, float ph) {
        panelX = px; panelY = py; panelW = pw; panelH = ph;

        float totalW = 3 * DIFF_BTN_W + 2 * DIFF_GAP;
        float startX = px + (pw - totalW) / 2f;
        float btnY   = py + ph * 0.42f - DIFF_BTN_H / 2f;

        MenuButton easyBtn = new MenuButton(
                "Easy", startX, btnY, DIFF_BTN_W, DIFF_BTN_H,
                () -> startGame(DifficultySettings.EASY));
        if (easyOff != null) easyBtn.setTextures(easyOff, easyOn);

        MenuButton normalBtn = new MenuButton(
                "Normal", startX + DIFF_BTN_W + DIFF_GAP, btnY, DIFF_BTN_W, DIFF_BTN_H,
                () -> startGame(DifficultySettings.NORMAL));
        if (normalOff != null) normalBtn.setTextures(normalOff, normalOn);

        MenuButton hardBtn = new MenuButton(
                "Hard", startX + 2 * (DIFF_BTN_W + DIFF_GAP), btnY, DIFF_BTN_W, DIFF_BTN_H,
                () -> startGame(DifficultySettings.HARD));
        if (hardOff != null) hardBtn.setTextures(hardOff, hardOn);

        backBtn  = new MenuButton("Back", px + 30f, py + 30f, 150f, 50f, onClose);
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

        font.getData().setScale(2f);
        backBtn.renderTexture(batch);
        backBtn.renderLabel(batch, font);

        // Fallback title when no panel texture
        if (panelTex == null) {
            font.getData().setScale(3f);
            font.setColor(0.85f, 0.75f, 1f, 1f);
            String title = "SELECT DIFFICULTY";
            GlyphLayout gl = new GlyphLayout(font, title);
            font.draw(batch, title, panelX + (panelW - gl.width) / 2f, panelY + panelH * 0.88f);
        }
        font.getData().setScale(3f); // reset for caller
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private void startGame(DifficultySettings diff) {
        onClose.run();
        UiManager.getInstance().startNewGame(diff);
    }
}
