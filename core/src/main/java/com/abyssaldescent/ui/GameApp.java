package com.abyssaldescent.ui;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.audio.MusicPlayer;
import com.abyssaldescent.combat.CombatManager;
import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyFactory;
import com.abyssaldescent.entity.enemy.EnemyType;
import com.abyssaldescent.entity.player.Player;
import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.entity.player.PlayerInputHandler;
import com.abyssaldescent.entity.state.AttackingState;
import com.abyssaldescent.combat.chips.ChipInventory;
import com.abyssaldescent.combat.chips.ChipPickupSystem;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.KeyPickedUpEvent;
import com.abyssaldescent.event.RespawnUsedEvent;
import com.abyssaldescent.render.CameraController;
import com.abyssaldescent.ui.hud.HudRenderer;
import com.abyssaldescent.render.EnemySpriteRegistry;
import com.abyssaldescent.render.SpriteOrientation;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashMap;
import java.util.Map;

public class GameApp extends ApplicationAdapter {
    private static final float VIEWPORT_HEIGHT = 9f;
    private static final float VIEWPORT_WIDTH = 16f;
    private static final float SPRITE_SIZE = 1f;
    private static final float ENEMY_SIZE = 0.9f;
    private static final int FLOOR_WIDTH = 25;
    private static final int FLOOR_HEIGHT = 25;
    private static final float ATTACK_WINDUP_FRACTION = 0.4f;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private Texture karinIdleTexture;
    private Texture karinAttackWindupTexture;
    private Texture karinAttackStrikeTexture;
    private Texture floorTexture;
    private Player player;
    private PlayerInputHandler inputHandler;
    private CameraController cameraController;
    private CombatManager combatManager;
    private final SpriteOrientation playerOrientation = new SpriteOrientation();
    private EnemySpriteRegistry enemySprites;
    private final Map<String, SpriteOrientation> enemyOrientations = new HashMap<>();
    private final Map<String, Float> enemyLastX = new HashMap<>();
    private final MusicPlayer musicPlayer = new MusicPlayer();
    private HudRenderer      hudRenderer;
    private ChipInventory    chipInventory;
    private ChipPickupSystem chipPickupSystem;
    private int              demoKeys       = 0;
    private int              demoRespawns   = 3;

