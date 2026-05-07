package com.abyssaldescent.ui.hud;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.RespawnUsedEvent;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Displays remaining respawns using small filled squares (red = alive, dark = used). */
public final class RespawnCounterWidget {

    private static final float ICON  = 12f;
    private static final float GAP   = 6f;
    static final float H              = 28f;
    private static final float PAD   = 4f;
    private static final int   MAX_DEFAULT = 3;

    private int remaining = MAX_DEFAULT;
    private int max       = MAX_DEFAULT;
    private final BitmapFont font;
    private final EventListener<RespawnUsedEvent> listener = this::onRespawnUsed;

    public RespawnCounterWidget(BitmapFont font) {
        this.font = font;
        EventBus.getInstance().subscribe(RespawnUsedEvent.class, listener);
    }

    private void onRespawnUsed(RespawnUsedEvent e) {
        remaining = e.getRemainingRespawns();
        max       = e.getMaxRespawns();
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        float totalW = max * ICON + (max - 1) * GAP + 8f /* label gap */ + 60f /* "LIVES " */;

        shapes.setColor(0.1f, 0.1f, 0.1f, 0.85f);
        shapes.rect(x - PAD, y - PAD, totalW + PAD * 2, H + PAD * 2);

        // draw life icons as small filled squares
        float ix = x + 66f;
        float iy = y + (H - ICON) / 2f;
        for (int i = 0; i < max; i++) {
            if (i < remaining) {
                shapes.setColor(1f, 0.267f, 0.267f, 1f);  // #FF4444
            } else {
                shapes.setColor(0.25f, 0.05f, 0.05f, 1f);
            }
            shapes.rect(ix + i * (ICON + GAP), iy, ICON, ICON);
        }
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        font.getData().setScale(1.6f);
        font.setColor(1f, 0.267f, 0.267f, 1f);  // #FF4444
        font.draw(batch, "LIVES", x + 6f, y + H);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(RespawnUsedEvent.class, listener);
    }
}
