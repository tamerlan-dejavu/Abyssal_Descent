package com.abyssaldescent.world.gen;

import com.abyssaldescent.world.Floor;
import com.abyssaldescent.world.tile.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Builder pattern implementation for procedural dungeon floor generation.
 *
 * <p>Each step adds a layer of content to the floor:
 * {@code generateRooms → connectCorridors → placeSpawn → placeExit → placeDecorations}.
 * The {@link FloorDirector} orchestrates the sequence, but callers can also
 * drive the builder manually for testing or custom layouts.
 *
 * <p><b>Pattern:</b> Builder (GoF creational).
 */
public final class DungeonBuilder {

    private static final int MIN_ROOM_SIZE = 4;
    private static final int MAX_ROOM_SIZE = 9;
    private static final int MAX_PLACEMENT_ATTEMPTS = 100;

    private final int width;
    private final int height;
    private final int floorNumber;
    private final Random random;

    private Floor floor;
    private final List<Room> rooms = new ArrayList<>();

    public DungeonBuilder(int width, int height, int floorNumber) {
        this(width, height, floorNumber, new Random());
    }

    public DungeonBuilder(int width, int height, int floorNumber, Random random) {
        this.width = width;
        this.height = height;
        this.floorNumber = floorNumber;
        this.random = random;
        this.floor = new Floor(width, height, floorNumber);
    }

    /** Step 1: carve out rooms. */
    public DungeonBuilder generateRooms(int targetRoomCount) {
        rooms.clear();
        for (int attempt = 0; attempt < MAX_PLACEMENT_ATTEMPTS && rooms.size() < targetRoomCount; attempt++) {
            int rw = MIN_ROOM_SIZE + random.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1);
            int rh = MIN_ROOM_SIZE + random.nextInt(MAX_ROOM_SIZE - MIN_ROOM_SIZE + 1);
            int rx = 1 + random.nextInt(Math.max(1, width - rw - 2));
            int ry = 1 + random.nextInt(Math.max(1, height - rh - 2));

            Room candidate = new Room(rx, ry, rw, rh);
            boolean overlaps = false;
            for (Room existing : rooms) {
                if (candidate.overlaps(existing)) {
                    overlaps = true;
                    break;
                }
            }
            if (!overlaps) {
                carveRoom(candidate);
                rooms.add(candidate);
            }
        }
        return this;
    }

    /** Step 2: connect rooms with corridors. */
    public DungeonBuilder connectCorridors() {
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room a = rooms.get(i);
            Room b = rooms.get(i + 1);
            carveCorridor(a.getCenterX(), a.getCenterY(), b.getCenterX(), b.getCenterY());
        }
        return this;
    }

    /** Step 3: place spawn point in the first room. */
    public DungeonBuilder placeSpawn() {
        if (rooms.isEmpty()) return this;
        Room first = rooms.get(0);
        int sx = first.getCenterX();
        int sy = first.getCenterY();
        floor.setTile(sx, sy, TileType.SPAWN);
        floor.setSpawn(sx, sy);
        return this;
    }

    /** Step 4: place exit in the last room. */
    public DungeonBuilder placeExit() {
        if (rooms.isEmpty()) return this;
        Room last = rooms.get(rooms.size() - 1);
        int ex = last.getCenterX();
        int ey = last.getCenterY();
        floor.setTile(ex, ey, TileType.EXIT);
        floor.setExit(ex, ey);
        return this;
    }

    /** Step 5: optional decorations (placeholder for future traps/items). */
    public DungeonBuilder placeDecorations() {
        // Will be expanded in Sprint 2 with traps, items, enemies
        return this;
    }

    /** Terminal operation: return the built floor. */
    public Floor build() {
        return floor;
    }

    /** Expose rooms for testing/inspection. */
    public List<Room> getRooms() {
        return List.copyOf(rooms);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private void carveRoom(Room room) {
        for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
            for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
                floor.setTile(x, y, TileType.FLOOR);
            }
        }
    }

    private void carveCorridor(int x1, int y1, int x2, int y2) {
        int x = x1;
        while (x != x2) {
            floor.setTile(x, y1, TileType.CORRIDOR);
            x += (x2 > x1) ? 1 : -1;
        }
        int y = y1;
        while (y != y2) {
            floor.setTile(x2, y, TileType.CORRIDOR);
            y += (y2 > y1) ? 1 : -1;
        }
        floor.setTile(x2, y2, TileType.CORRIDOR);
    }
}
