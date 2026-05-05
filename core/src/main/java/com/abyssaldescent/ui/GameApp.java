package com.abyssaldescent.ui;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.audio.MusicPlayer;
import com.abyssaldescent.combat.CombatManager;
import com.abyssaldescent.combat.strategy.MeleeStrategy;
import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyFactory;
import com.abyssaldescent.entity.enemy.EnemyType;
import com.abyssaldescent.entity.player.Player;
import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.entity.player.PlayerInputHandler;
import com.abyssaldescent.entity.state.AttackingState;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerInteractionEvent;
import com.abyssaldescent.event.RoomEnteredEvent;
import com.abyssaldescent.event.TierTransitionEvent;
import com.abyssaldescent.render.EnemySpriteRegistry;
import com.abyssaldescent.render.SpriteOrientation;
import com.abyssaldescent.world.Direction;
import com.abyssaldescent.world.Door;
import com.abyssaldescent.world.Dungeon;
import com.abyssaldescent.world.DungeonGenerator;
import com.abyssaldescent.world.DoorOpeningSystem;
import com.abyssaldescent.world.Key;
import com.abyssaldescent.world.KeyInventory;
import com.abyssaldescent.world.PlayerInteractionSystem;
import com.abyssaldescent.world.Room;
import com.abyssaldescent.world.RoomEventHandler;
import com.abyssaldescent.world.RoomManager;
import com.abyssaldescent.world.Tier;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameApp extends ApplicationAdapter {

    // World height is fixed at 10 units; width is computed from the actual screen aspect ratio
    // so the background always fills the entire screen without letterboxing.
    private static final float ROOM_H         = 10f;
    private static float ROOM_W               = 16f; // updated in create() and resize()
    // Solid wall band thickness (world units) — no entity enters this zone
    private static final float WALL_T         = 0.6f;
    // Width of the passable gap cut in a wall where a door exists
    private static final float DOOR_GAP       = 1.8f;

    private static final float SPRITE_SIZE    = 0.8f;
    private static final float ENEMY_SIZE     = 0.75f;
    private static final float INTERACT_RANGE = 2.0f;
    private static final float ATTACK_WINDUP  = 0.4f;

    // Offset from the wall where the player spawns after a door transition
    private static final float SPAWN_OFFSET   = WALL_T + SPRITE_SIZE;

    private SpriteBatch batch;
    private ShapeRenderer shapes;

    // World camera: always covers exactly roomW × ROOM_H world units
    private OrthographicCamera camera;
    // UI camera: screen-space pixels, for minimap overlay
    private OrthographicCamera uiCamera;

    private Texture karinIdleTexture;
    private Texture karinAttackWindupTexture;
    private Texture karinAttackStrikeTexture;
    private Texture fallbackFloor;

    private final Map<String, Texture> roomBgCache = new HashMap<>();

    private Player player;
    private PlayerInputHandler inputHandler;
    private CombatManager combatManager;

    private final SpriteOrientation playerOrientation = new SpriteOrientation();
    private EnemySpriteRegistry enemySprites;
    private final Map<String, SpriteOrientation> enemyOrientations = new HashMap<>();
    private final Map<String, Float> enemyLastX = new HashMap<>();
    private final MusicPlayer musicPlayer = new MusicPlayer();

    private Dungeon dungeon;
    private RoomManager roomManager;
    private KeyInventory keyInventory;
    private DoorOpeningSystem doorOpeningSystem;
    private MinimapRenderer minimapRenderer;

    @Override
    public void create() {
        GameStateManager.getInstance().activateKarin();

        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();

        camera   = new OrthographicCamera();
        uiCamera = new OrthographicCamera();
        ROOM_W = ROOM_H * Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        karinIdleTexture         = tryLoadTexture("karin.png",               Color.CYAN);
        karinAttackWindupTexture = tryLoadOrFallback("karin_attack_windup.png", karinIdleTexture);
        karinAttackStrikeTexture = tryLoadOrFallback("karin_attack_strike.png", karinIdleTexture);
        fallbackFloor            = buildDotGridTexture(
                new Color(0.20f, 0.20f, 0.20f, 1f),
                new Color(0.27f, 0.27f, 0.27f, 1f));

        EventBus eventBus = EventBus.getInstance();
        keyInventory      = new KeyInventory();
        roomManager       = new RoomManager(eventBus);
        dungeon           = new DungeonGenerator(new EnemyFactory()).generate();
        roomManager.loadDungeon(dungeon);

        combatManager = new CombatManager(eventBus, new MeleeStrategy());
        combatManager.setPlayerBaseDamage(15);
        enemySprites = new EnemySpriteRegistry();

        doorOpeningSystem = new DoorOpeningSystem(keyInventory, roomManager, eventBus);
        new RoomEventHandler(roomManager, eventBus);
        new PlayerInteractionSystem(roomManager, doorOpeningSystem, eventBus);

        eventBus.subscribe(RoomEnteredEvent.class,    this::onRoomEntered);
        eventBus.subscribe(TierTransitionEvent.class, this::onTierTransition);

        minimapRenderer = new MinimapRenderer(dungeon);

        Room startRoom = dungeon.getRoom(dungeon.getStartRoomId());
        // First room: spawn in left-centre
        float startX = SPAWN_OFFSET;
        float startY = ROOM_H * 0.5f;
        player = new Player(startX, startY);

        inputHandler = new PlayerInputHandler(player);
        Gdx.input.setInputProcessor(inputHandler);
        inputHandler.setCamera(camera);

        loadRoomEnemies(startRoom);
        applyCameraForRoom(startRoom);
        musicPlayer.start();
    }

    // ── Events ────────────────────────────────────────────────────────────────

    private void onRoomEntered(RoomEnteredEvent event) {
        Room room = dungeon.getRoom(event.getRoomId());
        if (room == null) return;

        combatManager.getEnemies().clear();
        enemyOrientations.clear();
        enemyLastX.clear();
        loadRoomEnemies(room);

        // Place the player near the door they just came through
        float spawnX, spawnY;
        Direction entry = event.getEntryDirection();
        if (entry == null) {
            // Very first room
            spawnX = SPAWN_OFFSET;
            spawnY = ROOM_H * 0.5f;
        } else {
            switch (entry) {
                case WEST:
                    // entered from the left wall → spawn just inside left wall
                    spawnX = SPAWN_OFFSET;
                    spawnY = ROOM_H * 0.5f;
                    break;
                case EAST:
                    // entered from the right wall → spawn just inside right wall
                    spawnX = ROOM_W - SPAWN_OFFSET;
                    spawnY = ROOM_H * 0.5f;
                    break;
                case NORTH:
                    // entered from top wall → spawn just below top wall
                    spawnX = ROOM_W * 0.5f;
                    spawnY = ROOM_H - SPAWN_OFFSET;
                    break;
                case SOUTH:
                default:
                    // entered from bottom wall → spawn just above bottom wall
                    spawnX = ROOM_W * 0.5f;
                    spawnY = SPAWN_OFFSET;
                    break;
            }
        }
        player.getContext().setPosition(spawnX, spawnY);
        applyCameraForRoom(room);
    }

    private void onTierTransition(TierTransitionEvent event) {
        musicPlayer.playForTier(tierMusicKey(event.getToTier()));
    }

    private String tierMusicKey(Tier tier) {
        switch (tier) {
            case SURFACE_CAVERNS: return "surface_caverns";
            case DROWNED_DEPTHS:  return "drowned_depths";
            case LAVA_SANCTUM:    return "lava_sanctum";
            default: return "surface_caverns";
        }
    }

    private void loadRoomEnemies(Room room) {
        for (Enemy enemy : room.getEnemies()) {
            combatManager.addEnemy(enemy);
        }
    }

    /**
     * Sets the camera so it shows exactly roomW × ROOM_H world units,
     * centred on the room. LibGDX will letterbox/pillarbox on the GPU side
     * when the screen aspect differs — the background fills the viewport exactly
     * without any stretching.
     */
    private void applyCameraForRoom(Room room) {
        camera.setToOrtho(false, ROOM_W, ROOM_H);
        camera.position.set(ROOM_W * 0.5f, ROOM_H * 0.5f, 0);
        camera.update();
    }

    // ── Main render loop ─────────────────────────────────────────────────────

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        Room room = roomManager.getCurrentRoom();
        float roomW = ROOM_W;

        // Input
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            EventBus.getInstance().post(new PlayerInteractionEvent(
                    player.getX(), player.getY(), INTERACT_RANGE));
        }

        // Update
        inputHandler.update();
        player.update(dt);
        clampPlayer(roomW);

        for (Enemy e : combatManager.getEnemies()) {
            e.observeTarget(player.getX(), player.getY(), true);
        }
        combatManager.update(dt);
        clampEnemies(roomW);

        // ── Clear ─────────────────────────────────────────────────────────────
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── World draw ───────────────────────────────────────────────────────
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawBackground(room, roomW);
        drawPlayer();
        drawEnemySprites();
        batch.end();

        // Shape-based world overlays
        shapes.setProjectionMatrix(camera.combined);
        renderWalls(room, roomW);
        renderDoorMarkers(room, roomW);
        renderEnemyShapes();
        renderKeyPickups(room);

        // ── UI overlay ───────────────────────────────────────────────────────
        minimapRenderer.render(shapes, uiCamera, room);
    }

    // ── Background ────────────────────────────────────────────────────────────

    private void drawBackground(Room room, float roomW) {
        Texture bg = room != null ? getBackground(room) : fallbackFloor;
        batch.setColor(Color.WHITE);
        batch.draw(bg, 0, 0, ROOM_W, ROOM_H);
    }

    private Texture getBackground(Room room) {
        if (roomBgCache.containsKey(room.getId())) return roomBgCache.get(room.getId());

        String tierKey = tierFolderKey(room.getTier());
        String[] candidates = {
            "rooms/" + room.getId() + ".png",
            "rooms/" + room.getId() + ".jpg",
            "rooms/" + tierKey + ".png",
            "rooms/" + tierKey + ".jpg",
            "rooms/background.png",
            "rooms/background.jpg",
        };
        for (String path : candidates) {
            if (Gdx.files.internal(path).exists()) {
                try {
                    Texture t = new Texture(Gdx.files.internal(path));
                    t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    roomBgCache.put(room.getId(), t);
                    return t;
                } catch (RuntimeException ignored) {}
            }
        }
        roomBgCache.put(room.getId(), fallbackFloor);
        return fallbackFloor;
    }

    private String tierFolderKey(Tier tier) {
        switch (tier) {
            case SURFACE_CAVERNS: return "surface_caverns";
            case DROWNED_DEPTHS:  return "drowned_depths";
            case LAVA_SANCTUM:    return "lava_sanctum";
            default: return "surface_caverns";
        }
    }

    // ── Walls ─────────────────────────────────────────────────────────────────
    /**
     * Draws thick wall bands around the room. Where a door exists in a given
     * direction, a gap of DOOR_GAP is cut at the wall midpoint.
     */
    private void renderWalls(Room room, float roomW) {
        if (room == null) return;

        boolean hasW = room.getDoor(Direction.WEST)  != null;
        boolean hasE = room.getDoor(Direction.EAST)  != null;
        boolean hasN = room.getDoor(Direction.NORTH) != null;
        boolean hasS = room.getDoor(Direction.SOUTH) != null;

        float wt      = WALL_T;
        float hg      = DOOR_GAP * 0.5f;
        float midX    = ROOM_W * 0.5f;
        float midY    = ROOM_H * 0.5f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.12f, 0.10f, 0.09f, 1f);

        // Bottom wall
        if (hasS) {
            shapes.rect(0,              0, midX - hg, wt);
            shapes.rect(midX + hg,      0, ROOM_W - midX - hg, wt);
        } else {
            shapes.rect(0, 0, ROOM_W, wt);
        }
        // Top wall
        if (hasN) {
            shapes.rect(0,              ROOM_H - wt, midX - hg, wt);
            shapes.rect(midX + hg,      ROOM_H - wt, ROOM_W - midX - hg, wt);
        } else {
            shapes.rect(0, ROOM_H - wt, ROOM_W, wt);
        }
        // Left wall
        if (hasW) {
            shapes.rect(0, 0,           wt, midY - hg);
            shapes.rect(0, midY + hg,   wt, ROOM_H - midY - hg);
        } else {
            shapes.rect(0, 0, wt, ROOM_H);
        }
        // Right wall
        if (hasE) {
            shapes.rect(ROOM_W - wt, 0,          wt, midY - hg);
            shapes.rect(ROOM_W - wt, midY + hg,  wt, ROOM_H - midY - hg);
        } else {
            shapes.rect(ROOM_W - wt, 0, wt, ROOM_H);
        }

        shapes.end();
    }

    /**
     * Draws a coloured rectangle in each door gap so the player can see the exit.
     * Open doors → green, locked → orange, closed (unlocked) → grey.
     */
    private void renderDoorMarkers(Room room, float roomW) {
        if (room == null) return;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Door door : room.getDoors()) {
            Color c = door.isOpen() ? new Color(0.2f, 0.7f, 0.3f, 1f)
                    : door.isLocked() ? new Color(0.75f, 0.50f, 0.05f, 1f)
                    : new Color(0.45f, 0.45f, 0.45f, 1f);
            shapes.setColor(c);

            float hg   = DOOR_GAP * 0.5f;
            float wt   = WALL_T;
            float midX = ROOM_W * 0.5f;
            float midY = ROOM_H * 0.5f;

            switch (door.getDirection()) {
                case WEST:  shapes.rect(0,           midY - hg, wt, DOOR_GAP); break;
                case EAST:  shapes.rect(ROOM_W - wt, midY - hg, wt, DOOR_GAP); break;
                case NORTH: shapes.rect(midX - hg, ROOM_H - wt, DOOR_GAP, wt); break;
                case SOUTH: shapes.rect(midX - hg, 0,           DOOR_GAP, wt); break;
            }
        }
        shapes.end();
    }

    // ── Collision clamping ────────────────────────────────────────────────────

    private void clampPlayer(float roomW) {
        float half = SPRITE_SIZE * 0.5f;
        float x = MathUtils.clamp(player.getX(), WALL_T + half, ROOM_W - WALL_T - half);
        float y = MathUtils.clamp(player.getY(), WALL_T + half, ROOM_H - WALL_T - half);
        player.getContext().setPosition(x, y);
    }

    private void clampEnemies(float roomW) {
        float half = ENEMY_SIZE * 0.5f;
        float minX = WALL_T + half, maxX = ROOM_W - WALL_T - half;
        float minY = WALL_T + half, maxY = ROOM_H - WALL_T - half;
        for (Enemy e : combatManager.getEnemies()) {
            float x = MathUtils.clamp(e.getX(), minX, maxX);
            float y = MathUtils.clamp(e.getY(), minY, maxY);
            if (x != e.getX() || y != e.getY()) e.getContext().setPosition(x, y);
        }
    }

    // ── Key pickups ───────────────────────────────────────────────────────────

    private void renderKeyPickups(Room room) {
        if (room == null) return;
        List<Key> keys = room.getKeys();
        if (keys.isEmpty()) return;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.YELLOW);
        for (Key key : keys) {
            if (!key.isCollected()) {
                shapes.circle(key.getPosition().x, key.getPosition().y, 0.25f, 12);
            }
        }
        shapes.end();

        for (Key key : keys) {
            if (!key.isCollected()) {
                float dist = dst(player.getX(), player.getY(),
                        key.getPosition().x, key.getPosition().y);
                if (dist < 0.8f) {
                    key.collect();
                    keyInventory.addKey(key.getId());
                }
            }
        }
    }

    // ── Entity rendering ──────────────────────────────────────────────────────

    private void drawPlayer() {
        Texture frame = currentPlayerFrame();
        playerOrientation.update(player.getContext().getFacing());
        float half = SPRITE_SIZE * 0.5f;
        batch.draw(frame,
                player.getX() - half, player.getY() - half, SPRITE_SIZE, SPRITE_SIZE,
                0, 0, frame.getWidth(), frame.getHeight(), playerOrientation.isFlipX(), false);
    }

    private Texture currentPlayerFrame() {
        if (player.getCurrentState() != AttackingState.INSTANCE) return karinIdleTexture;
        float t = player.getContext().getStateTimer();
        return t < PlayerContext.ATTACK_DURATION * ATTACK_WINDUP
                ? karinAttackWindupTexture : karinAttackStrikeTexture;
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
            batch.draw(tex, e.getX() - half, e.getY() - half, ENEMY_SIZE, ENEMY_SIZE,
                    0, 0, tex.getWidth(), tex.getHeight(), orient.isFlipX(), false);
        }
    }

    private void renderEnemyShapes() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : combatManager.getEnemies()) {
            if (!enemySprites.has(e.getType())) {
                shapes.setColor(e.isDead() ? Color.DARK_GRAY : colorFor(e.getType()));
                shapes.rect(e.getX() - ENEMY_SIZE * 0.5f, e.getY() - ENEMY_SIZE * 0.5f,
                        ENEMY_SIZE, ENEMY_SIZE);
            }
            if (!e.isDead()) {
                float hpPct = e.getCurrentHp() / (float) e.getType().getMaxHp();
                float bx = e.getX() - ENEMY_SIZE * 0.5f;
                float by = e.getY() + ENEMY_SIZE * 0.5f + 0.04f;
                shapes.setColor(Color.DARK_GRAY);
                shapes.rect(bx, by, ENEMY_SIZE, 0.1f);
                shapes.setColor(Color.SCARLET);
                shapes.rect(bx, by, ENEMY_SIZE * hpPct, 0.1f);
            }
        }
        shapes.end();
    }

    private Color colorFor(EnemyType type) {
        switch (type) {
            case SHADOW_GOBLIN:  return Color.FOREST;
            case MOSS_CRAWLER:   return Color.OLIVE;
            case STONE_WATCHER:  return Color.GRAY;
            case BONE_ARCHER:    return Color.WHITE;
            case DROWNER:        return Color.BLUE;
            case RIFT_JELLYFISH: return Color.PURPLE;
            case RIFT_KNIGHT:    return Color.NAVY;
            case LAVA_SNAKE:     return Color.ORANGE;
            case SLAG_ELEMENTAL: return Color.SCARLET;
            case MALTARION_ECHO: return Color.MAGENTA;
            default:             return Color.RED;
        }
    }

    // ── Resize ────────────────────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        if (height == 0) return;
        ROOM_W = ROOM_H * width / (float) height;
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
        Room room = roomManager.getCurrentRoom();
        if (room != null) applyCameraForRoom(room);
    }

    // ── Dispose ───────────────────────────────────────────────────────────────

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
        karinIdleTexture.dispose();
        if (karinAttackWindupTexture != karinIdleTexture) karinAttackWindupTexture.dispose();
        if (karinAttackStrikeTexture != karinIdleTexture) karinAttackStrikeTexture.dispose();
        fallbackFloor.dispose();
        for (Texture t : roomBgCache.values()) {
            if (t != fallbackFloor) t.dispose();
        }
        if (enemySprites != null) enemySprites.dispose();
        musicPlayer.dispose();
        if (combatManager != null) combatManager.dispose();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private float dst(float ax, float ay, float bx, float by) {
        float dx = ax - bx, dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private Texture tryLoadTexture(String path, Color fallback) {
        if (Gdx.files.internal(path).exists()) {
            try {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                return t;
            } catch (RuntimeException ignored) {}
        }
        return solidTexture(fallback);
    }

    private Texture tryLoadOrFallback(String path, Texture fallback) {
        if (Gdx.files.internal(path).exists()) {
            try {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                return t;
            } catch (RuntimeException ignored) {}
        }
        return fallback;
    }

    private Texture solidTexture(Color color) {
        Pixmap p = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        p.setColor(color);
        p.fill();
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    private Texture buildDotGridTexture(Color base, Color dot) {
        int sz = 64;
        Pixmap p = new Pixmap(sz, sz, Pixmap.Format.RGBA8888);
        p.setColor(base);
        p.fill();
        p.setColor(dot);
        for (int x = 0; x < sz; x += 8)
            for (int y = 0; y < sz; y += 8)
                p.fillRectangle(x, y, 1, 1);
        Texture t = new Texture(p);
        t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        p.dispose();
        return t;
    }
}
