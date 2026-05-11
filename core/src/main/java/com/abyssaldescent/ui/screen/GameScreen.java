package com.abyssaldescent.ui.screen;

import com.abyssaldescent.GameController;
import com.abyssaldescent.GamePhase;
import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.audio.MusicPlayer;
import com.abyssaldescent.combat.CombatManager;
import com.abyssaldescent.combat.chips.ChipInventory;
import com.abyssaldescent.combat.chips.ChipPickupSystem;
import com.abyssaldescent.config.DifficultySettings;
import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyFactory;
import com.abyssaldescent.entity.enemy.EnemyType;
import com.abyssaldescent.entity.player.Player;
import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.entity.player.PlayerEffectSystem;
import com.abyssaldescent.entity.player.PlayerInputHandler;
import com.abyssaldescent.entity.state.AttackingState;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.GamePhaseChangedEvent;
import com.abyssaldescent.event.KeyPickedUpEvent;
import com.abyssaldescent.event.RespawnUsedEvent;
import com.abyssaldescent.render.CameraController;
import com.abyssaldescent.render.EnemySpriteRegistry;
import com.abyssaldescent.render.SpriteOrientation;
import com.abyssaldescent.ui.hud.HudRenderer;
import com.abyssaldescent.ui.screen.GameOverStats;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashMap;
import java.util.Map;

public class GameScreen implements Screen {

    private static final float VIEWPORT_HEIGHT       = 9f;
    private static final float VIEWPORT_WIDTH        = 16f;
    private static final float SPRITE_SIZE           = 1f;
    private static final float ENEMY_SIZE            = 0.9f;
    private static final int   FLOOR_WIDTH           = 25;
    private static final int   FLOOR_HEIGHT          = 25;
    private static final float ATTACK_WINDUP_FRACTION = 0.4f;

    private final DifficultySettings difficulty;

    private SpriteBatch    batch;
    private ShapeRenderer  shapes;
    private Texture        karinIdleTexture;
    private Texture        karinAttackWindupTexture;
    private Texture        karinAttackStrikeTexture;
    private Texture        floorTexture;
    private Player         player;
    private PlayerInputHandler    inputHandler;
    private CameraController      cameraController;
    private CombatManager         combatManager;
    private final SpriteOrientation playerOrientation = new SpriteOrientation();
    private EnemySpriteRegistry   enemySprites;
    private final Map<String, SpriteOrientation> enemyOrientations = new HashMap<>();
    private final Map<String, Float>             enemyLastX        = new HashMap<>();
    private final MusicPlayer musicPlayer = new MusicPlayer();
    private HudRenderer        hudRenderer;
    private ChipInventory      chipInventory;
    private ChipPickupSystem   chipPickupSystem;
    private GameController     controller;
    private PlayerEffectSystem playerEffectSystem;
    private EventListener<GamePhaseChangedEvent> phaseListener;
    private int demoKeys     = 0;
    private int demoRespawns = 3;
    private GameOverStats pendingGameOverStats = null;

