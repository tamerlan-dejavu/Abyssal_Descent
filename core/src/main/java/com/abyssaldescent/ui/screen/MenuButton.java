package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Reusable button for all menu screens.
 *
 * <p>Two rendering modes:
 * <ul>
 *   <li><b>Texture mode</b> — call {@link #setTextures} once after construction.
 *       {@link #renderTexture} draws the sprite in the batch pass;
 *       {@link #renderBackground} becomes a no-op (border still drawn via shapes).</li>
 *   <li><b>Shape mode</b> (default) — {@link #renderBackground} draws a filled
 *       rectangle + yellow hover border via ShapeRenderer.</li>
 * </ul>
 *
 * <p>Frame order:
 * <ol>
 *   <li>{@link #update(float, float)} — update hover state (Y-up world coords).</li>
 *   <li>Shapes pass → {@link #renderBackground(ShapeRenderer)}.</li>
 *   <li>Batch pass  → {@link #renderTexture(SpriteBatch)} then {@link #renderLabel}.</li>
 * </ol>
 */
public class MenuButton {

    private static final float BORDER = 3f;

    private final String   label;
    private final float    x, y, width, height;
    private final Runnable onClickAction;

    private boolean hovered = false;
    private boolean enabled = true;
    private float   alpha   = 1f;

    private Texture normalTexture;
    private Texture hoverTexture;

    private final GlyphLayout glyphLayout = new GlyphLayout();

    public MenuButton(String label, float x, float y,
                      float width, float height, Runnable onClickAction) {
        this.label         = label;
        this.x             = x;
        this.y             = y;
        this.width         = width;
        this.height        = height;
        this.onClickAction = onClickAction;
    }

    /**
     * Enables texture-based rendering.
     * @param normal  texture for idle state (required)
     * @param hover   texture for hovered state; {@code null} applies a yellow tint instead
     */
    public void setTextures(Texture normal, Texture hover) {
        this.normalTexture = normal;
        this.hoverTexture  = hover;
    }

    public boolean hasTextures() { return normalTexture != null; }

    // ── per-frame update ──────────────────────────────────────────────────────

    /**
     * Updates hover state.
     * Pass mouse coordinates in world-space Y-up: {@code mouseY = Gdx.graphics.getHeight() - Gdx.input.getY()}.
     */
    public void update(float mouseX, float mouseY) {
        hovered = enabled && contains(mouseX, mouseY);
    }

    /**
     * Processes a click. Returns {@code true} if consumed.
     */
    public boolean handleClick(float mouseX, float mouseY) {
        if (!enabled || !contains(mouseX, mouseY)) return false;
        if (onClickAction != null) onClickAction.run();
        return true;
    }

    // ── rendering ─────────────────────────────────────────────────────────────

    /**
     * Shapes pass.
     * In texture mode: draws only the yellow hover border.
     * In shape mode: draws filled background + yellow hover border.
     * Must be called inside {@code shapes.begin(ShapeType.Filled)}.
     */
    public void renderBackground(ShapeRenderer shapes) {
        float a = alpha;
        if (!hasTextures()) {
            if (!enabled) {
                shapes.setColor(0.15f, 0.15f, 0.15f, 0.40f * a);
            } else if (hovered) {
                shapes.setColor(0.227f, 0.227f, 0.227f, 0.80f * a);
            } else {
                shapes.setColor(0.165f, 0.165f, 0.165f, 0.60f * a);
            }
            shapes.rect(x, y, width, height);
        }

        if (hovered && enabled) {
            shapes.setColor(1f, 1f, 0f, a);
            shapes.rect(x,                  y + height - BORDER, width,  BORDER);
            shapes.rect(x,                  y,                    width,  BORDER);
            shapes.rect(x,                  y,                    BORDER, height);
            shapes.rect(x + width - BORDER, y,                    BORDER, height);
        }
    }

    /**
     * Batch pass — draws the button texture (texture mode only, no-op otherwise).
     * Must be called inside {@code batch.begin()}.
     */
    public void renderTexture(SpriteBatch batch) {
        if (!hasTextures()) return;
        Texture tex = (hovered && enabled && hoverTexture != null) ? hoverTexture : normalTexture;
        if (!enabled) {
            batch.setColor(0.5f, 0.5f, 0.5f, alpha);
        } else if (hovered && hoverTexture == null) {
            batch.setColor(1f, 0.92f, 0.5f, alpha); // warm yellow tint when no hover tex
        } else {
            batch.setColor(1f, 1f, 1f, alpha);
        }
        batch.draw(tex, x, y, width, height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /**
     * Batch pass — draws the centred label.
     * Must be called inside {@code batch.begin()}.
     */
    public void renderLabel(SpriteBatch batch, BitmapFont font) {
        if (!enabled) {
            font.setColor(0.502f, 0.502f, 0.502f, alpha);
        } else if (hovered) {
            font.setColor(1f, 1f, 0f, alpha);
        } else {
            font.setColor(1f, 1f, 1f, alpha);
        }
        glyphLayout.setText(font, label);
        float tx = x + (width  - glyphLayout.width)  * 0.5f;
        float ty = y + (height + glyphLayout.height) * 0.5f;
        font.draw(batch, glyphLayout, tx, ty);
    }

    // ── accessors ─────────────────────────────────────────────────────────────

    public void    setEnabled(boolean enabled) { this.enabled = enabled; }
    public void    setAlpha(float alpha)       { this.alpha   = alpha;   }
    public boolean isEnabled()                 { return enabled; }
    public boolean isHovered()                 { return hovered; }
    public float   getX()                      { return x; }
    public float   getY()                      { return y; }
    public float   getWidth()                  { return width; }
    public float   getHeight()                 { return height; }

    private boolean contains(float mx, float my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }
}
