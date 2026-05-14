package com.abyssaldescent.ui.screen;

import com.abyssaldescent.GameController;
import com.abyssaldescent.GamePhase;
import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.audio.MusicPlayer;
import com.abyssaldescent.combat.CombatManager;
import com.abyssaldescent.combat.chips.ChipInventory;
import com.abyssaldescent.combat.chips.ChipPickupSystem;
import com.abyssaldescent.config.DifficultySettings;
import com.abyssaldescent.dungeon.Chest;
import com.abyssaldescent.dungeon.Direction;
import com.abyssaldescent.dungeon.Door;
import com.abyssaldescent.dungeon.DungeonManager;
import com.abyssaldescent.dungeon.Room;
import com.abyssaldescent.dungeon.RoomType;
import com.abyssaldescent.dungeon.Tier;
import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyAttackEffect;
import com.abyssaldescent.entity.enemy.EnemyAttackEffectSystem;
import com.abyssaldescent.entity.enemy.EnemyFactory;
import com.abyssaldescent.entity.enemy.EnemyType;
import com.abyssaldescent.entity.player.Player;
import com.abyssaldescent.entity.player.PlayerContext;
import com.abyssaldescent.entity.effect.BloodEffect;
import com.abyssaldescent.entity.effect.BloodEffectSystem;
import com.abyssaldescent.entity.player.PlayerEffectSystem;
import com.abyssaldescent.entity.player.PlayerInputHandler;
import com.abyssaldescent.entity.state.AttackingState;
import com.abyssaldescent.entity.state.DashingState;
import com.abyssaldescent.entity.state.PlayerState;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.EnemyAttackEvent;
import com.abyssaldescent.event.GamePhaseChangedEvent;
import com.abyssaldescent.event.KeyPickedUpEvent;
import com.abyssaldescent.event.RespawnUsedEvent;
import com.abyssaldescent.event.RoomChangedEvent;
import com.abyssaldescent.render.CameraController;
import com.abyssaldescent.render.EnemySpriteRegistry;
import com.abyssaldescent.render.SpriteOrientation;
import com.abyssaldescent.ui.hud.AllKeysCollectedWindow;
import com.abyssaldescent.ui.hud.HudRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScreen implements Screen {

    // Set in show() from the actual screen size so world coords == screen pixels.
    private int   ROOM_WIDTH;
    private int   ROOM_HEIGHT;
    private static final float SPRITE_SIZE            = 270f;  // 360 * 0.75
    private static final float ENEMY_SIZE             = 225f;  // 300 * 0.75
    private static final float ATTACK_WINDUP_FRACTION = 0.4f;

    private static final float DOOR_TRIGGER_MARGIN    = 60f;
    private static final float DOOR_INDICATOR_THICK   = 24f;
    private static final float DOOR_INDICATOR_LEN     = 200f;
    private static final float DOOR_SIDE_OFFSET       = 160f;  // offset from edge for side doors

    private final DifficultySettings difficulty;

    private SpriteBatch   batch;
    private ShapeRenderer shapes;
    private BitmapFont    font;
    private Matrix4       screenMatrix;

    private Texture karinIdleTexture;
    private Texture karinWalk1Texture;
    private Texture karinWalk2Texture;
    private Texture karinAttackWindupTexture;
    private Texture karinAttackStrikeTexture;
    private Texture karinPullPreparingTexture;
    private Texture karinPullTexture;
    private Texture karinBlockTexture;
    private Texture karinCastingTexture;
    private Texture fallbackBgTexture;

    private float walkAnimationTimer = 0f;
    private static final float WALK_FRAME_DURATION = 0.3f;
    private Texture chestClosedTexture;
    private Texture chestOpenTexture;
    private Texture doorClosedTexture;
    private Texture doorOpenTexture;
    private Texture doorEastClosedTexture;
    private Texture doorEastOpenTexture;
    private Texture doorWestClosedTexture;
    private Texture doorWestOpenTexture;
    private Texture bloodTexture1;
    private Texture bloodTexture2;

    private Player              player;
    private PlayerInputHandler  inputHandler;
    private CameraController    cameraController;
    private CombatManager       combatManager;
    private EnemyFactory        enemyFactory;

    private final SpriteOrientation              playerOrientation = new SpriteOrientation();
    private final Map<String, SpriteOrientation> enemyOrientations = new HashMap<>();
    private final Map<String, Float>             enemyLastX        = new HashMap<>();

    private final MusicPlayer musicPlayer = new MusicPlayer();

    private HudRenderer        hudRenderer;
    private ChipInventory      chipInventory;
    private ChipPickupSystem   chipPickupSystem;
    private GameController     controller;
    private PlayerEffectSystem playerEffectSystem;
    private EnemySpriteRegistry enemySprites;
    private AllKeysCollectedWindow allKeysWindow;
    private EnemyAttackEffectSystem attackEffectSystem;
    private BloodEffectSystem bloodEffectSystem;
    private int lastKeysCount = 0;

    private EventListener<GamePhaseChangedEvent> phaseListener;
    private EventListener<EnemyAttackEvent> attackListener;

    private int demoKeys     = 0;
    private int demoRespawns = 3;

    private GameOverStats pendingGameOverStats = null;

    // Tracks which room we last entered so we only respawn enemies on actual transitions
    private String lastEnteredRoomId = null;

    public GameScreen(DifficultySettings difficulty) {
        this.difficulty = difficulty;
    }

    // ── lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public void show() {
        ROOM_WIDTH  = Gdx.graphics.getWidth();
        ROOM_HEIGHT = Gdx.graphics.getHeight();

        GameStateManager.getInstance().initWithDifficulty(difficulty);

        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();
        font   = new BitmapFont();
        font.getData().setScale(1.6f);

        karinIdleTexture           = loadTextureOrPlaceholder("karin/karin.png", Color.CYAN);
        karinWalk1Texture          = loadTextureOrPlaceholder("karin/karin_walk_1.png", Color.CYAN);
        karinWalk2Texture          = loadTextureOrPlaceholder("karin/karin_walk_2.png", Color.CYAN);
        karinAttackWindupTexture   = loadAttackFrame("karin/karin_attack_windup.png");
        karinAttackStrikeTexture   = loadAttackFrame("karin/karin_attack_strike.png");
        karinPullPreparingTexture  = loadTextureOrPlaceholder("karin/karin_pull_preparing.png", Color.CYAN);
        karinPullTexture           = loadTextureOrPlaceholder("karin/karin_pull.png", Color.CYAN);
        karinBlockTexture          = loadTextureOrPlaceholder("karin/karin_block.png", Color.CYAN);
        karinCastingTexture        = loadTextureOrPlaceholder("karin/karin_casting.png", Color.CYAN);
        fallbackBgTexture          = generateFallbackBg();
        chestClosedTexture       = loadTextureOrPlaceholder("doors/chest_closed.png", null);
        if (chestClosedTexture == null) chestClosedTexture = generateChestClosed();
        chestOpenTexture         = loadTextureOrPlaceholder("doors/chest_opened.png", null);
        if (chestOpenTexture == null) chestOpenTexture = generateChestOpen();
        doorClosedTexture        = loadTextureOrPlaceholder("doors/door_closed.png", null);
        doorOpenTexture          = loadTextureOrPlaceholder("doors/door_opened.png", null);
        doorEastClosedTexture    = loadTextureOrPlaceholder("doors/door_east_closed.png", null);
        doorEastOpenTexture      = loadTextureOrPlaceholder("doors/door_east_opened.png", null);
        doorWestClosedTexture    = loadTextureOrPlaceholder("doors/door_west_closed.png", null);
        doorWestOpenTexture      = loadTextureOrPlaceholder("doors/door_west_opened.png", null);
        bloodTexture1            = loadTextureOrPlaceholder("effects/attack/attack_effect_blood_1.png", null);
        bloodTexture2            = loadTextureOrPlaceholder("effects/attack/attack_effect_blood_2.png", null);

        player       = new Player(ROOM_WIDTH / 2f, ROOM_HEIGHT / 2f);
        inputHandler = new PlayerInputHandler(player);

        cameraController = new CameraController(ROOM_WIDTH, ROOM_HEIGHT);
        cameraController.setWorldBounds(ROOM_WIDTH, ROOM_HEIGHT);
        cameraController.setTarget(player.getX(), player.getY());
        cameraController.snapToTarget();

        combatManager = new CombatManager(EventBus.getInstance(), player.getCombatStrategy());
        combatManager.setPlayerBaseDamage(15);
        bloodEffectSystem = new BloodEffectSystem();
        combatManager.setDamageCallback((x, y) -> bloodEffectSystem.spawnEffect(x, y));
        enemySprites  = new EnemySpriteRegistry();
        enemyFactory  = new EnemyFactory();

        chipInventory    = new ChipInventory();
        chipPickupSystem = new ChipPickupSystem(
                chipInventory, combatManager,
                EventBus.getInstance(), player.getCombatStrategy());
        hudRenderer = new HudRenderer(chipInventory);
        allKeysWindow = new AllKeysCollectedWindow(font);

        playerEffectSystem = new PlayerEffectSystem(player.getContext(), EventBus.getInstance());
        attackEffectSystem = new EnemyAttackEffectSystem();

        // ── Init dungeon ──────────────────────────────────────────────────────
        DungeonManager.getInstance().setMusicPlayer(musicPlayer);
        DungeonManager.getInstance().loadTier(Tier.UPPER_RUINS);
        enterCurrentRoom();

        // ── Game controller ───────────────────────────────────────────────────
        controller = new GameController();
        controller.startNewRun();
        controller.initPlayer(player);

        phaseListener = e -> {
            if (e.getNewPhase() == GamePhase.GAME_OVER) {
                int floor    = controller.getState().getFloorNumber();
                int respUsed = controller.getMaxRespawns() - controller.getRespawnsRemaining();
                int maxResp  = controller.getMaxRespawns();
                pendingGameOverStats = new GameOverStats(floor, respUsed, maxResp);
            }
        };
        EventBus.getInstance().subscribe(GamePhaseChangedEvent.class, phaseListener);

        EventBus.getInstance().subscribe(RoomChangedEvent.class, (EventListener<RoomChangedEvent>) e -> {
            enterCurrentRoom();
        });

        attackListener = e -> {
            attackEffectSystem.spawnEffect(e.getX(), e.getY());
        };
        EventBus.getInstance().subscribe(EnemyAttackEvent.class, attackListener);

        InputMultiplexer multiplexer = new InputMultiplexer(buildHudAdapter(), inputHandler);
        Gdx.input.setInputProcessor(multiplexer);

        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void render(float delta) {
        inputHandler.update();
        player.update(delta);
        player.consumeInputFlags();
        playerEffectSystem.update(delta);
        controller.update(delta);
        clampPlayerToWorld();

        // Update walk animation timer
        walkAnimationTimer += delta;
        if (walkAnimationTimer > WALK_FRAME_DURATION * 2) {
            walkAnimationTimer = 0f;
        }

        // Check if all keys collected
        int currentKeys = DungeonManager.getInstance().getKeysCollected();
        if (currentKeys >= 4 && lastKeysCount < 4) {
            allKeysWindow.show();
        }
        lastKeysCount = currentKeys;
        allKeysWindow.update(delta);

        for (Enemy e : combatManager.getEnemies()) {
            e.observeTarget(player.getX(), player.getY(), true);
        }
        combatManager.update(delta);
        clampEnemiesToWorld();
        attackEffectSystem.update(delta);
        bloodEffectSystem.update(delta);

        cameraController.setTarget(player.getX(), player.getY());
        cameraController.update(delta);

        ScreenUtils.clear(0, 0, 0, 1f);

        int sw = Gdx.graphics.getWidth();
        int sh = Gdx.graphics.getHeight();
        screenMatrix = new Matrix4().setToOrtho2D(0, 0, sw, sh);

        drawBackground(sw, sh);

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        batch.begin();
        drawPlayer();
        drawEnemySprites();
        drawChest();
        drawAttackEffects();
        drawBloodEffects();
        batch.end();

        shapes.setProjectionMatrix(cameraController.getCamera().combined);
        renderEnemyShapes();

        drawDoorIndicators(sw, sh);
        drawRoomLabel(sw, sh);

        fireDemoEvents();
        hudRenderer.render(batch, shapes, delta);
        allKeysWindow.render(batch, shapes, sw, sh);

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
        Gdx.input.setCursorCatched(false);
        Gdx.input.setInputProcessor(null);
        if (phaseListener != null) {
            EventBus.getInstance().unsubscribe(GamePhaseChangedEvent.class, phaseListener);
        }
        if (attackListener != null) {
            EventBus.getInstance().unsubscribe(EnemyAttackEvent.class, attackListener);
        }
    }

    @Override
    public void dispose() {
        try {
            if (batch != null)               batch.dispose();
            if (shapes != null)              shapes.dispose();
            if (font != null)                font.dispose();
            if (karinIdleTexture != null)    karinIdleTexture.dispose();
            if (karinWalk1Texture != null)   karinWalk1Texture.dispose();
            if (karinWalk2Texture != null)   karinWalk2Texture.dispose();
            if (karinAttackWindupTexture != null && karinAttackWindupTexture != karinIdleTexture)
                karinAttackWindupTexture.dispose();
            if (karinAttackStrikeTexture != null && karinAttackStrikeTexture != karinIdleTexture)
                karinAttackStrikeTexture.dispose();
            if (karinPullPreparingTexture != null)  karinPullPreparingTexture.dispose();
            if (karinPullTexture != null)          karinPullTexture.dispose();
            if (karinBlockTexture != null)         karinBlockTexture.dispose();
            if (karinCastingTexture != null)       karinCastingTexture.dispose();
            if (fallbackBgTexture != null)   fallbackBgTexture.dispose();
            if (chestClosedTexture != null)  chestClosedTexture.dispose();
            if (chestOpenTexture != null)    chestOpenTexture.dispose();
            if (doorClosedTexture != null)   doorClosedTexture.dispose();
            if (doorOpenTexture != null)     doorOpenTexture.dispose();
            if (doorEastClosedTexture != null)   doorEastClosedTexture.dispose();
            if (doorEastOpenTexture != null)     doorEastOpenTexture.dispose();
            if (doorWestClosedTexture != null)   doorWestClosedTexture.dispose();
            if (doorWestOpenTexture != null)     doorWestOpenTexture.dispose();
            if (bloodTexture1 != null)           bloodTexture1.dispose();
            if (bloodTexture2 != null)           bloodTexture2.dispose();
            if (enemySprites != null)        enemySprites.dispose();
            if (musicPlayer != null)         musicPlayer.dispose();
            if (combatManager != null)       combatManager.dispose();
            if (chipPickupSystem != null)    chipPickupSystem.dispose();
            if (hudRenderer != null)         hudRenderer.dispose();
            if (playerEffectSystem != null)  playerEffectSystem.dispose();
            if (controller != null)          controller.dispose();
            if (allKeysWindow != null)       allKeysWindow.dispose();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error in dispose()", e);
        }
    }

    // ── dungeon room management ───────────────────────────────────────────────

    public void enterCurrentRoom() {
        Room room = DungeonManager.getInstance().getCurrentRoom();
        if (room == null) return;
        if (room.getId().equals(lastEnteredRoomId)) return;
        lastEnteredRoomId = room.getId();

        combatManager.clearEnemies();
        enemyOrientations.clear();
        enemyLastX.clear();
        attackEffectSystem.clear();
        bloodEffectSystem.clear();

        if (room.getType() == RoomType.BATTLE_ARENA) {
            spawnEnemiesForTier(room.getTier());
        }
        // STARTING, SAVE_ROOM, FINAL — no enemies
    }

    private void spawnEnemiesForTier(Tier tier) {
        float cx = ROOM_WIDTH  / 2f;
        float cy = ROOM_HEIGHT / 2f;
        float gap = 200f;
        switch (tier) {
            case UPPER_RUINS:
                combatManager.addEnemy(enemyFactory.create(EnemyType.SHADOW_GOBLIN, cx + gap,       cy));
                combatManager.addEnemy(enemyFactory.create(EnemyType.SHADOW_GOBLIN, cx - gap,       cy));
                combatManager.addEnemy(enemyFactory.create(EnemyType.MOSS_CRAWLER,  cx + gap * 2.5f, cy + gap));
                combatManager.addEnemy(enemyFactory.create(EnemyType.STONE_WATCHER, cx - gap * 2.5f, cy - gap));
                break;
            case FLOODED_CATACOMBS:
                combatManager.addEnemy(enemyFactory.create(EnemyType.BONE_ARCHER,    cx + gap,       cy - gap));
                combatManager.addEnemy(enemyFactory.create(EnemyType.DROWNER,        cx - gap,       cy + gap));
                combatManager.addEnemy(enemyFactory.create(EnemyType.RIFT_JELLYFISH, cx + gap * 2f,  cy));
                break;
            case MALTARIONS_ABYSS:
                combatManager.addEnemy(enemyFactory.create(EnemyType.RIFT_KNIGHT,    cx + gap,       cy));
                combatManager.addEnemy(enemyFactory.create(EnemyType.LAVA_SNAKE,     cx - gap,       cy));
                combatManager.addEnemy(enemyFactory.create(EnemyType.SLAG_ELEMENTAL, cx + gap * 2f,  cy + gap));
                break;
        }
    }


    // ── rendering ─────────────────────────────────────────────────────────────

    private void drawBackground(int sw, int sh) {
        Texture bg = DungeonManager.getInstance().getBackgroundTexture();
        Texture tex = (bg != null) ? bg : fallbackBgTexture;
        batch.setProjectionMatrix(screenMatrix);
        batch.setColor(Color.WHITE);
        batch.begin();
        batch.draw(tex, 0, 0, sw, sh);
        batch.end();
    }

    /** Draws door indicators on screen edges for every door in the current room. */
    private void drawDoorIndicators(int sw, int sh) {
        Room room = DungeonManager.getInstance().getCurrentRoom();
        if (room == null) return;

        List<Door> doors = room.getDoors();
        if (doors.isEmpty()) return;

        // Draw door textures if available, otherwise use shapes
        if (doorClosedTexture != null) {
            batch.setProjectionMatrix(screenMatrix);
            batch.begin();
            batch.setColor(Color.WHITE);

            DungeonManager mgr = DungeonManager.getInstance();
            boolean allKeysCollected = mgr.allKeysCollected();

            for (Door door : doors) {
                Room targetRoom = mgr.getGraph().getRoom(door.getToRoomId());
                boolean isBlockedFinalDoor = (targetRoom != null &&
                                             targetRoom.getType() == RoomType.FINAL &&
                                             !allKeysCollected);

                float cx = sw * 0.5f;
                float cy = sh * 0.5f;
                float doorSize = 277f;  // 231 * 1.2

                Texture doorTex = null;
                switch (door.getDirection()) {
                    case NORTH:
                    case SOUTH:
                        doorTex = isBlockedFinalDoor ? doorClosedTexture : doorOpenTexture;
                        break;
                    case EAST:
                        doorTex = isBlockedFinalDoor ? doorEastClosedTexture : doorEastOpenTexture;
                        break;
                    case WEST:
                        doorTex = isBlockedFinalDoor ? doorWestClosedTexture : doorWestOpenTexture;
                        break;
                    default: break;
                }
                if (doorTex == null) doorTex = doorClosedTexture;

                switch (door.getDirection()) {
                    case NORTH:
                        batch.draw(doorTex, cx - doorSize * 0.5f, sh - doorSize - 20f, doorSize, doorSize);
                        break;
                    case SOUTH:
                        batch.draw(doorTex, cx - doorSize * 0.5f, 20f, doorSize, doorSize);
                        break;
                    case EAST:
                        batch.draw(doorTex, sw - doorSize - DOOR_SIDE_OFFSET, cy - doorSize * 0.5f, doorSize, doorSize);
                        break;
                    case WEST:
                        batch.draw(doorTex, DOOR_SIDE_OFFSET - 100f, cy - doorSize * 0.5f, doorSize, doorSize);
                        break;
                    default: break;
                }
            }
            batch.end();
        } else {
            // Fallback to shape rendering
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                               com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

            shapes.setProjectionMatrix(screenMatrix);
            shapes.begin(ShapeRenderer.ShapeType.Filled);

            Color doorColor = doorColorForRoom(room);

            for (Door door : doors) {
                shapes.setColor(doorColor.r, doorColor.g, doorColor.b, 0.85f);
                float cx = sw * 0.5f;
                float cy = sh * 0.5f;
                float hw = DOOR_INDICATOR_LEN * 0.5f;
                float hh = DOOR_INDICATOR_LEN * 0.5f;

                switch (door.getDirection()) {
                    case NORTH:
                        shapes.rect(cx - hw, sh - DOOR_INDICATOR_THICK, DOOR_INDICATOR_LEN, DOOR_INDICATOR_THICK);
                        break;
                    case SOUTH:
                        shapes.rect(cx - hw, 0, DOOR_INDICATOR_LEN, DOOR_INDICATOR_THICK);
                        break;
                    case EAST:
                        shapes.rect(sw - DOOR_INDICATOR_THICK - DOOR_SIDE_OFFSET, cy - hh, DOOR_INDICATOR_THICK, DOOR_INDICATOR_LEN);
                        break;
                    case WEST:
                        shapes.rect(DOOR_SIDE_OFFSET - 100f, cy - hh, DOOR_INDICATOR_THICK, DOOR_INDICATOR_LEN);
                        break;
                    default: break;
                }
            }
            shapes.end();
            Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        }
    }

    private Color doorColorForRoom(Room room) {
        switch (room.getType()) {
            case SAVE_ROOM:  return Color.GREEN;
            case FINAL:      return Color.GOLD;
            default:         return Color.WHITE;
        }
    }

    private void drawRoomLabel(int sw, int sh) {
        Room room = DungeonManager.getInstance().getCurrentRoom();
        if (room == null) return;

        String label = roomLabel(room);
        batch.setProjectionMatrix(screenMatrix);
        batch.begin();
        font.setColor(1f, 1f, 1f, 0.75f);
        font.draw(batch, label, sw * 0.5f - label.length() * 5f, sh - 36f);
        batch.end();
    }

    private String roomLabel(Room room) {
        String tier = tierDisplayName(room.getTier());
        switch (room.getType()) {
            case STARTING:     return tier + "  —  Entrance Hall";
            case BATTLE_ARENA: return tier + "  —  Battle Arena";
            case SAVE_ROOM:    return tier + "  —  Save Room";
            case SECRET:       return tier + "  —  Secret Chamber";
            case FINAL:        return tier + "  —  Final Chamber";
            default:           return tier;
        }
    }

    private String tierDisplayName(Tier tier) {
        switch (tier) {
            case UPPER_RUINS:       return "Upper Ruins";
            case FLOODED_CATACOMBS: return "Flooded Catacombs";
            case MALTARIONS_ABYSS:  return "Maltarion's Abyss";
            default:                return tier.name();
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
                0, 0, frame.getWidth(), frame.getHeight(),
                playerOrientation.isFlipX(), false);
    }

    private Texture currentPlayerFrame() {
        PlayerState state = player.getCurrentState();

        if (state == AttackingState.INSTANCE) {
            float t = player.getContext().getStateTimer();
            float windupEnd = PlayerContext.ATTACK_DURATION * ATTACK_WINDUP_FRACTION;
            return t < windupEnd ? karinAttackWindupTexture : karinAttackStrikeTexture;
        }

        if (state == DashingState.INSTANCE) {
            float t = player.getContext().getStateTimer();
            float dashDuration = PlayerContext.DASH_DURATION;
            float prepDuration = dashDuration * 0.3f;
            return t < prepDuration ? karinPullPreparingTexture : karinPullTexture;
        }

        if (state.getName().equals("Walking")) {
            if (walkAnimationTimer < WALK_FRAME_DURATION) {
                return karinWalk1Texture;
            } else {
                return karinWalk2Texture;
            }
        }

        return karinIdleTexture;
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

            // Boss is 1.5x larger, regular enemies same size as player
            float size = (e.getType() == EnemyType.MALTARION_ECHO) ? SPRITE_SIZE * 1.5f : SPRITE_SIZE;
            float half = size * 0.5f;
            batch.draw(tex,
                    e.getX() - half, e.getY() - half,
                    size, size,
                    0, 0, tex.getWidth(), tex.getHeight(),
                    orient.isFlipX(), false);
        }
    }

    private void drawChest() {
        Room room = DungeonManager.getInstance().getCurrentRoom();
        if (room == null || room.getChest() == null) return;

        Chest chest = room.getChest();
        Texture chestTex = chest.isOpened() ? chestOpenTexture : chestClosedTexture;
        if (chestTex == null) return;

        float cx = ROOM_WIDTH / 2f;
        float cy = ROOM_HEIGHT / 2f;
        float chestSize = 127.8f;  // 213 / 2
        float half = chestSize * 0.5f;

        batch.setColor(Color.WHITE);
        batch.draw(chestTex, cx - half, cy - half, chestSize, chestSize);
    }

    public boolean collidesWithChest(float px, float py, float radius) {
        Room room = DungeonManager.getInstance().getCurrentRoom();
        if (room == null || room.getChest() == null) return false;

        float cx = ROOM_WIDTH / 2f;
        float cy = ROOM_HEIGHT / 2f;
        float chestSize = 127.8f;
        float half = chestSize * 0.4f;

        float dx = px - cx;
        float dy = py - cy;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist < half + radius;
    }


    private void drawAttackEffects() {
        for (EnemyAttackEffect effect : attackEffectSystem.getActiveEffects()) {
            float radius = 40f * (1f - effect.getProgress());
            float alpha = effect.getAlpha();

            batch.setColor(1f, 0.4f, 0.2f, alpha * 0.8f);
            // Draw expanding circle effect
            shapes.setProjectionMatrix(cameraController.getCamera().combined);
            shapes.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
            shapes.setColor(1f, 0.4f, 0.2f, alpha);
            shapes.circle(effect.getX(), effect.getY(), radius, 16);
            shapes.end();
        }
    }

    private void drawBloodEffects() {
        for (BloodEffect effect : bloodEffectSystem.getActiveEffects()) {
            Texture tex = effect.getTextureIndex() == 0 ? bloodTexture1 : bloodTexture2;
            if (tex == null) continue;
            float alpha = effect.getAlpha();
            float scale = 0.8f + effect.getProgress() * 0.2f;
            float size = 60f * scale;
            float x = effect.getX() - size * 0.5f;
            float y = effect.getY() - size * 0.5f;
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(tex, x, y, size, size);
        }
        batch.setColor(Color.WHITE);
    }

    private void renderEnemyShapes() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Enemy e : combatManager.getEnemies()) {
            if (!enemySprites.has(e.getType())) {
                Color body = colorFor(e.getType());
                if (e.isDead()) body = Color.DARK_GRAY;
                shapes.setColor(body);
                shapes.rect(e.getX() - ENEMY_SIZE * 0.5f, e.getY() - ENEMY_SIZE * 0.5f,
                        ENEMY_SIZE, ENEMY_SIZE);
            }
            // Only show health bar for boss
            if (!e.isDead() && e.getType() == EnemyType.MALTARION_ECHO) {
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
            case SHADOW_GOBLIN:  return Color.FOREST;
            case MOSS_CRAWLER:   return Color.OLIVE;
            case STONE_WATCHER:  return Color.GRAY;
            case BONE_ARCHER:    return Color.WHITE;
            case DROWNER:        return Color.CYAN;
            case RIFT_JELLYFISH: return Color.PURPLE;
            case RIFT_KNIGHT:    return Color.NAVY;
            case LAVA_SNAKE:     return Color.ORANGE;
            case SLAG_ELEMENTAL: return Color.FIREBRICK;
            case MALTARION_ECHO: return Color.GOLD;
            default:             return Color.RED;
        }
    }

    // ── physics ───────────────────────────────────────────────────────────────

    private void clampPlayerToWorld() {
        float halfW = SPRITE_SIZE * 0.5f;
        float x = MathUtils.clamp(player.getX(), halfW, ROOM_WIDTH  - halfW);
        float y = MathUtils.clamp(player.getY(), halfW, ROOM_HEIGHT - halfW);

        // Collision with chest
        if (collidesWithChest(x, y, halfW)) {
            float px = player.getX();
            float py = player.getY();
            float cx = ROOM_WIDTH / 2f;
            float cy = ROOM_HEIGHT / 2f;
            float dx = px - cx;
            float dy = py - cy;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 0) {
                float chestSize = 127.8f;
                float half = chestSize * 0.4f;
                float pushDist = half + halfW + 5f;
                x = cx + (dx / dist) * pushDist;
                y = cy + (dy / dist) * pushDist;
            }
        }

        x = MathUtils.clamp(x, halfW, ROOM_WIDTH  - halfW);
        y = MathUtils.clamp(y, halfW, ROOM_HEIGHT - halfW);
        player.getContext().setPosition(x, y);
    }

    private void clampEnemiesToWorld() {
        float half = ENEMY_SIZE * 0.5f;
        for (Enemy e : combatManager.getEnemies()) {
            float x = MathUtils.clamp(e.getX(), half, ROOM_WIDTH - half);
            float y = MathUtils.clamp(e.getY(), half, ROOM_HEIGHT - half);

            // Collision with other enemies
            for (Enemy other : combatManager.getEnemies()) {
                if (e == other) continue;
                float dx = x - other.getX();
                float dy = y - other.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float minDist = ENEMY_SIZE;
                if (dist < minDist && dist > 0) {
                    float overlap = minDist - dist;
                    x += (dx / dist) * overlap * 0.5f;
                    y += (dy / dist) * overlap * 0.5f;
                }
            }

            x = MathUtils.clamp(x, half, ROOM_WIDTH - half);
            y = MathUtils.clamp(y, half, ROOM_HEIGHT - half);
            if (x != e.getX() || y != e.getY()) e.getContext().setPosition(x, y);
        }
    }

    // ── input ─────────────────────────────────────────────────────────────────

    private InputAdapter buildHudAdapter() {
        final GameScreen self = this;
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
                if (keycode == Input.Keys.NUM_1) {
                    self.teleportToTier(Tier.FLOODED_CATACOMBS);
                    return true;
                }
                if (keycode == Input.Keys.NUM_2) {
                    self.teleportToTier(Tier.MALTARIONS_ABYSS);
                    return true;
                }
                if (keycode == Input.Keys.NUM_3) {
                    self.killFinalBoss();
                    return true;
                }
                if (keycode == Input.Keys.NUM_4) {
                    hudRenderer.activateChipSlot(3);
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

    // ── demo key events (K/R) ─────────────────────────────────────────────────

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

    // ── asset helpers ─────────────────────────────────────────────────────────

    private Texture loadTextureOrPlaceholder(String path, Color fallback) {
        Texture t;
        if (Gdx.files.internal(path).exists()) {
            try {
                t = new Texture(path);
            } catch (RuntimeException e) {
                Gdx.app.error("GameScreen", "Failed to load " + path, e);
                t = (fallback != null) ? solidTexture(fallback) : null;
            }
        } else {
            t = (fallback != null) ? solidTexture(fallback) : null;
        }
        if (t != null) t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
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

    private Texture generateChestClosed() {
        int size = 64;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0.6f, 0.4f, 0.2f, 1f); // Brown
        px.fill();
        px.setColor(0.4f, 0.25f, 0.1f, 1f); // Dark brown border
        px.drawRectangle(5, 5, 54, 54);
        px.drawRectangle(20, 15, 24, 20); // Lid line
        Texture t = new Texture(px);
        px.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    private Texture generateChestOpen() {
        int size = 64;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0.8f, 0.6f, 0.2f, 1f); // Gold/Light brown
        px.fill();
        px.setColor(1f, 0.8f, 0.2f, 1f); // Bright gold (inside)
        px.fillRectangle(10, 20, 44, 30);
        px.setColor(0.6f, 0.4f, 0.1f, 1f); // Dark brown outline
        px.drawRectangle(5, 5, 54, 54);
        Texture t = new Texture(px);
        px.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    private Texture generateFallbackBg() {
        int size = 64;
        Pixmap px = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        px.setColor(0.08f, 0.06f, 0.12f, 1f);
        px.fill();
        px.setColor(0.12f, 0.10f, 0.18f, 1f);
        for (int x = 0; x < size; x += 8)
            for (int y = 0; y < size; y += 8)
                px.fillRectangle(x, y, 4, 4);
        Texture t = new Texture(px);
        px.dispose();
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return t;
    }

    private void teleportToTier(Tier tier) {
        combatManager.clearEnemies();
        enemyOrientations.clear();
        enemyLastX.clear();
        DungeonManager.getInstance().loadTier(tier);
        enterCurrentRoom();
    }

    private void killFinalBoss() {
        Room room = DungeonManager.getInstance().getCurrentRoom();
        if (room == null || room.getType() != RoomType.FINAL) return;

        // Kill all enemies in final room
        for (Enemy e : combatManager.getEnemies()) {
            e.getContext().setCurrentHp(0);
        }
    }
}
