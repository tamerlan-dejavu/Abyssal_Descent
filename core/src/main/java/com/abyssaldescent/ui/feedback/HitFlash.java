package com.abyssaldescent.ui.feedback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

public class HitFlash {

    private static final float FLASH_DURATION = 0.12f;
    private static final float BLINK_PERIOD   = 0.08f;
    private static final float IFRAME_TOTAL   = 0.5f;

    private float   flashTimer   = 0f;
    private float   iframeTimer  = 0f;
    private float   blinkTimer   = 0f;
    private boolean blinkVisible = true;

    public HitFlash(TypedEventBus eventBus) {
        eventBus.subscribe(TypedEvent.Type.PLAYER_DAMAGED, event -> {
            flashTimer   = FLASH_DURATION;
            iframeTimer  = IFRAME_TOTAL;
            blinkVisible = true;
            blinkTimer   = 0f;
        });
    }

    public void update(float delta) {
        if (flashTimer  > 0) flashTimer  -= delta;
        if (iframeTimer > 0) {
            iframeTimer -= delta;
            blinkTimer  += delta;
            if (blinkTimer >= BLINK_PERIOD) {
                blinkTimer   = 0f;
                blinkVisible = !blinkVisible;
            }
        } else {
            blinkVisible = true;
        }
    }

    public boolean applyColor(SpriteBatch batch) {
        if (!blinkVisible) return false;
        if (flashTimer > 0) {
            batch.setColor(1f, 1f, 1f, 1f);
        } else {
            batch.setColor(Color.WHITE);
        }
        return true;
    }

    public void resetColor(SpriteBatch batch) {
        batch.setColor(Color.WHITE);
    }

    public boolean isIframeActive() { return iframeTimer > 0; }
}
