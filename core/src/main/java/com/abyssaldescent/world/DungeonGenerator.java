package com.abyssaldescent.world;

import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyFactory;
import com.abyssaldescent.entity.enemy.EnemyType;

import java.util.List;
import java.util.Random;

public final class DungeonGenerator {

    private final EnemyFactory enemyFactory;
    private final Random random;

    public DungeonGenerator(EnemyFactory enemyFactory, long seed) {
        this.enemyFactory = enemyFactory;
        this.random = new Random(seed);
    }

    public DungeonGenerator(EnemyFactory enemyFactory) {
        this(enemyFactory, 1337L);
    }

    public Dungeon generate() {
        Dungeon dungeon = new Dungeon();
        buildTier1(dungeon);
        buildTier2(dungeon);
        buildTier3(dungeon);
        connectTiers(dungeon);
        dungeon.setStartRoomId("t1_start");
        return dungeon;
    }

    // ─── Tier 1: Surface Caverns ─────────────────────────────────────────────
    // Layout (each cell = room, arrows = doors):
    //
    //  [treasure1] ← N
    //      ↓ S
    //  [start] →E→ [combat1] →E→ [combat2] →E→ [rest1]
    //                  ↓ S           ↓ S           ↓ S
    //              [combat3]     [treasure2]    [combat4] →E→ [boss]
    //                  ↓ S                         ↑ N (key locked)
    //              [secret1]                    [rest2]
    //
    private void buildTier1(Dungeon dungeon) {
        Tier tier = Tier.SURFACE_CAVERNS;
        int keyId = DungeonBlueprint.keyIdFor(tier);

        Room start     = createRoom("t1_start",     RoomType.START,    RoomSize.SMALL,  tier);
        Room treasure1 = createRoom("t1_treasure1", RoomType.TREASURE, RoomSize.SMALL,  tier);
        Room combat1   = createRoom("t1_combat1",   RoomType.COMBAT,   RoomSize.MEDIUM, tier);
        Room combat2   = createRoom("t1_combat2",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room rest1     = createRoom("t1_rest1",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room combat3   = createRoom("t1_combat3",   RoomType.COMBAT,   RoomSize.MEDIUM, tier);
        Room treasure2 = createRoom("t1_treasure2", RoomType.TREASURE, RoomSize.SMALL,  tier);
        Room combat4   = createRoom("t1_combat4",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room secret1   = createRoom("t1_secret1",   RoomType.CORRIDOR, RoomSize.SMALL,  tier);
        Room rest2     = createRoom("t1_rest2",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room boss      = createRoom("t1_boss",      RoomType.BOSS,     RoomSize.LARGE,  tier);

        // Key spawns: treasure1 holds the boss key
        treasure1.addKey(new Key(keyId, treasure1.getWidth() * 0.5f, 2.0f));
        // A free key in treasure2 for early door
        treasure2.addKey(new Key(0, treasure2.getWidth() * 0.5f, 2.0f));

        // Horizontal main spine
        connect(start,   combat1,   Direction.EAST,  0);
        connect(combat1, combat2,   Direction.EAST,  0);
        connect(combat2, rest1,     Direction.EAST,  0);

        // Vertical branches down from spine
        connect(combat1, combat3,   Direction.SOUTH, 0);
        connect(combat2, treasure2, Direction.SOUTH, 0);
        connect(rest1,   combat4,   Direction.SOUTH, 0);

        // Start has a north branch with treasure
        connect(start,   treasure1, Direction.NORTH, 0);

        // combat3 goes deeper south to secret
        connect(combat3, secret1,   Direction.SOUTH, 0);

        // rest2 bridges up to combat4
        connect(combat4, rest2,     Direction.SOUTH, 0);

        // Boss locked with key
        connect(combat4, boss,      Direction.EAST,  keyId);

        spawnEnemies(combat1,  tier);
        spawnEnemies(combat2,  tier);
        spawnEnemies(combat3,  tier);
        spawnEnemies(combat4,  tier);
        spawnBoss(boss, tier);

        dungeon.addRoom(start);
        dungeon.addRoom(treasure1);
        dungeon.addRoom(combat1);
        dungeon.addRoom(combat2);
        dungeon.addRoom(rest1);
        dungeon.addRoom(combat3);
        dungeon.addRoom(treasure2);
        dungeon.addRoom(combat4);
        dungeon.addRoom(secret1);
        dungeon.addRoom(rest2);
        dungeon.addRoom(boss);
        dungeon.setBossRoomId(tier, boss.getId());
    }

    // ─── Tier 2: Drowned Depths ───────────────────────────────────────────────
    // Wider, longer layout with 12 rooms
    //
    //  [entrance] →E→ [combat1] →E→ [puzzle1] →E→ [combat2] →E→ [rest1]
    //                    ↓ S           ↓ S            ↓ S          ↓ S
    //                [combat3]      [shop1]       [treasure1]  [combat4] →E→ [combat5] →E→ [boss]
    //                                                              ↑ N key locked ────────────┘
    //                                              [rest2] ←S← [combat5]
    //
    private void buildTier2(Dungeon dungeon) {
        Tier tier = Tier.DROWNED_DEPTHS;
        int keyId = DungeonBlueprint.keyIdFor(tier);

        Room entrance  = createRoom("t2_entrance",  RoomType.START,    RoomSize.MEDIUM, tier);
        Room combat1   = createRoom("t2_combat1",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room puzzle1   = createRoom("t2_puzzle1",   RoomType.PUZZLE,   RoomSize.MEDIUM, tier);
        Room combat2   = createRoom("t2_combat2",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room rest1     = createRoom("t2_rest1",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room combat3   = createRoom("t2_combat3",   RoomType.COMBAT,   RoomSize.MEDIUM, tier);
        Room shop1     = createRoom("t2_shop1",     RoomType.SHOP,     RoomSize.SMALL,  tier);
        Room treasure1 = createRoom("t2_treasure1", RoomType.TREASURE, RoomSize.SMALL,  tier);
        Room combat4   = createRoom("t2_combat4",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room combat5   = createRoom("t2_combat5",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room rest2     = createRoom("t2_rest2",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room boss      = createRoom("t2_boss",      RoomType.BOSS,     RoomSize.HUGE,   tier);

        treasure1.addKey(new Key(keyId, treasure1.getWidth() * 0.5f, 2.0f));

        // Main horizontal spine
        connect(entrance, combat1,   Direction.EAST,  0);
        connect(combat1,  puzzle1,   Direction.EAST,  0);
        connect(puzzle1,  combat2,   Direction.EAST,  0);
        connect(combat2,  rest1,     Direction.EAST,  0);

        // South branches
        connect(combat1,  combat3,   Direction.SOUTH, 0);
        connect(puzzle1,  shop1,     Direction.SOUTH, 0);
        connect(combat2,  treasure1, Direction.SOUTH, 0);
        connect(rest1,    combat4,   Direction.SOUTH, 0);

        // Second spine layer
        connect(combat4,  combat5,   Direction.EAST,  0);
        connect(combat5,  rest2,     Direction.SOUTH, 0);

        // Boss requires key
        connect(combat5,  boss,      Direction.EAST,  keyId);

        spawnEnemies(combat1,  tier);
        spawnEnemies(combat2,  tier);
        spawnEnemies(combat3,  tier);
        spawnEnemies(combat4,  tier);
        spawnEnemies(combat5,  tier);
        spawnBoss(boss, tier);

        dungeon.addRoom(entrance);
        dungeon.addRoom(combat1);
        dungeon.addRoom(puzzle1);
        dungeon.addRoom(combat2);
        dungeon.addRoom(rest1);
        dungeon.addRoom(combat3);
        dungeon.addRoom(shop1);
        dungeon.addRoom(treasure1);
        dungeon.addRoom(combat4);
        dungeon.addRoom(combat5);
        dungeon.addRoom(rest2);
        dungeon.addRoom(boss);
        dungeon.setBossRoomId(tier, boss.getId());
    }

    // ─── Tier 3: Lava Sanctum ────────────────────────────────────────────────
    // Largest tier — 14 rooms, complex grid
    //
    //  [entrance] →E→ [combat1] →E→ [combat2] →E→ [rest1]  →E→ [combat3]
    //                    ↓ S           ↓ S                         ↓ S
    //                [combat4]      [shop1]                     [combat5] →E→ [boss (HUGE)]
    //                    ↓ S           ↓ S                         ↑ N (key locked)
    //                [rest2]       [treasure1]                  [combat6]
    //                                 ↓ S
    //                              [secret2]
    //
    private void buildTier3(Dungeon dungeon) {
        Tier tier = Tier.LAVA_SANCTUM;
        int keyId = DungeonBlueprint.keyIdFor(tier);

        Room entrance  = createRoom("t3_entrance",  RoomType.START,    RoomSize.MEDIUM, tier);
        Room combat1   = createRoom("t3_combat1",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room combat2   = createRoom("t3_combat2",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room rest1     = createRoom("t3_rest1",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room combat3   = createRoom("t3_combat3",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room combat4   = createRoom("t3_combat4",   RoomType.COMBAT,   RoomSize.MEDIUM, tier);
        Room shop1     = createRoom("t3_shop1",     RoomType.SHOP,     RoomSize.SMALL,  tier);
        Room combat5   = createRoom("t3_combat5",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room combat6   = createRoom("t3_combat6",   RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room rest2     = createRoom("t3_rest2",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room treasure1 = createRoom("t3_treasure1", RoomType.TREASURE, RoomSize.MEDIUM, tier);
        Room secret2   = createRoom("t3_secret2",   RoomType.CORRIDOR, RoomSize.SMALL,  tier);
        Room boss      = createRoom("t3_boss",      RoomType.BOSS,     RoomSize.HUGE,   tier);

        treasure1.addKey(new Key(keyId, treasure1.getWidth() * 0.5f, 2.0f));

        // Top horizontal spine
        connect(entrance, combat1,   Direction.EAST,  0);
        connect(combat1,  combat2,   Direction.EAST,  0);
        connect(combat2,  rest1,     Direction.EAST,  0);
        connect(rest1,    combat3,   Direction.EAST,  0);

        // South branches off spine
        connect(combat1,  combat4,   Direction.SOUTH, 0);
        connect(combat2,  shop1,     Direction.SOUTH, 0);
        connect(combat3,  combat5,   Direction.SOUTH, 0);

        // combat4 goes deeper south
        connect(combat4,  rest2,     Direction.SOUTH, 0);

        // shop1 leads to treasure
        connect(shop1,    treasure1, Direction.SOUTH, 0);
        connect(treasure1, secret2,  Direction.SOUTH, 0);

        // combat5 has a deeper south path and the boss to the east (key locked)
        connect(combat5,  combat6,   Direction.SOUTH, 0);
        connect(combat5,  boss,      Direction.EAST,  keyId);

        spawnEnemies(combat1,  tier);
        spawnEnemies(combat2,  tier);
        spawnEnemies(combat3,  tier);
        spawnEnemies(combat4,  tier);
        spawnEnemies(combat5,  tier);
        spawnEnemies(combat6,  tier);
        spawnBoss(boss, tier);

        dungeon.addRoom(entrance);
        dungeon.addRoom(combat1);
        dungeon.addRoom(combat2);
        dungeon.addRoom(rest1);
        dungeon.addRoom(combat3);
        dungeon.addRoom(combat4);
        dungeon.addRoom(shop1);
        dungeon.addRoom(combat5);
        dungeon.addRoom(combat6);
        dungeon.addRoom(rest2);
        dungeon.addRoom(treasure1);
        dungeon.addRoom(secret2);
        dungeon.addRoom(boss);
        dungeon.setBossRoomId(tier, boss.getId());
        dungeon.setFinalBossRoomId(boss.getId());
    }

    // ─── Tier connections: boss room descends SOUTH into next tier entrance ──
    private void connectTiers(Dungeon dungeon) {
        Room t1Boss    = dungeon.getRoom("t1_boss");
        Room t2Entrance = dungeon.getRoom("t2_entrance");
        Room t2Boss    = dungeon.getRoom("t2_boss");
        Room t3Entrance = dungeon.getRoom("t3_entrance");

        // Descending: tier boss SOUTH → next tier entrance NORTH
        addConnection(t1Boss,    "tier_descent_1",      t2Entrance.getId(), Direction.SOUTH, 0);
        addConnection(t2Entrance,"tier_descent_1_back", t1Boss.getId(),     Direction.NORTH, 0);

        addConnection(t2Boss,    "tier_descent_2",      t3Entrance.getId(), Direction.SOUTH, 0);
        addConnection(t3Entrance,"tier_descent_2_back", t2Boss.getId(),     Direction.NORTH, 0);
    }

    private Room createRoom(String id, RoomType type, RoomSize size, Tier tier) {
        Room r = new Room(id, type, size, tier);
        // Spawn player in left-centre of each room so they walk right into it
        r.setSpawnPoint(1.5f, size.getHeight() * 0.5f);
        return r;
    }

    private void connect(Room a, Room b, Direction dirFromA, int keyId) {
        String doorAtoB = a.getId() + "_to_" + b.getId();
        String doorBtoA = b.getId() + "_to_" + a.getId();
        addConnection(a, doorAtoB, b.getId(), dirFromA, keyId);
        addConnection(b, doorBtoA, a.getId(), dirFromA.opposite(), keyId);
    }

    private void addConnection(Room room, String doorId, String targetId, Direction dir, int keyId) {
        boolean locked = keyId > 0;
        room.addDoor(new Door(doorId, keyId, dir, targetId, locked));
    }

    private void spawnEnemies(Room room, Tier tier) {
        int count = DungeonBlueprint.enemyCountFor(room.getType(), room.getSize());
        List<EnemyType> pool = DungeonBlueprint.tierEnemies(tier);
        float roomW = room.getWidth();
        float roomH = room.getHeight();
        for (int i = 0; i < count; i++) {
            EnemyType type = pool.get(random.nextInt(pool.size()));
            // Spread enemies across the room width, keep them on the floor
            float x = 4.0f + random.nextFloat() * (roomW - 6.0f);
            float y = 1.5f + random.nextFloat() * (roomH - 3.0f);
            Enemy enemy = enemyFactory.create(type, x, y);
            room.addEnemy(enemy);
        }
    }

    private void spawnBoss(Room room, Tier tier) {
        EnemyType bossType = DungeonBlueprint.tierBoss(tier);
        float x = room.getWidth() * 0.65f;
        float y = room.getHeight() * 0.5f;
        Enemy enemy = enemyFactory.create(bossType, x, y);
        room.addEnemy(enemy);
    }
}
