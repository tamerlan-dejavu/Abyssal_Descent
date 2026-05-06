package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Base class for full-screen panel overlays (Settings, Difficulty).
 *
 * <p>MainMenuScreen drives a 5-phase render pipeline per frame when an overlay is active:
 * <ol>
 *   <li>Batch — blurred background (managed by MainMenuScreen).</li>
 *   <li>Shapes (blend on) — dim + {@link #renderFallbackBackground} (when no panel texture).</li>
 *   <li>Batch — {@link #renderPanelTexture} (when panel texture exists).</li>
 *   <li>Shapes (blend on) — {@link #renderInteractiveShapes} (sliders, button fills, borders).</li>
 *   <li>Batch — {@link #renderLabels} (text, button labels).</li>
 * </ol>
 *
 * <p>Call {@link #rebuildLayout} whenever the panel origin or screen size changes.
 */
abstract class MenuOverlay {

    protected Texture  panelTex;
    protected Runnable onClose;

    protected float panelX, panelY, panelW, panelH;

    MenuOverlay(Texture panelTex, Runnable onClose) {
        this.panelTex = panelTex;
        this.onClose  = onClose;
    }

    // ── lifecycle ─────────────────────────────────────────────────────────────

    /** Recalculate all button / slider positions for the given panel rect. */
    abstract void rebuildLayout(float panelX, float panelY, float panelW, float panelH);

    /** Update hover state; call each frame with Y-up world-space mouse coords. */
    abstract void update(float mx, float my);

    /** Route a click; returns {@code true} if consumed. */
    abstract boolean handleClick(float wx, float wy);

    /** Route a drag (slider use); returns {@code true} if consumed. Default: no-op. */
    boolean handleDrag(float wx) { return false; }

    /** Release any active drag. */
    void stopDrag() {}

    // ── render phases ─────────────────────────────────────────────────────────

    /**
     * Phase 2 — shapes, blend on.
     * Draws a dark fallback panel rect + border when no panel texture is set.
     */
    void renderFallbackBackground(ShapeRenderer shapes) {
        if (panelTex != null) return;
        shapes.setColor(0.04f, 0.04f, 0.14f, 0.97f);
        shapes.rect(panelX, panelY, panelW, panelH);
        float t = 3f;
        shapes.setColor(0.35f, 0.25f, 0.55f, 1f);
        shapes.rect(panelX,               panelY + panelH - t, panelW, t);
        shapes.rect(panelX,               panelY,              panelW, t);
        shapes.rect(panelX,               panelY,              t,      panelH);
        shapes.rect(panelX + panelW - t,  panelY,              t,      panelH);
    }

    /**
     * Phase 3 — batch.
     * Draws the panel texture if available; no-op otherwise.
     */
    void renderPanelTexture(SpriteBatch batch) {
        if (panelTex != null) {
            batch.draw(panelTex, panelX, panelY, panelW, panelH);
        }
    }

    /**
     * Phase 4 — shapes, blend on.
     * Draws interactive elements: sliders, button fills, hover borders.
     */
    abstract void renderInteractiveShapes(ShapeRenderer shapes);

    /**
     * Phase 5 — batch.
     * Draws all text labels and button label text.
     */
    abstract void renderLabels(SpriteBatch batch, BitmapFont font);
}
