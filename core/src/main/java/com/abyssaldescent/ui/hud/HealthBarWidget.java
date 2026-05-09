package com.abyssaldescent.ui.hud;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.entity.player.PlayerSlot;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.HealthChangedEvent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Renders the player HP bar (Observer for HealthChangedEvent, polls GameStateManager as fallback). */
public final class HealthBarWidget {
    static final float BAR_W = 300f;
    static final float BAR_H = 28f;
    private static final float PAD = 4f;

    private int currentHp;
    private int maxHp;
    private final BitmapFont font;
    private final EventListener<HealthChangedEvent> listener = this::onHealthChanged;

    public HealthBarWidget(BitmapFont font) {
        this.font = font;
        syncFromGameState();
        EventBus.getInstance().subscribe(HealthChangedEvent.class, listener);
    }

    private void onHealthChanged(HealthChangedEvent e) {
        currentHp = e.getCurrentHp();
        maxHp     = e.getMaxHp();
    }

    /** Pull current HP directly from GameStateManager each frame so the bar is always fresh. */
    public void syncFromGameState() {
        PlayerSlot slot = GameStateManager.getInstance().getKarinSlot();
        currentHp = slot.getCurrentHp();
        maxHp     = slot.getCharacterType().getMaxHp();
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        float pct = maxHp > 0 ? (float) currentHp / maxHp : 0f;

        // dark background
        shapes.setColor(0.1f, 0.1f, 0.1f, 0.85f);
        shapes.rect(x - PAD, y - PAD, BAR_W + PAD * 2, BAR_H + PAD * 2);

        // empty part (dark red)
        shapes.setColor(0.3f, 0.05f, 0.05f, 1f);
        shapes.rect(x, y, BAR_W, BAR_H);

        // HP fill (#FF4444)
        shapes.setColor(1f, 0.267f, 0.267f, 1f);
        shapes.rect(x, y, BAR_W * pct, BAR_H);
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        font.getData().setScale(1.6f);
        font.setColor(Color.WHITE);
        font.draw(batch, "HP  " + currentHp + " / " + maxHp, x + 6, y + BAR_H);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(HealthChangedEvent.class, listener);
    }
}
