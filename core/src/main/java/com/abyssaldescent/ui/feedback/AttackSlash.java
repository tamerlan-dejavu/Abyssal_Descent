package com.abyssaldescent.ui.feedback;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

import java.util.ArrayList;
import java.util.List;

public class AttackSlash {

    private static final float SLASH_DURATION = 0.15f;
    private static final int   LINE_COUNT     = 5;
    private static final float SLASH_LENGTH   = 28f;
    private static final float SLASH_SPREAD   = 18f;

    public static final class AttackInfo {
        public final float worldX;
        public final float worldY;
        public final float directionX;

        public AttackInfo(float worldX, float worldY, float directionX) {
            this.worldX     = worldX;
            this.worldY     = worldY;
            this.directionX = directionX;
        }
    }

    private static class Slash {
        float cx, cy, dirX, timer;
        boolean active;

        void init(float cx, float cy, float dirX) {
            this.cx = cx; this.cy = cy; this.dirX = dirX;
            this.timer = SLASH_DURATION;
            this.active = true;
        }
    }

    private final List<Slash> slashes = new ArrayList<Slash>();
    private final ShapeRenderer shapeRenderer;

    public AttackSlash(TypedEventBus eventBus, ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
        for (int i = 0; i < 8; i++) slashes.add(new Slash());

        eventBus.subscribe(TypedEvent.Type.PLAYER_ATTACK, event -> {
            Object payload = event.getPayload();
            if (payload instanceof AttackInfo) {
                AttackInfo info = (AttackInfo) payload;
                spawn(info.worldX, info.worldY, info.directionX);
            }
        });
    }

    public void update(float delta) {
        for (Slash s : slashes) {
            if (s.active) {
                s.timer -= delta;
                if (s.timer <= 0) s.active = false;
            }
        }
    }

    public void render() {
        for (Slash s : slashes) {
            if (!s.active) continue;
            float ratio = s.timer / SLASH_DURATION;
            float alpha = ratio;
            for (int i = 0; i < LINE_COUNT; i++) {
                float t = (float) i / (LINE_COUNT - 1);
                float angleDeg  = -SLASH_SPREAD / 2f + t * SLASH_SPREAD;
                float angleRad  = (float) Math.toRadians(angleDeg);
                float baseAngle = s.dirX > 0 ? 0f : (float) Math.PI;
                float totalAngle = baseAngle + angleRad;
                float endX = s.cx + (float) Math.cos(totalAngle) * SLASH_LENGTH * ratio;
                float endY = s.cy + (float) Math.sin(totalAngle) * SLASH_LENGTH * ratio;
                float r = 1f, g = 0.7f + ratio * 0.3f, b = ratio * 0.5f;
                shapeRenderer.setColor(new Color(r, g, b, alpha));
                shapeRenderer.line(s.cx, s.cy, endX, endY);
            }
        }
    }

    private void spawn(float cx, float cy, float dirX) {
        for (Slash s : slashes) {
            if (!s.active) { s.init(cx, cy, dirX); return; }
        }
    }
}
