package com.abyssaldescent.dungeon;

import java.util.Random;

/**
 * Generates a dungeon graph for one tier.
 *
 * Layout (grid coords, Y increases downward):
 *   Start room at (0, 0).
 *   Branch SOUTH: rooms at (0,-1), (0,-2) … (0,-N), final at (0,-N-1)
 *   Branch EAST:  rooms at (1,0), (2,0) … (N,0), save room at (N+1,0)
 *   Branch WEST:  rooms at (-1,0), (-2,0) … (-N,0), save room at (-N-1,0)
 *
 * SOUTH branch ends with FINAL room (boss). EAST/WEST end with SAVE rooms.
 * 4 keys total: 1 in SOUTH branch, 1 in EAST branch, 1 in WEST branch, 1 in starting room
 */
public final class DungeonGenerator {

    private static final int MIN_ARENAS = 4;
    private static final int MAX_ARENAS = 6;

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
        start.setGridPos(0, 0);
        graph.addRoom(start);
        graph.setStartRoomId(start.getId());

        buildBranch(graph, tier, start, 0,  -1, Direction.SOUTH);  // down - contains final room
        buildBranch(graph, tier, start, 1,  0,  Direction.EAST);   // right
        buildBranch(graph, tier, start, -1, 0,  Direction.WEST);   // left

        return graph;
    }

    /** Builds one linear branch from startRoom. SOUTH branch ends with final room, others end with save room. Save room placed in middle of all branches. */
    private void buildBranch(DungeonGraph graph, Tier tier,
                             Room startRoom, int dx, int dy, Direction dir) {
        int arenaCount = MIN_ARENAS + rng.nextInt(MAX_ARENAS - MIN_ARENAS + 1);
        int saveIndex  = arenaCount / 2;

        Room prev = startRoom;
        int gx = startRoom.getGridX();
        int gy = startRoom.getGridY();

        boolean isSouthBranch = (dir == Direction.SOUTH);

        // Generate battle arenas and save room in middle
        for (int i = 0; i < arenaCount; i++) {
            gx += dx;
            gy += dy;
            RoomType type = (i == saveIndex) ? RoomType.SAVE_ROOM : RoomType.BATTLE_ARENA;
            Room room = newRoom(type, tier);
            room.setGridPos(gx, gy);
            graph.addRoom(room);
            graph.connect(prev, room, dir);
            prev = room;
        }

        // Add final room to SOUTH branch, save room to EAST/WEST
        gx += dx;
        gy += dy;
        Room endRoom = newRoom(isSouthBranch ? RoomType.FINAL : RoomType.SAVE_ROOM, tier);
        endRoom.setGridPos(gx, gy);
        graph.addRoom(endRoom);
        graph.connect(prev, endRoom, dir);
    }

    private Room newRoom(RoomType type, Tier tier) {
        String id = tier.name() + "_" + idCounter++;
        return new Room(id, type, tier);
    }
}
