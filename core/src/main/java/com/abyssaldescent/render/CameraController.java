package com.abyssaldescent.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


public final class CameraController {

    private static final float DEFAULT_LERP_FACTOR = 5f;

    private final OrthographicCamera camera;
    private final Vector2 target = new Vector2();

    private float lerpFactor = DEFAULT_LERP_FACTOR;

    private final float baseViewportHeight;

    private float worldWidth;
    private float worldHeight;
    private boolean hasBounds;

    public CameraController(float viewportWidth, float viewportHeight) {
        this.baseViewportHeight = viewportHeight;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
    }

    public void setTarget(float x, float y) {
        target.set(x, y);
    }

    public void update(float deltaTime) {
        float alpha = 1f - (float) Math.exp(-lerpFactor * deltaTime);

        camera.position.x += (target.x - camera.position.x) * alpha;
        camera.position.y += (target.y - camera.position.y) * alpha;

        if (hasBounds) {
            clampToBounds();
        }

        camera.update();
    }

    
    public void snapToTarget() {
        camera.position.x = target.x;
        camera.position.y = target.y;

        if (hasBounds) {
            clampToBounds();
        }

        camera.update();
    }

   
    public void setWorldBounds(float worldWidth, float worldHeight) {
        if (worldWidth <= 0 || worldHeight <= 0) {
            throw new IllegalArgumentException(
                "World bounds must be positive, got " + worldWidth + "x" + worldHeight);
        }
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.hasBounds = true;
    }

    public void clearWorldBounds() {
        this.hasBounds = false;
        this.worldWidth = 0;
        this.worldHeight = 0;
    }

    
    public void setLerpFactor(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("lerpFactor must be > 0, got " + factor);
        }
        this.lerpFactor = factor;
    }

    public float getLerpFactor() {
        return lerpFactor;
    }
    public void setZoom(float zoom) {
        if (zoom <= 0) {
            throw new IllegalArgumentException("zoom must be > 0, got " + zoom);
        }
        camera.zoom = zoom;
    }

    public float getZoom() {
        return camera.zoom;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Vector2 getTarget() {
        return target;
    }

    public void resize(int screenWidth, int screenHeight) {
        if (screenHeight == 0) return;
        float aspectRatio = (float) screenWidth / screenHeight;
        camera.viewportHeight = baseViewportHeight;
        camera.viewportWidth = baseViewportHeight * aspectRatio;
        camera.update();
    }

    private void clampToBounds() {
        float halfW = camera.viewportWidth * camera.zoom * 0.5f;
        float halfH = camera.viewportHeight * camera.zoom * 0.5f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfW, worldWidth - halfW);
        camera.position.y = MathUtils.clamp(camera.position.y, halfH, worldHeight - halfH);
    }
}