package com.abyssaldescent.dungeon;

import java.util.Random;

public final class DungeonGenerator {

    private static final int MIN_ARENAS = 5;
    private static final int MAX_ARENAS = 7;
    private static final int BRANCHES   = 3;

    private final Random rng;
    private int idCounter;

    public DungeonGenerator(long seed) {
        this.rng = new Random(seed);
    }

    public DungeonGenerator() {
        this(System.currentTimeMillis());
    }

    public DungeonGraph generate(Tier tier) {
        idCounter = 0;
        DungeonGraph graph = new DungeonGraph();

        Room start = newRoom(RoomType.STARTING, tier);
        graph.addRoom(start);
        graph.setStartRoomId(start.getId());

        Direction[] branchDirs = { Direction.NORTH, Direction.EAST, Direction.WEST };

        for (int b = 0; b < BRANCHES; b++) {
            int arenaCount = MIN_ARENAS + rng.nextInt(MAX_ARENAS - MIN_ARENAS + 1);
            int saveIndex  = arenaCount / 2;
            Direction dir  = branchDirs[b];

            Room prev = start;
            for (int i = 0; i < arenaCount; i++) {
                RoomType type = (i == saveIndex) ? RoomType.SAVE_ROOM : RoomType.BATTLE_ARENA;
                Room room = newRoom(type, tier);
                graph.addRoom(room);
                graph.connect(prev, room, (prev == start) ? dir : Direction.SOUTH);
                prev = room;
            }

            Room finalRoom = newRoom(RoomType.FINAL, tier);
            graph.addRoom(finalRoom);
            graph.connect(prev, finalRoom, Direction.SOUTH);
        }

        return graph;
    }

    private Room newRoom(RoomType type, Tier tier) {
        String id = tier.name() + "_" + idCounter++;
        return new Room(id, type, tier);
    }
}