    public GameScreen(DifficultySettings difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public void show() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.initWithDifficulty(difficulty);

        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();

        karinIdleTexture         = loadTextureOrPlaceholder("karin.png", Color.CYAN);
        karinAttackWindupTexture = loadAttackFrame("karin_attack_windup.png");
        karinAttackStrikeTexture = loadAttackFrame("karin_attack_strike.png");
        floorTexture             = loadOrGenerateFloorTile();

        player       = new Player(FLOOR_WIDTH / 2f, PlayerContext.GROUND_Y);
        inputHandler = new PlayerInputHandler(player);

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

        playerEffectSystem = new PlayerEffectSystem(player.getContext(), EventBus.getInstance());

        controller = new GameController();
        controller.startNewRun();
        controller.initPlayer(player);

        phaseListener = e -> {
            if (e.getNewPhase() == GamePhase.GAME_OVER) {
                int floor       = controller.getState().getFloorNumber();
                int respUsed    = controller.getMaxRespawns() - controller.getRespawnsRemaining();
                int maxResp     = controller.getMaxRespawns();
                pendingGameOverStats = new GameOverStats(floor, respUsed, maxResp);
            }
        };
        EventBus.getInstance().subscribe(GamePhaseChangedEvent.class, phaseListener);

        InputMultiplexer multiplexer = new InputMultiplexer(buildHudAdapter(), inputHandler);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        inputHandler.update();
        player.update(delta);
        playerEffectSystem.update(delta);
        controller.update(delta);
        clampPlayerToWorld();

        for (Enemy e : combatManager.getEnemies()) {
            e.observeTarget(player.getX(), player.getY(), true);
        }
        combatManager.update(delta);
        clampEnemiesToWorld();

        cameraController.setTarget(player.getX(), player.getY());
        cameraController.update(delta);

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
        hudRenderer.render(batch, shapes, delta);

        if (pendingGameOverStats != null) {
            UiManager.getInstance().showGameOver(pendingGameOverStats);
            pendingGameOverStats = null;
        }
    }

    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
        if (hudRenderer != null) hudRenderer.resize(width, height);
    }

    @Override public void pause()  {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        if (phaseListener != null) {
            EventBus.getInstance().unsubscribe(GamePhaseChangedEvent.class, phaseListener);
        }
    }

    @Override
    public void dispose() {
        try {
            if (batch != null)  batch.dispose();
            if (shapes != null) shapes.dispose();
            if (karinIdleTexture != null) karinIdleTexture.dispose();
            if (karinAttackWindupTexture != null && karinAttackWindupTexture != karinIdleTexture)
                karinAttackWindupTexture.dispose();
            if (karinAttackStrikeTexture != null && karinAttackStrikeTexture != karinIdleTexture)
                karinAttackStrikeTexture.dispose();
            if (floorTexture != null)    floorTexture.dispose();
            if (enemySprites != null)    enemySprites.dispose();
            if (musicPlayer != null) musicPlayer.dispose();
            if (combatManager != null)   combatManager.dispose();
            if (chipPickupSystem != null)    chipPickupSystem.dispose();
            if (hudRenderer != null)         hudRenderer.dispose();
            if (playerEffectSystem != null)  playerEffectSystem.dispose();
            if (controller != null)          controller.dispose();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error in dispose()", e);
        }
    }

    // ── input ────────────────────────────────────────────────────────────────

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

    // ── demo helpers ─────────────────────────────────────────────────────────

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

    private void spawnDemoEnemies() {
        EnemyFactory factory = new EnemyFactory();
        float cx = FLOOR_WIDTH / 2f;
        float gy = PlayerContext.GROUND_Y;
        combatManager.addEnemy(factory.create(EnemyType.SHADOW_GOBLIN, cx + 3, gy));
        combatManager.addEnemy(factory.create(EnemyType.SHADOW_GOBLIN, cx - 3, gy));
        combatManager.addEnemy(factory.create(EnemyType.MOSS_CRAWLER,  cx + 5, gy));
        combatManager.addEnemy(factory.create(EnemyType.BONE_ARCHER,   cx - 5, gy));
    }

    // ── rendering ────────────────────────────────────────────────────────────

    private void drawFloorTiles() {
        batch.setColor(Color.WHITE);
        batch.draw(floorTexture, 0, 0, FLOOR_WIDTH, FLOOR_HEIGHT);
    }

    private void drawPlayer() {
        Texture frame = currentPlayerFrame();
        PlayerContext ctx = player.getContext();
        playerOrientation.update(ctx.getFacing());
        float half = SPRITE_SIZE * 0.5f;
        batch.draw(frame,
                player.getX() - half, player.getY() - half,
                SPRITE_SIZE, SPRITE_SIZE,
                0, 0, frame.getWidth(), frame.getHeight(),
                playerOrientation.isFlipX(), false);
    }

    private Texture currentPlayerFrame() {
        if (player.getCurrentState() != AttackingState.INSTANCE) return karinIdleTexture;
        float t = player.getContext().getStateTimer();
        float windupEnd = PlayerContext.ATTACK_DURATION * ATTACK_WINDUP_FRACTION;
        return t < windupEnd ? karinAttackWindupTexture : karinAttackStrikeTexture;
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
                    0, 0, tex.getWidth(), tex.getHeight(),
                    orient.isFlipX(), false);
        }
    }

    private void renderEnemies() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : combatManager.getEnemies()) {
            if (!enemySprites.has(e.getType())) {
                Color body = colorFor(e.getType());
                if (e.isDead()) body = Color.DARK_GRAY;
                shapes.setColor(body);
                shapes.rect(e.getX() - ENEMY_SIZE * 0.5f, e.getY() - ENEMY_SIZE * 0.5f,
                        ENEMY_SIZE, ENEMY_SIZE);
            }
            if (!e.isDead()) {
                float hpPct = e.getCurrentHp() / (float) e.getType().getMaxHp();
                float barW = ENEMY_SIZE, barH = 0.12f;
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
            case MOSS_CRAWLER:  return Color.OLIVE;
            case STONE_WATCHER: return Color.GRAY;
            case BONE_ARCHER:   return Color.WHITE;
            default:            return Color.RED;
        }
    }

    // ── physics helpers ──────────────────────────────────────────────────────

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
            if (x != e.getX() || y != e.getY()) e.getContext().setPosition(x, y);
        }
    }

    // ── asset loaders ────────────────────────────────────────────────────────

    private Texture loadTextureOrPlaceholder(String path, Color fallback) {
        Texture t;
        if (Gdx.files.internal(path).exists()) {
            try {
                t = new Texture(path);
            } catch (RuntimeException e) {
                Gdx.app.error("GameScreen", "Failed to load " + path, e);
                t = solidTexture(fallback);
            }
        } else {
            Gdx.app.log("GameScreen", "Missing asset: " + path + " — using placeholder.");
            t = solidTexture(fallback);
        }
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }

    private Texture loadAttackFrame(String path) {
        if (Gdx.files.internal(path).exists()) {
            try {
                Texture t = new Texture(path);
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                return t;
            } catch (RuntimeException e) {
                Gdx.app.error("GameScreen", "Failed to load " + path, e);
            }
        }
        return karinIdleTexture;
    }

    private Texture solidTexture(Color color) {
        Pixmap px = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        px.setColor(color);
        px.fill();
        Texture t = new Texture(px);
        px.dispose();
        return t;
    }

    private Texture loadOrGenerateFloorTile() {
        if (Gdx.files.internal("floor_tile.png").exists()) {
            try {
                Pixmap src  = new Pixmap(Gdx.files.internal("floor_tile.png"));
                Pixmap rgba = new Pixmap(src.getWidth(), src.getHeight(), Pixmap.Format.RGBA8888);
                rgba.setBlending(Pixmap.Blending.None);
                rgba.drawPixmap(src, 0, 0);
                Texture t = new Texture(rgba);
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                src.dispose();
                rgba.dispose();
                return t;
            } catch (RuntimeException e) {
                Gdx.app.error("GameScreen", "Failed to load floor_tile.png", e);
            }
        }
        return generatePlaceholderTile();
    }

    private Texture generatePlaceholderTile() {
        int size = 16;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0.3f, 0.3f, 0.3f, 1f);
        px.fill();
        px.setColor(0.4f, 0.4f, 0.4f, 1f);
        for (int x = 0; x < size; x += 4)
            for (int y = 0; y < size; y += 4)
                px.drawPixel(x, y);
        Texture t = new Texture(px);
        px.dispose();
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return t;
    }
}
