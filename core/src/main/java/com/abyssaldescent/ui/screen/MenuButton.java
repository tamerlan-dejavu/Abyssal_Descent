package com.abyssaldescent.ui.screen;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Reusable pixel-rendered button for all menu screens.
 *
 * <p>Render order per frame:
 * <ol>
 *   <li>Call {@link #update(float, float)} with current mouse position (world Y-up coords).</li>
 *   <li>Inside an active {@link ShapeRenderer} Filled pass → call {@link #renderBackground(ShapeRenderer)}.</li>
 *   <li>Inside an active {@link SpriteBatch} pass → call {@link #renderLabel(SpriteBatch, BitmapFont)}.</li>
 * </ol>
 *
 * <p>Palette (GDD §UI):
 * <ul>
 *   <li>Normal  bg  : #2a2a2a at 60 % alpha</li>
 *   <li>Hover   bg  : #3a3a3a at 80 % alpha + yellow border</li>
 *   <li>Disabled bg  : dimmed at 40 % alpha, grey text</li>
 * </ul>
 */
public class MenuButton {

    private static final float BORDER = 2f;

    private final String   label;
    private final float    x, y, width, height;
    private final Runnable onClickAction;

    private boolean hovered  = false;
    private boolean enabled  = true;
    private float   alpha    = 1f;

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
     * Updates the hover state. Call every frame with screen-to-world converted coordinates
     * (i.e. {@code mouseY = Gdx.graphics.getHeight() - Gdx.input.getY()}).
     */
    public void update(float mouseX, float mouseY) {
        hovered = enabled && contains(mouseX, mouseY);
    }

    /**
     * Processes a click at the given world-space position.
     * @return {@code true} if the click was consumed by this button.
     */
    public boolean handleClick(float mouseX, float mouseY) {
        if (!enabled || !contains(mouseX, mouseY)) return false;
        if (onClickAction != null) onClickAction.run();
        return true;
    }

    /**
     * Shapes pass: filled background rectangle + yellow border rects when hovered.
     * Must be called inside {@code shapes.begin(ShapeType.Filled)}.
     */
    public void renderBackground(ShapeRenderer shapes) {
        float a = alpha;
        if (!enabled) {
            shapes.setColor(0.15f, 0.15f, 0.15f, 0.40f * a);
        } else if (hovered) {
            shapes.setColor(0.227f, 0.227f, 0.227f, 0.80f * a); // #3a3a3a
        } else {
            shapes.setColor(0.165f, 0.165f, 0.165f, 0.60f * a); // #2a2a2a
        }
        shapes.rect(x, y, width, height);

        if (hovered && enabled) {
            shapes.setColor(1f, 1f, 0f, a); // #FFFF00
            shapes.rect(x,               y + height - BORDER, width,  BORDER); // top
            shapes.rect(x,               y,                    width,  BORDER); // bottom
            shapes.rect(x,               y,                    BORDER, height); // left
            shapes.rect(x + width - BORDER, y,                BORDER, height); // right
        }
    }

    /**
     * Batch pass: horizontally and vertically centred label text.
     * Must be called inside {@code batch.begin()}.
     */
    public void renderLabel(SpriteBatch batch, BitmapFont font) {
        if (!enabled) {
            font.setColor(0.502f, 0.502f, 0.502f, alpha); // #808080
        } else if (hovered) {
            font.setColor(1f, 1f, 0f, alpha); // #FFFF00
        } else {
            font.setColor(1f, 1f, 1f, alpha); // #FFFFFF
        }
        glyphLayout.setText(font, label);
        float tx = x + (width  - glyphLayout.width)  * 0.5f;
        float ty = y + (height + glyphLayout.height) * 0.5f;
        font.draw(batch, glyphLayout, tx, ty);
    }

    // ── accessors ────────────────────────────────────────────────────────────

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
