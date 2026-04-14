package com.abyssaldescent.ui;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.entity.Player;
import com.abyssaldescent.entity.PlayerInputHandler;
import com.abyssaldescent.render.CameraController;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameApp extends ApplicationAdapter {

    private static final float VIEWPORT_HEIGHT = 9f;
    private static final float VIEWPORT_WIDTH = 16f;

    private static final float SPRITE_SIZE = 1f;

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

        karinTexture = new Texture("karin.png");
        karinTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        floorTexture = loadOrGenerateFloorTile();
        floorTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        floorTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

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

        inputHandler.update();

        player.update(dt);

        cameraController.setTarget(player.getX(), player.getY());
        cameraController.update(dt);

        ScreenUtils.clear(0, 0, 0, 1f);

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        batch.begin();

        batch.draw(floorRegion, 0, 0, FLOOR_WIDTH, FLOOR_HEIGHT);

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
    private Texture loadOrGenerateFloorTile() {
        if (Gdx.files.internal("floor_tile.png").exists()) {
            return new Texture("floor_tile.png");
        }
        return generatePlaceholderTile();
    }

    private Texture generatePlaceholderTile() {
        int size = 16;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
        pixmap.fill();
        pixmap.setColor(0.4f, 0.4f, 0.4f, 1f);
        for (int x = 0; x < size; x += 4) {
            for (int y = 0; y < size; y += 4) {
                pixmap.drawPixel(x, y);
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;

     
    }
}
