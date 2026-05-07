package com.abyssaldescent.ui.hud;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.KeyPickedUpEvent;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Displays the current key count (e.g. "KEY 0/1") — updates via KeyPickedUpEvent. */
public final class KeyCounterWidget {

    static final float W   = 160f;
    static final float H   = 28f;
    private static final float PAD = 4f;

    private int currentKeys = 0;
    private int totalKeys   = 1;
    private final BitmapFont font;
    private final EventListener<KeyPickedUpEvent> listener = this::onKeyPickup;

    public KeyCounterWidget(BitmapFont font) {
        this.font = font;
        EventBus.getInstance().subscribe(KeyPickedUpEvent.class, listener);
    }

    private void onKeyPickup(KeyPickedUpEvent e) {
        currentKeys = e.getCurrentKeys();
        totalKeys   = e.getTotalKeys();
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        shapes.setColor(0.1f, 0.1f, 0.1f, 0.85f);
        shapes.rect(x - PAD, y - PAD, W + PAD * 2, H + PAD * 2);
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        font.getData().setScale(1.6f);
        font.setColor(1f, 1f, 0f, 1f);  // #FFFF00
        font.draw(batch, "KEY  " + currentKeys + " / " + totalKeys, x + 6f, y + H);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(KeyPickedUpEvent.class, listener);
    }
}
