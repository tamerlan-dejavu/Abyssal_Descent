package com.abyssaldescent.ui.feedback;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

public class ScreenFlash {

    public enum FlashType { RED, WHITE, BLACK }

    public static final class ScreenFlashParams {
        public final FlashType type;
        public final float     duration;

        public ScreenFlashParams(FlashType type, float duration) {
            this.type     = type;
            this.duration = duration;
        }
    }

    private static final float DEFAULT_DURATION = 0.35f;

    private float     timer    = 0f;
    private float     duration = DEFAULT_DURATION;
    private FlashType type     = FlashType.RED;

    private final ShapeRenderer      shapeRenderer;
    private final OrthographicCamera camera;

    public ScreenFlash(TypedEventBus eventBus) {
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        eventBus.subscribe(TypedEvent.Type.PLAYER_DAMAGED, event -> {
            trigger(FlashType.RED, 0.4f);
        });

        eventBus.subscribe(TypedEvent.Type.SCREEN_FLASH, event -> {
            Object payload = event.getPayload();
            if (payload instanceof ScreenFlashParams) {
                ScreenFlashParams params = (ScreenFlashParams) payload;
                trigger(params.type, params.duration);
            }
        });
    }

    public void trigger(FlashType flashType, float durationSec) {
        this.type     = flashType;
        this.duration = durationSec;
        this.timer    = durationSec;
    }

    public void update(float delta) {
        if (timer > 0) timer -= delta;
    }

    public void render() {
        if (timer <= 0) return;

        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();
        float ratio = timer / duration;
        float alpha = ratio * 0.55f;

        Color color;
        switch (type) {
            case WHITE: color = new Color(1.0f, 1.0f, 1.0f, alpha); break;
            case BLACK: color = new Color(0.0f, 0.0f, 0.0f, alpha); break;
            default:    color = new Color(0.85f, 0.05f, 0.05f, alpha); break;
        }

        camera.update();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(0, 0, W, H);
        shapeRenderer.end();
    }

    public void resize(int w, int h) {
        camera.setToOrtho(false, w, h);
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
