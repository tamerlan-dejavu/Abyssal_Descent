package com.abyssaldescent.ui;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.audio.MusicPlayer;
import com.abyssaldescent.combat.CombatManager;
import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyFactory;
import com.abyssaldescent.entity.player.Player;
import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.entity.player.PlayerInputHandler;
import com.abyssaldescent.entity.state.AttackingState;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerInteractionEvent;
import com.abyssaldescent.event.RoomEnteredEvent;
import com.abyssaldescent.event.TierTransitionEvent;
import com.abyssaldescent.render.CameraController;
import com.abyssaldescent.render.EnemySpriteRegistry;
import com.abyssaldescent.render.SpriteOrientation;
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
    private static final float VIEWPORT_HEIGHT = 9f;
    private static final float VIEWPORT_WIDTH = 16f;
    private static final float SPRITE_SIZE = 1f;
    private static final float ENEMY_SIZE = 0.9f;
    private static final float ATTACK_WINDUP_FRACTION = 0.4f;
    private static final float DOOR_INTERACT_RANGE = 2.5f;

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

    private Dungeon dungeon;
    private RoomManager roomManager;
    private KeyInventory keyInventory;
    private DoorOpeningSystem doorOpeningSystem;
    private Tier currentTier = Tier.SURFACE_CAVERNS;

    @Override
    public void create() {
        GameStateManager.getInstance().activateKarin();

        batch = new SpriteBatch();
        shapes = new ShapeRenderer();

        karinIdleTexture = loadTexture("karin.png", Color.CYAN);
        karinAttackWindupTexture = loadAttackFrame("karin_attack_windup.png");
        karinAttackStrikeTexture = loadAttackFrame("karin_attack_strike.png");
        floorTexture = buildFloorTexture();

        EventBus eventBus = EventBus.getInstance();

        keyInventory = new KeyInventory();
        roomManager = new RoomManager(eventBus);

        dungeon = new DungeonGenerator(new EnemyFactory()).generate();
        roomManager.loadDungeon(dungeon);

        combatManager = new CombatManager(eventBus, new com.abyssaldescent.combat.strategy.MeleeStrategy());
        combatManager.setPlayerBaseDamage(15);
        enemySprites = new EnemySpriteRegistry();

        doorOpeningSystem = new DoorOpeningSystem(keyInventory, roomManager, eventBus);
        new RoomEventHandler(roomManager, eventBus);
        new PlayerInteractionSystem(roomManager, doorOpeningSystem, eventBus);

        eventBus.subscribe(RoomEnteredEvent.class, this::onRoomEntered);
        eventBus.subscribe(TierTransitionEvent.class, this::onTierTransition);

        Room startRoom = dungeon.getRoom(dungeon.getStartRoomId());
        player = new Player(startRoom.getSpawnPoint().x, startRoom.getSpawnPoint().y);

        inputHandler = new PlayerInputHandler(player);
        Gdx.input.setInputProcessor(inputHandler);

        cameraController = new CameraController(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        cameraController.setTarget(player.getX(), player.getY());
        cameraController.snapToTarget();
        inputHandler.setCamera(cameraController.getCamera());

        musicPlayer.start();

        loadRoomEnemies(startRoom);
    }

    private void onRoomEntered(RoomEnteredEvent event) {
        Room room = dungeon.getRoom(event.getRoomId());
        if (room == null) return;

        combatManager.getEnemies().clear();
        enemyOrientations.clear();
        enemyLastX.clear();

        loadRoomEnemies(room);

        float w = room.getWidth();
        float h = room.getHeight();
        cameraController.setWorldBounds(w, h);
        player.getContext().setPosition(room.getSpawnPoint().x, room.getSpawnPoint().y);
    }

    private void onTierTransition(TierTransitionEvent event) {
        currentTier = event.getToTier();
    }

    private void loadRoomEnemies(Room room) {
        for (Enemy enemy : room.getEnemies()) {
            combatManager.addEnemy(enemy);
        }
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            EventBus.getInstance().post(new PlayerInteractionEvent(
                    player.getX(), player.getY(), DOOR_INTERACT_RANGE));
        }

        inputHandler.update();
        player.update(dt);

        Room room = roomManager.getCurrentRoom();
        if (room != null) {
            clampPlayer(room.getWidth(), room.getHeight());
        }

        for (Enemy e : combatManager.getEnemies()) {
            e.observeTarget(player.getX(), player.getY(), true);
        }
        combatManager.update(dt);

        if (room != null) {
            clampEnemies(room.getWidth(), room.getHeight());
        }

        cameraController.setTarget(player.getX(), player.getY());
        cameraController.update(dt);

        ScreenUtils.clear(0, 0, 0, 1f);

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        batch.begin();
        if (room != null) drawRoom(room);
        drawPlayer();
        drawEnemySprites();
        batch.end();

        shapes.setProjectionMatrix(cameraController.getCamera().combined);
        renderEnemyShapes();
        if (room != null) renderDoors(room);
        renderKeyPickups(room);
    }

    private void drawRoom(Room room) {
        Tier tier = room.getTier();
        batch.setColor(tier.getAmbientR(), tier.getAmbientG(), tier.getAmbientB(), 1f);
        batch.draw(floorTexture, 0, 0, room.getWidth(), room.getHeight());
        batch.setColor(Color.WHITE);
    }

    private void renderDoors(Room room) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Door door : room.getDoors()) {
            float dx = doorX(door, room);
            float dy = doorY(door, room);

            if (door.isOpen()) {
                shapes.setColor(0.2f, 0.8f, 0.3f, 1f);
            } else if (door.isLocked()) {
                shapes.setColor(0.8f, 0.6f, 0.1f, 1f);
            } else {
                shapes.setColor(0.5f, 0.5f, 0.5f, 1f);
            }
            shapes.rect(dx - 0.4f, dy - 0.6f, 0.8f, 1.2f);
        }
        shapes.end();
    }

    private void renderKeyPickups(Room room) {
        if (room == null) return;
        List<Key> keys = room.getKeys();
        if (keys.isEmpty()) return;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.YELLOW);
        for (Key key : keys) {
            if (!key.isCollected()) {
                shapes.circle(key.getPosition().x, key.getPosition().y, 0.3f, 12);
            }
        }
        shapes.end();

        for (Key key : keys) {
            if (!key.isCollected()) {
                float dist = dst(player.getX(), player.getY(), key.getPosition().x, key.getPosition().y);
                if (dist < 1.0f) {
                    key.collect();
                    keyInventory.addKey(key.getId());
                }
            }
        }
    }

    private float doorX(Door door, Room room) {
        switch (door.getDirection()) {
            case EAST:  return room.getWidth() - 0.5f;
            case WEST:  return 0.5f;
            default:    return room.getWidth() * 0.5f;
        }
    }

    private float doorY(Door door, Room room) {
        switch (door.getDirection()) {
            case NORTH: return room.getHeight() - 0.5f;
            case SOUTH: return 0.5f;
            default:    return room.getHeight() * 0.5f;
        }
    }

    private float dst(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void clampPlayer(float w, float h) {
        float halfW = SPRITE_SIZE * 0.5f;
        float x = MathUtils.clamp(player.getX(), halfW, w - halfW);
        float y = MathUtils.clamp(player.getY(), PlayerContext.GROUND_Y, h - halfW);
        player.getContext().setPosition(x, y);
    }

    private void clampEnemies(float w, float h) {
        float half = ENEMY_SIZE * 0.5f;
        for (Enemy e : combatManager.getEnemies()) {
            float x = MathUtils.clamp(e.getX(), half, w - half);
            float y = MathUtils.clamp(e.getY(), half, h - half);
            if (x != e.getX() || y != e.getY()) {
                e.getContext().setPosition(x, y);
            }
        }
    }

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
        PlayerContext ctx = player.getContext();
        playerOrientation.update(ctx.getFacing());
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
                float by = e.getY() + ENEMY_SIZE * 0.5f + 0.05f;
                shapes.setColor(Color.DARK_GRAY);
                shapes.rect(bx, by, ENEMY_SIZE, 0.12f);
                shapes.setColor(Color.RED);
                shapes.rect(bx, by, ENEMY_SIZE * hpPct, 0.12f);
            }
        }
        shapes.end();
    }

    private Color colorFor(com.abyssaldescent.entity.enemy.EnemyType type) {
        switch (type) {
            case SHADOW_GOBLIN:   return Color.FOREST;
            case MOSS_CRAWLER:    return Color.OLIVE;
            case STONE_WATCHER:   return Color.GRAY;
            case BONE_ARCHER:     return Color.WHITE;
            case DROWNER:         return Color.BLUE;
            case RIFT_JELLYFISH:  return Color.PURPLE;
            case RIFT_KNIGHT:     return Color.NAVY;
            case LAVA_SNAKE:      return Color.ORANGE;
            case SLAG_ELEMENTAL:  return Color.SCARLET;
            case MALTARION_ECHO:  return Color.MAGENTA;
            default:              return Color.RED;
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
        karinIdleTexture.dispose();
        if (karinAttackWindupTexture != karinIdleTexture) karinAttackWindupTexture.dispose();
        if (karinAttackStrikeTexture != karinIdleTexture) karinAttackStrikeTexture.dispose();
        floorTexture.dispose();
        if (enemySprites != null) enemySprites.dispose();
        musicPlayer.dispose();
        if (combatManager != null) combatManager.dispose();
    }

    private Texture loadTexture(String path, Color fallback) {
        Texture t;
        if (Gdx.files.internal(path).exists()) {
            try {
                t = new Texture(path);
            } catch (RuntimeException e) {
                t = solidTexture(fallback);
            }
        } else {
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
            } catch (RuntimeException ignored) {}
        }
        return karinIdleTexture;
    }

    private Texture solidTexture(Color color) {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture t = new Texture(pixmap);
        pixmap.dispose();
        return t;
    }

    private Texture buildFloorTexture() {
        int size = 16;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        pixmap.setColor(0.85f, 0.85f, 0.85f, 1f);
        for (int x = 0; x < size; x += 4) {
            for (int y = 0; y < size; y += 4) {
                pixmap.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pixmap);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return t;
    }
}
