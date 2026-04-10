package com.abyssaldescent.ui;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.core.entity.Player;
import com.abyssaldescent.core.entity.PlayerInputHandler;
import com.abyssaldescent.render.CameraController;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameApp extends ApplicationAdapter {

    /**
     * Viewport height in world units (tiles).
     * Width is computed from the aspect ratio to avoid stretching.
     * 9 tiles at 1080p ≈ 120 px per tile — good for 16×16 pixel art scaled up.
     */
    private static final float VIEWPORT_HEIGHT = 9f;
    private static final float VIEWPORT_WIDTH = 16f;

    /** Karin sprite size in world units (one tile). */
    private static final float SPRITE_SIZE = 1f;

    /** Size of the floor in tiles. */
    private static final int FLOOR_WIDTH = 50;
    private static final int FLOOR_HEIGHT = 50;

    private SpriteBatch batch;

    private Texture karinTexture;
    private Texture floorTexture;
    private TextureRegion floorRegion;

    private Player player;
    private PlayerInputHandler inputHandler;
    private CameraController cameraController;

    @Override
    public void create() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.activateKarin();

        batch = new SpriteBatch();

        // Karin sprite
        karinTexture = new Texture("karin.png");
        karinTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Floor tile — loads floor_tile.png if it exists, otherwise generates a placeholder
        floorTexture = loadOrGenerateFloorTile();
        floorTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        floorTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // TextureRegion covers the entire floor — GPU repeats the 16x16 tile automatically
        floorRegion = new TextureRegion(floorTexture);
        floorRegion.setRegion(0, 0, FLOOR_WIDTH * 16, FLOOR_HEIGHT * 16);

        player = new Player(FLOOR_WIDTH / 2f, FLOOR_HEIGHT / 2f);

        inputHandler = new PlayerInputHandler(player);
        Gdx.input.setInputProcessor(inputHandler);

        cameraController = new CameraController(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        cameraController.setWorldBounds(FLOOR_WIDTH, FLOOR_HEIGHT);
        cameraController.setTarget(player.getX(), player.getY());
        cameraController.snapToTarget();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // 1. Gather input
        inputHandler.update();

        // 2. Update game logic
        player.update(dt);

        // 3. Update camera to follow Karin
        cameraController.setTarget(player.getX(), player.getY());
        cameraController.update(dt);

        // 4. Render
        ScreenUtils.clear(0, 0, 0, 1f);

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        batch.begin();

        // 4a. Draw tiled floor
        batch.draw(floorRegion, 0, 0, FLOOR_WIDTH, FLOOR_HEIGHT);

        // 4b. Draw Karin
        batch.draw(karinTexture,
            player.getX() - SPRITE_SIZE * 0.5f,
            player.getY() - SPRITE_SIZE * 0.5f,
            SPRITE_SIZE, SPRITE_SIZE);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        karinTexture.dispose();
        floorTexture.dispose();
    }

    /**
     * Tries to load {@code floor_tile.png} (16×16) from assets.
     * If missing, generates a stone-floor placeholder so the game
     * runs without any external art. Replace the file later with real art.
     */
    private Texture loadOrGenerateFloorTile() {
        if (Gdx.files.internal("floor_tile.png").exists()) {
            return new Texture("floor_tile.png");
        }
        return generatePlaceholderTile();
    }

    /**
     * Procedurally generates a 16×16 stone-floor tile in the grey-green
     * palette of Tier 1 (Upper Ruins). Acts as a stand-in until real art is ready.
     */
    private Texture generatePlaceholderTile() {
        int size = 16;
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        // Base stone colour (dark grey-green)
        int base = toRGBA8888(38, 40, 36, 255);
        pm.setColor(base);
        pm.fill();

        // Subtle noise — lighter and darker pixels for texture
        int light = toRGBA8888(48, 52, 44, 255);
        int dark  = toRGBA8888(28, 30, 26, 255);

        // Fixed "random" pattern — looks like cracked stone
        int[] lightPixels = {
            1,1, 3,5, 6,2, 9,7, 12,3, 14,10, 4,12, 7,14, 11,11, 2,8,
            5,9, 13,6, 8,4, 10,13, 15,1, 0,14, 6,10, 3,0, 14,15, 9,12
        };
        for (int i = 0; i < lightPixels.length; i += 2) {
            pm.drawPixel(lightPixels[i], lightPixels[i + 1], light);
        }

        int[] darkPixels = {
            2,3, 5,1, 8,6, 11,2, 14,8, 1,13, 7,9, 10,0, 13,14, 4,7,
            0,5, 6,13, 9,3, 12,11, 15,6, 3,10, 8,15, 11,8, 2,0, 14,4
        };
        for (int i = 0; i < darkPixels.length; i += 2) {
            pm.drawPixel(darkPixels[i], darkPixels[i + 1], dark);
        }

        // Grout lines (tile edges) — slightly darker border
        int grout = toRGBA8888(24, 26, 22, 255);
        for (int i = 0; i < size; i++) {
            pm.drawPixel(i, 0, grout);       // bottom edge
            pm.drawPixel(0, i, grout);       // left edge
        }

        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private static int toRGBA8888(int r, int g, int b, int a) {
        return (r << 24) | (g << 16) | (b << 8) | a;
    }
}
