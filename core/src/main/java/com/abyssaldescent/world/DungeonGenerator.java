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

    private void buildTier1(Dungeon dungeon) {
        Tier tier = Tier.SURFACE_CAVERNS;
        int keyId = DungeonBlueprint.keyIdFor(tier);

        Room start    = createRoom("t1_start",    RoomType.START,    RoomSize.SMALL,  tier);
        Room combat1  = createRoom("t1_combat1",  RoomType.COMBAT,   RoomSize.MEDIUM, tier);
        Room treasure = createRoom("t1_treasure", RoomType.TREASURE, RoomSize.SMALL,  tier);
        Room combat2  = createRoom("t1_combat2",  RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room rest     = createRoom("t1_rest",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room boss     = createRoom("t1_boss",     RoomType.BOSS,     RoomSize.LARGE,  tier);

        treasure.addKey(new Key(keyId, treasure.getWidth() * 0.5f, 2.0f));

        connect(start, combat1, Direction.EAST, 0);
        connect(combat1, treasure, Direction.NORTH, 0);
        connect(combat1, combat2, Direction.EAST, 0);
        connect(combat2, rest, Direction.NORTH, 0);
        connect(combat2, boss, Direction.EAST, keyId);

        spawnEnemies(combat1, tier);
        spawnEnemies(combat2, tier);
        spawnBoss(boss, tier);

        dungeon.addRoom(start);
        dungeon.addRoom(combat1);
        dungeon.addRoom(treasure);
        dungeon.addRoom(combat2);
        dungeon.addRoom(rest);
        dungeon.addRoom(boss);
        dungeon.setBossRoomId(tier, boss.getId());
    }

    private void buildTier2(Dungeon dungeon) {
        Tier tier = Tier.DROWNED_DEPTHS;
        int keyId = DungeonBlueprint.keyIdFor(tier);

        Room entrance = createRoom("t2_entrance", RoomType.START,    RoomSize.MEDIUM, tier);
        Room combat1  = createRoom("t2_combat1",  RoomType.COMBAT,   RoomSize.MEDIUM, tier);
        Room puzzle   = createRoom("t2_puzzle",   RoomType.PUZZLE,   RoomSize.MEDIUM, tier);
        Room combat2  = createRoom("t2_combat2",  RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room treasure = createRoom("t2_treasure", RoomType.TREASURE, RoomSize.SMALL,  tier);
        Room shop     = createRoom("t2_shop",     RoomType.SHOP,     RoomSize.SMALL,  tier);
        Room boss     = createRoom("t2_boss",     RoomType.BOSS,     RoomSize.LARGE,  tier);

        treasure.addKey(new Key(keyId, treasure.getWidth() * 0.5f, 2.0f));

        connect(entrance, combat1, Direction.EAST, 0);
        connect(combat1, puzzle, Direction.NORTH, 0);
        connect(combat1, combat2, Direction.EAST, 0);
        connect(puzzle, treasure, Direction.EAST, 0);
        connect(combat2, shop, Direction.NORTH, 0);
        connect(combat2, boss, Direction.EAST, keyId);

        spawnEnemies(combat1, tier);
        spawnEnemies(combat2, tier);
        spawnBoss(boss, tier);

        dungeon.addRoom(entrance);
        dungeon.addRoom(combat1);
        dungeon.addRoom(puzzle);
        dungeon.addRoom(combat2);
        dungeon.addRoom(treasure);
        dungeon.addRoom(shop);
        dungeon.addRoom(boss);
        dungeon.setBossRoomId(tier, boss.getId());
    }

    private void buildTier3(Dungeon dungeon) {
        Tier tier = Tier.LAVA_SANCTUM;
        int keyId = DungeonBlueprint.keyIdFor(tier);

        Room entrance = createRoom("t3_entrance", RoomType.START,    RoomSize.MEDIUM, tier);
        Room combat1  = createRoom("t3_combat1",  RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room rest     = createRoom("t3_rest",     RoomType.REST,     RoomSize.SMALL,  tier);
        Room combat2  = createRoom("t3_combat2",  RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room treasure = createRoom("t3_treasure", RoomType.TREASURE, RoomSize.MEDIUM, tier);
        Room combat3  = createRoom("t3_combat3",  RoomType.COMBAT,   RoomSize.LARGE,  tier);
        Room shop     = createRoom("t3_shop",     RoomType.SHOP,     RoomSize.SMALL,  tier);
        Room boss     = createRoom("t3_boss",     RoomType.BOSS,     RoomSize.HUGE,   tier);

        treasure.addKey(new Key(keyId, treasure.getWidth() * 0.5f, 2.0f));

        connect(entrance, combat1, Direction.EAST, 0);
        connect(combat1, rest, Direction.NORTH, 0);
        connect(combat1, combat2, Direction.EAST, 0);
        connect(combat2, treasure, Direction.NORTH, 0);
        connect(combat2, combat3, Direction.EAST, 0);
        connect(combat3, shop, Direction.NORTH, 0);
        connect(combat3, boss, Direction.EAST, keyId);

        spawnEnemies(combat1, tier);
        spawnEnemies(combat2, tier);
        spawnEnemies(combat3, tier);
        spawnBoss(boss, tier);

        dungeon.addRoom(entrance);
        dungeon.addRoom(combat1);
        dungeon.addRoom(rest);
        dungeon.addRoom(combat2);
        dungeon.addRoom(treasure);
        dungeon.addRoom(combat3);
        dungeon.addRoom(shop);
        dungeon.addRoom(boss);
        dungeon.setBossRoomId(tier, boss.getId());
        dungeon.setFinalBossRoomId(boss.getId());
    }

    private void connectTiers(Dungeon dungeon) {
        Room t1Boss = dungeon.getRoom("t1_boss");
        Room t2Entrance = dungeon.getRoom("t2_entrance");
        Room t2Boss = dungeon.getRoom("t2_boss");
        Room t3Entrance = dungeon.getRoom("t3_entrance");

        addConnection(t1Boss, "tier_descent_1", t2Entrance.getId(), Direction.NORTH, 0);
        addConnection(t2Entrance, "tier_descent_1_back", t1Boss.getId(), Direction.SOUTH, 0);

        addConnection(t2Boss, "tier_descent_2", t3Entrance.getId(), Direction.NORTH, 0);
        addConnection(t3Entrance, "tier_descent_2_back", t2Boss.getId(), Direction.SOUTH, 0);
    }

    private Room createRoom(String id, RoomType type, RoomSize size, Tier tier) {
        return new Room(id, type, size, tier);
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
        for (int i = 0; i < count; i++) {
            EnemyType type = pool.get(random.nextInt(pool.size()));
            float x = 2.0f + random.nextFloat() * (room.getWidth() - 4.0f);
            float y = 1.5f;
            Enemy enemy = enemyFactory.create(type, x, y);
            room.addEnemy(enemy);
        }
    }

    private void spawnBoss(Room room, Tier tier) {
        EnemyType bossType = DungeonBlueprint.tierBoss(tier);
        float x = room.getWidth() * 0.7f;
        float y = 1.5f;
        Enemy enemy = enemyFactory.create(bossType, x, y);
        room.addEnemy(enemy);
    }
}
