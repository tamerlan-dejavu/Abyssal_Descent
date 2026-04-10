package com.abyssaldescent.render;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.OrthographicCamera;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraControllerTest {

    private static final float VP_W = 20f;
    private static final float VP_H = 15f;
    private static final float EPSILON = 0.01f;

    private CameraController cam;

    @BeforeAll
    static void initGdx() {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = -1;
        new HeadlessApplication(new ApplicationAdapter() {}, config);
    }

    @BeforeEach
    void setUp() {
        cam = new CameraController(VP_W, VP_H);
    }

    // ── Construction ───────────────────────────────────────────────────────

    @Test
    void cameraCreatedWithCorrectViewport() {
        OrthographicCamera c = cam.getCamera();
        assertEquals(VP_W, c.viewportWidth, EPSILON);
        assertEquals(VP_H, c.viewportHeight, EPSILON);
    }

    @Test
    void defaultZoomIsOne() {
        assertEquals(1f, cam.getZoom(), EPSILON);
    }

    // ── snapToTarget ───────────────────────────────────────────────────────

    @Test
    void snapToTargetPositionsCameraExactly() {
        cam.setTarget(50, 30);
        cam.snapToTarget();

        assertEquals(50f, cam.getCamera().position.x, EPSILON);
        assertEquals(30f, cam.getCamera().position.y, EPSILON);
    }

    // ── Smooth follow (update) ─────────────────────────────────────────────

    @Test
    void updateMovesCameraTowardTarget() {
        cam.setTarget(0, 0);
        cam.snapToTarget();

        cam.setTarget(100, 0);
        cam.update(0.016f);

        float x = cam.getCamera().position.x;
        assertTrue(x > 0, "Camera should have moved right");
        assertTrue(x < 100, "Camera should not have reached target yet");
    }

    @Test
    void manyUpdatesConvergeOnTarget() {
        cam.setTarget(0, 0);
        cam.snapToTarget();

        cam.setTarget(50, 50);
        for (int i = 0; i < 300; i++) {
            cam.update(0.016f);
        }

        assertEquals(50f, cam.getCamera().position.x, 0.1f);
        assertEquals(50f, cam.getCamera().position.y, 0.1f);
    }

    // ── World bounds clamping ──────────────────────────────────────────────

    @Test
    void boundsClampCameraPosition() {
        cam.setWorldBounds(100, 100);
        cam.setTarget(-50, -50);
        cam.snapToTarget();

        float halfW = VP_W * 0.5f;
        float halfH = VP_H * 0.5f;
        assertTrue(cam.getCamera().position.x >= halfW - EPSILON);
        assertTrue(cam.getCamera().position.y >= halfH - EPSILON);
    }

    @Test
    void boundsClampTopRight() {
        cam.setWorldBounds(100, 100);
        cam.setTarget(200, 200);
        cam.snapToTarget();

        float maxX = 100f - VP_W * 0.5f;
        float maxY = 100f - VP_H * 0.5f;
        assertEquals(maxX, cam.getCamera().position.x, EPSILON);
        assertEquals(maxY, cam.getCamera().position.y, EPSILON);
    }

    @Test
    void clearBoundsAllowsFreeMovement() {
        cam.setWorldBounds(100, 100);
        cam.clearWorldBounds();

        cam.setTarget(-50, -50);
        cam.snapToTarget();

        assertEquals(-50f, cam.getCamera().position.x, EPSILON);
        assertEquals(-50f, cam.getCamera().position.y, EPSILON);
    }

    // ── Lerp factor ────────────────────────────────────────────────────────

    @Test
    void higherLerpFactorTracksMoreTightly() {
        CameraController slow = new CameraController(VP_W, VP_H);
        CameraController fast = new CameraController(VP_W, VP_H);

        slow.setLerpFactor(2f);
        fast.setLerpFactor(20f);

        slow.setTarget(0, 0); slow.snapToTarget();
        fast.setTarget(0, 0); fast.snapToTarget();

        slow.setTarget(100, 0);
        fast.setTarget(100, 0);

        slow.update(0.016f);
        fast.update(0.016f);

        assertTrue(fast.getCamera().position.x > slow.getCamera().position.x,
            "Faster lerp should be closer to target");
    }

    @Test
    void negativeLerpFactorThrows() {
        assertThrows(IllegalArgumentException.class, () -> cam.setLerpFactor(-1f));
    }

    @Test
    void zeroLerpFactorThrows() {
        assertThrows(IllegalArgumentException.class, () -> cam.setLerpFactor(0f));
    }

    // ── Zoom ───────────────────────────────────────────────────────────────

    @Test
    void setZoomUpdatesCamera() {
        cam.setZoom(0.5f);
        assertEquals(0.5f, cam.getCamera().zoom, EPSILON);
    }

    @Test
    void negativeZoomThrows() {
        assertThrows(IllegalArgumentException.class, () -> cam.setZoom(-1f));
    }

    @Test
    void zeroZoomThrows() {
        assertThrows(IllegalArgumentException.class, () -> cam.setZoom(0f));
    }

    // ── World bounds validation ────────────────────────────────────────────

    @Test
    void negativeBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> cam.setWorldBounds(-10, 100));
    }

    @Test
    void zeroBoundsThrows() {
        assertThrows(IllegalArgumentException.class, () -> cam.setWorldBounds(0, 100));
    }

    // ── Zoom affects bounds clamping ───────────────────────────────────────

    @Test
    void zoomedOutClampingAccountsForZoom() {
        cam.setZoom(2f);
        cam.setWorldBounds(100, 100);
        cam.setTarget(0, 0);
        cam.snapToTarget();

        float halfW = VP_W * 2f * 0.5f;
        float halfH = VP_H * 2f * 0.5f;
        assertTrue(cam.getCamera().position.x >= halfW - EPSILON);
        assertTrue(cam.getCamera().position.y >= halfH - EPSILON);
    }

    // ── Resize ─────────────────────────────────────────────────────────────

    @Test
    void resizeKeepsWorldUnitsAndAdjustsAspectRatio() {
        cam.resize(800, 600);
        // Height stays at base viewport (15 world units), width scales by aspect ratio
        assertEquals(VP_H, cam.getCamera().viewportHeight, EPSILON);
        float expectedWidth = VP_H * (800f / 600f);
        assertEquals(expectedWidth, cam.getCamera().viewportWidth, EPSILON);
    }

    @Test
    void resizeWithZeroHeightIsIgnored() {
        float prevW = cam.getCamera().viewportWidth;
        float prevH = cam.getCamera().viewportHeight;
        cam.resize(800, 0);
        assertEquals(prevW, cam.getCamera().viewportWidth, EPSILON);
        assertEquals(prevH, cam.getCamera().viewportHeight, EPSILON);
    }
}