    @Override
    public void create() {
            Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) { Gdx.app.exit(); return true; }
                return false;
            }
        });

        GameStateManager gsm = GameStateManager.getInstance();
        gsm.activateKarin();

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        karinIdleTexture = loadTextureOrPlaceholder("karin.png", Color.CYAN);

        karinAttackWindupTexture = loadAttackFrame("karin_attack_windup.png");
        karinAttackStrikeTexture = loadAttackFrame("karin_attack_strike.png");

        floorTexture = loadOrGenerateFloorTile();

        player = new Player(FLOOR_WIDTH / 2f, PlayerContext.GROUND_Y);

        inputHandler = new PlayerInputHandler(player);
        Gdx.input.setInputProcessor(inputHandler);

        cameraController = new CameraController(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        cameraController.setWorldBounds(FLOOR_WIDTH, FLOOR_HEIGHT);
        cameraController.setTarget(player.getX(), player.getY());
        cameraController.snapToTarget();
        inputHandler.setCamera(cameraController.getCamera());

        combatManager = new CombatManager(EventBus.getInstance(), player.getCombatStrategy());
        combatManager.setPlayerBaseDamage(15);
        enemySprites = new EnemySpriteRegistry();
        musicPlayer.start();
        spawnDemoEnemies();

        chipInventory    = new ChipInventory();
        chipPickupSystem = new ChipPickupSystem(
                chipInventory, combatManager,
                EventBus.getInstance(), player.getCombatStrategy());
        hudRenderer = new HudRenderer(chipInventory);

        // Replace the plain inputHandler processor with a multiplexer so HUD
        // keys (TAB, 1-4, ESC-close-inventory) are handled before player input.
        InputMultiplexer multiplexer = new InputMultiplexer(buildHudAdapter(), inputHandler);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private InputAdapter buildHudAdapter() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.TAB) {
                    hudRenderer.toggleInventory();
                    return true;
                }
                if (hudRenderer.isInventoryOpen()) {
                    if (keycode == Input.Keys.ESCAPE) {
                        hudRenderer.closeInventory();
                        return true;
                    }
                    return false;
                }
                // Keys 1-4 activate chip slots while inventory is closed
                if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_4) {
                    hudRenderer.activateChipSlot(keycode - Input.Keys.NUM_1);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (hudRenderer.isInventoryOpen()) {
                    hudRenderer.handleInventoryClick(screenX, screenY);
                    return true;
                }
                return false;
            }
        };
    }

    private Texture loadTextureOrPlaceholder(String path, Color fallbackColor) {
        Texture t;
        if (Gdx.files.internal(path).exists()) {
            try {
                t = new Texture(path);
            } catch (RuntimeException e) {
                Gdx.app.error("GameApp", "Failed to load " + path + ", using placeholder.", e);
                t = generateSolidTexture(fallbackColor);
            }
        } else {
            Gdx.app.log("GameApp", "Missing asset: " + path + " — using placeholder.");
            t = generateSolidTexture(fallbackColor);
        }
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    private Texture generateSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture loadAttackFrame(String path) {
        Texture t;
        if (Gdx.files.internal(path).exists()) {
            try {
                t = new Texture(path);
            } catch (RuntimeException e) {
                Gdx.app.error("GameApp", "Failed to load " + path + ", reusing idle frame.", e);
                return karinIdleTexture;
            }
        } else {
            t = karinIdleTexture;
        }
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    private void spawnDemoEnemies() {
        EnemyFactory factory = new EnemyFactory();
        float cx = FLOOR_WIDTH / 2f;
        float gy = PlayerContext.GROUND_Y;
        combatManager.addEnemy(factory.create(EnemyType.SHADOW_GOBLIN, cx + 3, gy));
        combatManager.addEnemy(factory.create(EnemyType.SHADOW_GOBLIN, cx - 3, gy));
        combatManager.addEnemy(factory.create(EnemyType.MOSS_CRAWLER,  cx + 5, gy));
        combatManager.addEnemy(factory.create(EnemyType.BONE_ARCHER,   cx - 5, gy));
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        inputHandler.update();
        player.update(dt);
        clampPlayerToWorld();

        for (Enemy e : combatManager.getEnemies()) {
            e.observeTarget(player.getX(), player.getY(), true);
        }
        combatManager.update(dt);
        clampEnemiesToWorld();

        cameraController.setTarget(player.getX(), player.getY());
        cameraController.update(dt);

        ScreenUtils.clear(0, 0, 0, 1f);

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        batch.begin();
        drawFloorTiles();
        drawPlayer();
        drawEnemySprites();
        batch.end();

        shapes.setProjectionMatrix(cameraController.getCamera().combined);
        renderEnemies();

        fireDemoEvents();
        hudRenderer.render(batch, shapes, dt);
    }

    /** Demo: K = pick up a key, R = use a respawn. */
    private void fireDemoEvents() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            demoKeys = Math.min(demoKeys + 1, 1);
            EventBus.getInstance().post(new KeyPickedUpEvent(demoKeys, 1));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            demoRespawns = Math.max(demoRespawns - 1, 0);
            EventBus.getInstance().post(new RespawnUsedEvent(demoRespawns, 3));
        }
    }

    private void drawFloorTiles() {
        batch.setColor(Color.WHITE);
        batch.draw(floorTexture, 0, 0, FLOOR_WIDTH, FLOOR_HEIGHT);
    }

    private void clampPlayerToWorld() {
        float halfW = SPRITE_SIZE * 0.5f;
        float x = MathUtils.clamp(player.getX(), halfW, FLOOR_WIDTH - halfW);
        float y = MathUtils.clamp(player.getY(), PlayerContext.GROUND_Y, FLOOR_HEIGHT - halfW);
        player.getContext().setPosition(x, y);
    }

    private void clampEnemiesToWorld() {
        float half = ENEMY_SIZE * 0.5f;
        for (Enemy e : combatManager.getEnemies()) {
            float x = MathUtils.clamp(e.getX(), half, FLOOR_WIDTH - half);
            float y = MathUtils.clamp(e.getY(), half, FLOOR_HEIGHT - half);
            if (x != e.getX() || y != e.getY()) {
                e.getContext().setPosition(x, y);
            }
        }
    }

    private void drawEnemySprites() {
        for (Enemy e : combatManager.getEnemies()) {
            Texture tex = enemySprites.get(e.getType());
            if (tex == null) continue;

            SpriteOrientation orient = enemyOrientations.computeIfAbsent(
                    e.getId(), id -> new SpriteOrientation());
            Float prevX = enemyLastX.get(e.getId());
            if (prevX != null) orient.update(e.getX() - prevX);
            enemyLastX.put(e.getId(), e.getX());

            float half = ENEMY_SIZE * 0.5f;
            batch.draw(tex,
                    e.getX() - half, e.getY() - half,
                    ENEMY_SIZE, ENEMY_SIZE,
                    0, 0,
                    tex.getWidth(), tex.getHeight(),
                    orient.isFlipX(), false);
        }
    }

    private void drawPlayer() {
        Texture frame = currentPlayerFrame();
        PlayerContext ctx = player.getContext();
        playerOrientation.update(ctx.getFacing());

        float half = SPRITE_SIZE * 0.5f;
        batch.draw(frame,
            player.getX() - half, player.getY() - half,
            SPRITE_SIZE, SPRITE_SIZE,
            0, 0,
            frame.getWidth(), frame.getHeight(),
            playerOrientation.isFlipX(), false);
    }

    private Texture currentPlayerFrame() {
        if (player.getCurrentState() != AttackingState.INSTANCE) {
            return karinIdleTexture;
        }
        float t = player.getContext().getStateTimer();
        float windupEnd = PlayerContext.ATTACK_DURATION * ATTACK_WINDUP_FRACTION;
        return t < windupEnd ? karinAttackWindupTexture : karinAttackStrikeTexture;
    }

    private void renderEnemies() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : combatManager.getEnemies()) {
            boolean hasSprite = enemySprites.has(e.getType());
            if (!hasSprite) {
                Color body = colorFor(e.getType());
                if (e.isDead()) body = Color.DARK_GRAY;
                shapes.setColor(body);
                shapes.rect(e.getX() - ENEMY_SIZE * 0.5f, e.getY() - ENEMY_SIZE * 0.5f,
                        ENEMY_SIZE, ENEMY_SIZE);
            }

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

    private Color colorFor(EnemyType type) {
        switch (type) {
            case SHADOW_GOBLIN: return Color.FOREST;
            case MOSS_CRAWLER: return Color.OLIVE;
            case STONE_WATCHER: return Color.GRAY;
            case BONE_ARCHER: return Color.WHITE;
            default: return Color.RED;
        }
    }

    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
        if (hudRenderer != null) hudRenderer.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        karinIdleTexture.dispose();
        if (karinAttackWindupTexture != karinIdleTexture) karinAttackWindupTexture.dispose();
        if (karinAttackStrikeTexture != karinIdleTexture) karinAttackStrikeTexture.dispose();
        floorTexture.dispose();
        if (enemySprites != null) enemySprites.dispose();
        musicPlayer.dispose();
        if (combatManager != null) combatManager.dispose();
        if (chipPickupSystem != null) chipPickupSystem.dispose();
        if (hudRenderer != null) hudRenderer.dispose();
    }

    private Texture loadOrGenerateFloorTile() {
        if (Gdx.files.internal("floor_tile.png").exists()) {
            try {
                Pixmap src = new Pixmap(Gdx.files.internal("floor_tile.png"));
                Pixmap rgba = new Pixmap(src.getWidth(), src.getHeight(), Pixmap.Format.RGBA8888);
                rgba.setBlending(Pixmap.Blending.None);
                rgba.drawPixmap(src, 0, 0);
                Texture t = new Texture(rgba);
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                src.dispose();
                rgba.dispose();
                return t;
            } catch (RuntimeException e) {
                Gdx.app.error("GameApp", "Failed to load floor_tile.png, using placeholder.", e);
            }
        }
        Texture placeholder = generatePlaceholderTile();
        placeholder.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return placeholder;
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
