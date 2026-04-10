package com.abyssaldescent.render;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Controls the {@link OrthographicCamera} so it smoothly follows Karin.
 *
 * <p>Features:
 * <ul>
 *   <li>Lerp-based smooth tracking toward the target position.</li>
 *   <li>Optional world bounds clamping to prevent showing areas outside the map.</li>
 *   <li>Configurable zoom level for pixel-art rendering (16x16 tiles).</li>
 * </ul>
 *
 * <p>Usage in the game loop:
 * <pre>
 *   cameraController.setTarget(player.getX(), player.getY());
 *   cameraController.update(deltaTime);
 *   batch.setProjectionMatrix(cameraController.getCamera().combined);
 * </pre>
 */
public final class CameraController {

    /** Default smoothing factor — higher = snappier tracking. */
    private static final float DEFAULT_LERP_FACTOR = 5f;

    private final OrthographicCamera camera;
    private final Vector2 target = new Vector2();

    private float lerpFactor = DEFAULT_LERP_FACTOR;

    /** Base viewport height in world units — preserved across resizes. */
    private final float baseViewportHeight;

    // World bounds (0 = unbounded)
    private float worldWidth;
    private float worldHeight;
    private boolean hasBounds;

    public CameraController(float viewportWidth, float viewportHeight) {
        this.baseViewportHeight = viewportHeight;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
    }

    /**
     * Sets the position the camera should follow (typically Karin's position in world units).
     */
    public void setTarget(float x, float y) {
        target.set(x, y);
    }

    /**
     * Smoothly moves the camera toward the target and applies bounds clamping.
     * Call once per frame.
     *
     * @param deltaTime frame delta in seconds
     */
    public void update(float deltaTime) {
        float alpha = 1f - (float) Math.exp(-lerpFactor * deltaTime);

        camera.position.x += (target.x - camera.position.x) * alpha;
        camera.position.y += (target.y - camera.position.y) * alpha;

        if (hasBounds) {
            clampToBounds();
        }

        camera.update();
    }

    /**
     * Instantly snaps the camera to the target position without interpolation.
     * Useful for floor transitions or initial placement.
     */
    public void snapToTarget() {
        camera.position.x = target.x;
        camera.position.y = target.y;

        if (hasBounds) {
            clampToBounds();
        }

        camera.update();
    }

    /**
     * Defines the rectangular world bounds. The camera viewport will not
     * reveal areas outside [0, 0] .. [worldWidth, worldHeight].
     *
     * @param worldWidth  total width of the current floor in world units
     * @param worldHeight total height of the current floor in world units
     */
    public void setWorldBounds(float worldWidth, float worldHeight) {
        if (worldWidth <= 0 || worldHeight <= 0) {
            throw new IllegalArgumentException(
                "World bounds must be positive, got " + worldWidth + "x" + worldHeight);
        }
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.hasBounds = true;
    }

    /** Removes world bounds — the camera can move freely. */
    public void clearWorldBounds() {
        this.hasBounds = false;
        this.worldWidth = 0;
        this.worldHeight = 0;
    }

    /**
     * Sets the interpolation speed factor. Higher values make the camera
     * track more tightly; lower values create a lazier follow.
     *
     * @param factor positive lerp speed (default {@value #DEFAULT_LERP_FACTOR})
     */
    public void setLerpFactor(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("lerpFactor must be > 0, got " + factor);
        }
        this.lerpFactor = factor;
    }

    public float getLerpFactor() {
        return lerpFactor;
    }

    /** Adjusts the camera zoom. 1.0 = default, <1.0 = zoom in, >1.0 = zoom out. */
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

    /**
     * Called when the window is resized. Recalculates the viewport in world units
     * so the visible area keeps the base height and adjusts width to the new aspect ratio.
     */
    public void resize(int screenWidth, int screenHeight) {
        if (screenHeight == 0) return;
        float aspectRatio = (float) screenWidth / screenHeight;
        camera.viewportHeight = baseViewportHeight;
        camera.viewportWidth = baseViewportHeight * aspectRatio;
        camera.update();
    }

    // ── Internal ───────────────────────────────────────────────────────────

    private void clampToBounds() {
        float halfW = camera.viewportWidth * camera.zoom * 0.5f;
        float halfH = camera.viewportHeight * camera.zoom * 0.5f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfW, worldWidth - halfW);
        camera.position.y = MathUtils.clamp(camera.position.y, halfH, worldHeight - halfH);
    }
}
