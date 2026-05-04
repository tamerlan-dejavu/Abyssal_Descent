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

    // Room is always 9 units tall; width comes from RoomSize
    private static final float ROOM_H          = 9f;
    // Wall thickness in world units — entities cannot enter this band
    private static final float WALL_THICKNESS  = 0.5f;
    // Door opening width (gap in the wall)
    private static final float DOOR_GAP        = 1.5f;

    private static final float SPRITE_SIZE     = 0.8f;
    private static final float ENEMY_SIZE      = 0.75f;
    private static final float DOOR_SIZE       = 0.9f;
    private static final float ATTACK_WINDUP_FRACTION = 0.4f;
    private static final float DOOR_INTERACT_RANGE    = 2.0f;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private OrthographicCamera camera;

    // UI camera fixed to screen coords for minimap overlay
    private OrthographicCamera uiCamera;

    private Texture karinIdleTexture;
    private Texture karinAttackWindupTexture;
    private Texture karinAttackStrikeTexture;
    private Texture fallbackFloor;
    private Texture fallbackDoor;
    private Texture fallbackDoorLocked;
    private Texture fallbackDoorOpen;

    private final Map<String, Texture> roomBgCache = new HashMap<>();
    private Texture doorTexture;
    private Texture doorLockedTexture;
    private Texture doorOpenTexture;

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

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 16f, ROOM_H);
        camera.position.set(8f, ROOM_H * 0.5f, 0);
        camera.update();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        karinIdleTexture         = tryLoadTexture("karin.png",               Color.CYAN);
        karinAttackWindupTexture = tryLoadOrFallback("karin_attack_windup.png", karinIdleTexture);
        karinAttackStrikeTexture = tryLoadOrFallback("karin_attack_strike.png", karinIdleTexture);

        fallbackFloor       = buildDotGridTexture(new Color(0.22f, 0.22f, 0.22f, 1f), new Color(0.28f, 0.28f, 0.28f, 1f));
        fallbackDoor        = solidTexture(new Color(0.5f, 0.5f, 0.5f, 1f));
        fallbackDoorLocked  = solidTexture(new Color(0.75f, 0.55f, 0.1f, 1f));
        fallbackDoorOpen    = solidTexture(new Color(0.2f, 0.75f, 0.3f, 1f));

        doorTexture         = tryLoadOrFallback("doors/door.png",        fallbackDoor);
        doorLockedTexture   = tryLoadOrFallback("doors/door_locked.png", fallbackDoorLocked);
        doorOpenTexture     = tryLoadOrFallback("doors/door_open.png",   fallbackDoorOpen);

        EventBus eventBus = EventBus.getInstance();

        keyInventory      = new KeyInventory();
        roomManager       = new RoomManager(eventBus);
        dungeon           = new DungeonGenerator(new EnemyFactory()).generate();
        roomManager.loadDungeon(dungeon);

        combatManager = new CombatManager(eventBus, new MeleeStrategy());
        combatManager.setPlayerBaseDamage(15);
        enemySprites  = new EnemySpriteRegistry();

        doorOpeningSystem = new DoorOpeningSystem(keyInventory, roomManager, eventBus);
        new RoomEventHandler(roomManager, eventBus);
        new PlayerInteractionSystem(roomManager, doorOpeningSystem, eventBus);

        eventBus.subscribe(RoomEnteredEvent.class,    this::onRoomEntered);
        eventBus.subscribe(TierTransitionEvent.class, this::onTierTransition);

        minimapRenderer = new MinimapRenderer(dungeon);

        Room startRoom = dungeon.getRoom(dungeon.getStartRoomId());
        player = new Player(startRoom.getSpawnPoint().x, startRoom.getSpawnPoint().y);

        inputHandler = new PlayerInputHandler(player);
        Gdx.input.setInputProcessor(inputHandler);
        inputHandler.setCamera(camera);

        loadRoomEnemies(startRoom);
        updateCameraForRoom(startRoom);
        musicPlayer.start();
    }

    private void onRoomEntered(RoomEnteredEvent event) {
        Room room = dungeon.getRoom(event.getRoomId());
        if (room == null) return;

        combatManager.getEnemies().clear();
        enemyOrientations.clear();
        enemyLastX.clear();

        loadRoomEnemies(room);
        player.getContext().setPosition(room.getSpawnPoint().x, room.getSpawnPoint().y);
        updateCameraForRoom(room);
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

    /** Re-centres the camera and sets its viewport to show the full room width. */
    private void updateCameraForRoom(Room room) {
        float roomW = room.getWidth();
        float aspect = (float) Gdx.graphics.getWidth() / Math.max(1, Gdx.graphics.getHeight());
        // Show full room width; adjust height to maintain aspect ratio
        camera.viewportWidth  = roomW;
        camera.viewportHeight = roomW / aspect;
        camera.position.set(roomW * 0.5f, ROOM_H * 0.5f, 0);
        camera.update();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        Room room = roomManager.getCurrentRoom();
        float roomW = room != null ? room.getWidth() : 16f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            EventBus.getInstance().post(new PlayerInteractionEvent(
                    player.getX(), player.getY(), DOOR_INTERACT_RANGE));
        }

        inputHandler.update();
        player.update(dt);
        clampPlayer(roomW, room);

        for (Enemy e : combatManager.getEnemies()) {
            e.observeTarget(player.getX(), player.getY(), true);
        }
        combatManager.update(dt);
        clampEnemies(roomW, room);

        ScreenUtils.clear(0, 0, 0, 1f);

        // ── World rendering ──────────────────────────────────────────────────
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawRoomBackground(room, roomW);
        drawPlayer();
        drawEnemySprites();
        drawDoors(room, roomW);
        batch.end();

        shapes.setProjectionMatrix(camera.combined);
        renderWalls(room, roomW);
        renderEnemyShapes();
        renderKeyPickups(room);

        // ── UI overlay (minimap) ─────────────────────────────────────────────
        minimapRenderer.render(shapes, uiCamera, room);
    }

    // ── Wall rendering ────────────────────────────────────────────────────────
    /**
     * Draws solid wall rectangles around the room perimeter.
     * Door openings are left as gaps. Entities are clamped outside walls.
     */
    private void renderWalls(Room room, float roomW) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.15f, 0.12f, 0.10f, 1f);

        float wt = WALL_THICKNESS;
        float halfGap = DOOR_GAP * 0.5f;

        // Floor (bottom wall)
        shapes.rect(0, 0, roomW, wt);
        // Ceiling (top wall)
        shapes.rect(0, ROOM_H - wt, roomW, wt);

        // West wall — check for WEST door gap
        boolean hasWest  = room != null && room.getDoor(Direction.WEST)  != null;
        boolean hasEast  = room != null && room.getDoor(Direction.EAST)  != null;
        boolean hasNorth = room != null && room.getDoor(Direction.NORTH) != null;
        boolean hasSouth = room != null && room.getDoor(Direction.SOUTH) != null;

        float midY = ROOM_H * 0.5f;
        float midX = roomW  * 0.5f;

        // West wall
        if (hasWest) {
            shapes.rect(0, 0,              wt, midY - halfGap);               // below gap
            shapes.rect(0, midY + halfGap, wt, ROOM_H - midY - halfGap - wt);// above gap
        } else {
            shapes.rect(0, wt, wt, ROOM_H - wt * 2f);
        }

        // East wall
        if (hasEast) {
            shapes.rect(roomW - wt, 0,              wt, midY - halfGap);
            shapes.rect(roomW - wt, midY + halfGap, wt, ROOM_H - midY - halfGap - wt);
        } else {
            shapes.rect(roomW - wt, wt, wt, ROOM_H - wt * 2f);
        }

        // North wall (top)
        if (hasNorth) {
            shapes.rect(wt,                  ROOM_H - wt, midX - halfGap - wt, wt);
            shapes.rect(midX + halfGap,      ROOM_H - wt, roomW - midX - halfGap - wt, wt);
        } else {
            shapes.rect(wt, ROOM_H - wt, roomW - wt * 2f, wt);
        }

        // South wall (bottom)
        if (hasSouth) {
            shapes.rect(wt,             0, midX - halfGap - wt, wt);
            shapes.rect(midX + halfGap, 0, roomW - midX - halfGap - wt, wt);
        } else {
            shapes.rect(wt, 0, roomW - wt * 2f, wt);
        }

        shapes.end();
    }

    // ── Collision helpers ─────────────────────────────────────────────────────
    /**
     * Clamps an entity inside the room, respecting wall thickness.
     * If a wall direction has a door, entities can enter the door gap zone
     * but not pass fully through (the RoomManager handles the actual transition
     * when E is pressed near a door).
     */
    private void clampPlayer(float roomW, Room room) {
        float half = SPRITE_SIZE * 0.5f;
        float wt   = WALL_THICKNESS;

        float minX = wt + half;
        float maxX = roomW - wt - half;
        float minY = wt + half;
        float maxY = ROOM_H - wt - half;

        float x = MathUtils.clamp(player.getX(), minX, maxX);
        float y = MathUtils.clamp(player.getY(), minY, maxY);
        player.getContext().setPosition(x, y);
    }

    private void clampEnemies(float roomW, Room room) {
        float half = ENEMY_SIZE * 0.5f;
        float wt   = WALL_THICKNESS;

        float minX = wt + half;
        float maxX = roomW - wt - half;
        float minY = wt + half;
        float maxY = ROOM_H - wt - half;

        for (Enemy e : combatManager.getEnemies()) {
            float x = MathUtils.clamp(e.getX(), minX, maxX);
            float y = MathUtils.clamp(e.getY(), minY, maxY);
            if (x != e.getX() || y != e.getY()) {
                e.getContext().setPosition(x, y);
            }
        }
    }

    // ── Room background ───────────────────────────────────────────────────────
    private void drawRoomBackground(Room room, float roomW) {
        Texture bg = room != null ? getRoomBackground(room) : fallbackFloor;
        batch.setColor(Color.WHITE);
        batch.draw(bg, 0, 0, roomW, ROOM_H);
    }

    private Texture getRoomBackground(Room room) {
        if (roomBgCache.containsKey(room.getId())) return roomBgCache.get(room.getId());

        String[] candidates = {
            "rooms/" + room.getId() + ".png",
            "rooms/" + room.getId() + ".jpg",
            "rooms/" + tierFolderKey(room.getTier()) + ".png",
            "rooms/" + tierFolderKey(room.getTier()) + ".jpg",
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

    // ── Door rendering ────────────────────────────────────────────────────────
    private void drawDoors(Room room, float roomW) {
        if (room == null) return;

        for (Door door : room.getDoors()) {
            float dx = doorCenterX(door, roomW);
            float dy = doorCenterY(door);
            float hw = DOOR_SIZE * 0.5f;

            Texture tex;
            if (door.isOpen()) {
                tex = doorOpenTexture;
            } else if (door.isLocked()) {
                tex = doorLockedTexture;
            } else {
                tex = doorTexture;
            }

            batch.setColor(Color.WHITE);
            batch.draw(tex, dx - hw, dy - hw, DOOR_SIZE, DOOR_SIZE);
        }
    }

    private float doorCenterX(Door door, float roomW) {
        switch (door.getDirection()) {
            case EAST:  return roomW - WALL_THICKNESS * 0.5f;
            case WEST:  return WALL_THICKNESS * 0.5f;
            default:    return roomW * 0.5f;
        }
    }

    private float doorCenterY(Door door) {
        switch (door.getDirection()) {
            case NORTH: return ROOM_H - WALL_THICKNESS * 0.5f;
            case SOUTH: return WALL_THICKNESS * 0.5f;
            default:    return ROOM_H * 0.5f;
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
                float dist = dst(player.getX(), player.getY(), key.getPosition().x, key.getPosition().y);
                if (dist < 0.8f) {
                    key.collect();
                    keyInventory.addKey(key.getId());
                }
            }
        }
    }

    // ── Enemy rendering ───────────────────────────────────────────────────────
    private void drawEnemySprites() {
        for (Enemy e : combatManager.getEnemies()) {
            Texture tex = enemySprites.get(e.getType());
            if (tex == null) continue;
            SpriteOrientation orient = enemyOrientations.computeIfAbsent(e.getId(), id -> new SpriteOrientation());
            Float prevX = enemyLastX.get(e.getId());
            if (prevX != null) orient.update(e.getX() - prevX);
            enemyLastX.put(e.getId(), e.getX());
            float half = ENEMY_SIZE * 0.5f;
            batch.draw(tex, e.getX() - half, e.getY() - half, ENEMY_SIZE, ENEMY_SIZE,
                    0, 0, tex.getWidth(), tex.getHeight(), orient.isFlipX(), false);
        }
    }

    private void drawPlayer() {
        Texture frame = currentPlayerFrame();
        playerOrientation.update(player.getContext().getFacing());
        float half = SPRITE_SIZE * 0.5f;
        batch.draw(frame, player.getX() - half, player.getY() - half, SPRITE_SIZE, SPRITE_SIZE,
                0, 0, frame.getWidth(), frame.getHeight(), playerOrientation.isFlipX(), false);
    }

    private Texture currentPlayerFrame() {
        if (player.getCurrentState() != AttackingState.INSTANCE) return karinIdleTexture;
        float t = player.getContext().getStateTimer();
        return t < PlayerContext.ATTACK_DURATION * ATTACK_WINDUP_FRACTION
                ? karinAttackWindupTexture : karinAttackStrikeTexture;
    }

    private void renderEnemyShapes() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : combatManager.getEnemies()) {
            if (!enemySprites.has(e.getType())) {
                shapes.setColor(e.isDead() ? Color.DARK_GRAY : colorFor(e.getType()));
                shapes.rect(e.getX() - ENEMY_SIZE * 0.5f, e.getY() - ENEMY_SIZE * 0.5f, ENEMY_SIZE, ENEMY_SIZE);
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
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();

        Room room = roomManager.getCurrentRoom();
        if (room != null) {
            updateCameraForRoom(room);
        } else {
            float aspect = (float) width / height;
            camera.viewportWidth  = ROOM_H * aspect;
            camera.viewportHeight = ROOM_H;
            camera.position.set(8f, ROOM_H * 0.5f, 0);
            camera.update();
        }
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
        fallbackDoor.dispose();
        fallbackDoorLocked.dispose();
        fallbackDoorOpen.dispose();
        for (Texture t : roomBgCache.values()) {
            if (t != fallbackFloor) t.dispose();
        }
        if (enemySprites != null) enemySprites.dispose();
        musicPlayer.dispose();
        if (combatManager != null) combatManager.dispose();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────
    private float dst(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
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
        int size = 32;
        Pixmap p = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        p.setColor(base);
        p.fill();
        p.setColor(dot);
        for (int x = 0; x < size; x += 8) {
            for (int y = 0; y < size; y += 8) {
                p.fillRectangle(x, y, 1, 1);
            }
        }
        Texture t = new Texture(p);
        t.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        p.dispose();
        return t;
    }
}
