package com.abyssaldescent.ui.hud;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.TierChangedEvent;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Shows the current floor number — subscribes to TierChangedEvent. */
public final class LevelIndicator {

    static final float W   = 220f;
    static final float H   = 28f;
    private static final float PAD = 4f;

    private int floor;
    private final BitmapFont font;
    private final EventListener<TierChangedEvent> listener = this::onTierChanged;

    public LevelIndicator(BitmapFont font) {
        this.font  = font;
        this.floor = GameStateManager.getInstance().getFloorNumber();
        EventBus.getInstance().subscribe(TierChangedEvent.class, listener);
    }

    private void onTierChanged(TierChangedEvent e) {
        floor = e.getNewTier();
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        shapes.setColor(0.1f, 0.1f, 0.1f, 0.85f);
        shapes.rect(x - PAD, y - PAD, W + PAD * 2, H + PAD * 2);
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        font.getData().setScale(2.0f);
        font.setColor(0f, 0.8f, 1f, 1f);  // #00CCFF
        font.draw(batch, "FLOOR  " + floor, x + 6f, y + H + 4f);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(TierChangedEvent.class, listener);
    }
}
