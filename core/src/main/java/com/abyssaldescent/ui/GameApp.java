package com.abyssaldescent.ui;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.combat.CombatManager;
import com.abyssaldescent.entity.AttackingState;
import com.abyssaldescent.entity.Player;
import com.abyssaldescent.entity.PlayerContext;
import com.abyssaldescent.entity.PlayerInputHandler;
import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyFactory;
import com.abyssaldescent.entity.enemy.EnemyType;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.render.CameraController;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameApp extends ApplicationAdapter {
    private static final float VIEWPORT_HEIGHT = 9f;
    private static final float VIEWPORT_WIDTH = 16f;
    private static final float SPRITE_SIZE = 1f;
    private static final float ENEMY_SIZE = 0.9f;
    private static final int FLOOR_WIDTH = 50;
    private static final int FLOOR_HEIGHT = 50;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private Texture karinTexture;
    private Texture floorTexture;
    private TextureRegion floorRegion;
    private Player player;
    private PlayerInputHandler inputHandler;
    private CameraController cameraController;
    private CombatManager combatManager;

    @Override
    public void create() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.activateKarin();

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

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
        inputHandler.setCamera(cameraController.getCamera());

        combatManager = new CombatManager(EventBus.getInstance(), player.getCombatStrategy());
        combatManager.setPlayerBaseDamage(15);
        spawnDemoEnemies();
    }

    private void spawnDemoEnemies() {
        EnemyFactory factory = new EnemyFactory();
        float cx = FLOOR_WIDTH / 2f;
        float cy = FLOOR_HEIGHT / 2f;
        combatManager.addEnemy(factory.create(EnemyType.SHADOW_GOBLIN, cx + 3, cy));
        combatManager.addEnemy(factory.create(EnemyType.SHADOW_GOBLIN, cx - 3, cy + 1));
        combatManager.addEnemy(factory.create(EnemyType.RATLING,       cx,     cy + 3));
        combatManager.addEnemy(factory.create(EnemyType.MOSS_CRAWLER,  cx + 4, cy - 2));
        combatManager.addEnemy(factory.create(EnemyType.BONE_ARCHER,   cx - 4, cy - 3));
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        inputHandler.update();
        player.update(dt);

        for (Enemy e : combatManager.getEnemies()) {
            e.observeTarget(player.getX(), player.getY(), true);
        }
        combatManager.update(dt);

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

        shapes.setProjectionMatrix(cameraController.getCamera().combined);
        renderEnemies();
        renderAttackCone();
    }

    private void renderEnemies() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : combatManager.getEnemies()) {
            Color body = colorFor(e.getType());
            if (e.isDead()) body = Color.DARK_GRAY;
            shapes.setColor(body);
            shapes.rect(e.getX() - ENEMY_SIZE * 0.5f, e.getY() - ENEMY_SIZE * 0.5f,
                    ENEMY_SIZE, ENEMY_SIZE);

            if (!e.isDead()) {
                float hpPct = e.getCurrentHp() / (float) e.getType().getMaxHp();
                float barW = ENEMY_SIZE;
                float barH = 0.12f;
                float bx = e.getX() - barW * 0.5f;
                float by = e.getY() + ENEMY_SIZE * 0.5f + 0.05f;
                shapes.setColor(Color.DARK_GRAY);
                shapes.rect(bx, by, barW, barH);
                shapes.setColor(Color.RED);
                shapes.rect(bx, by, barW * hpPct, barH);
            }
        }
        shapes.end();
    }

    private void renderAttackCone() {
        if (player.getCurrentState() != AttackingState.INSTANCE) return;

        PlayerContext ctx = player.getContext();
        float ox = ctx.getPosition().x;
        float oy = ctx.getPosition().y;
        float fx = ctx.getFacing().x;
        float fy = ctx.getFacing().y;
        float range = PlayerContext.ATTACK_RANGE;
        float leftX = -fy;
        float leftY = fx;

        float tip1x = ox + fx * range + leftX * range * 0.5f;
        float tip1y = oy + fy * range + leftY * range * 0.5f;
        float tip2x = ox + fx * range - leftX * range * 0.5f;
        float tip2y = oy + fy * range - leftY * range * 0.5f;

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 0.9f, 0.3f, 0.5f);
        shapes.triangle(ox, oy, tip1x, tip1y, tip2x, tip2y);
        shapes.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    private Color colorFor(EnemyType type) {
        switch (type) {
            case SHADOW_GOBLIN: return Color.FOREST;
            case MOSS_CRAWLER: return Color.OLIVE;
            case RATLING: return Color.BROWN;
            case STONE_WATCHER: return Color.GRAY;
            case BONE_ARCHER: return Color.WHITE;
            case CAVE_COLOSSUS: return Color.MAROON;
            case LIVING_SHADOW: return Color.PURPLE;
            case GRAVITY_PARASITE:return Color.TEAL;
            default: return Color.RED;
        }
    }

    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        karinTexture.dispose();
        floorTexture.dispose();
        if (combatManager != null) combatManager.dispose();
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
