package com.abyssaldescent.world;

import com.abyssaldescent.entity.enemy.Enemy;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public final class Room {
    private final String id;
    private final RoomType type;
    private final RoomSize size;
    private final Tier tier;
    private final Vector2 spawnPoint = new Vector2();
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Door> doors = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();
    private boolean cleared;
    private boolean visited;

    public Room(String id) {
        this(id, RoomType.COMBAT, RoomSize.MEDIUM, Tier.SURFACE_CAVERNS);
    }

    public Room(String id, RoomType type, RoomSize size, Tier tier) {
        this.id = id;
        this.type = type;
        this.size = size;
        this.tier = tier;
        this.cleared = !type.spawnsEnemies();
        this.visited = false;
        this.spawnPoint.set(size.getWidth() * 0.5f, 1.5f);
    }

    public void addEnemy(Enemy enemy) {
        if (enemy != null) {
            enemies.add(enemy);
        }
    }

    public void addDoor(Door door) {
        if (door != null) {
            doors.add(door);
        }
    }

    public void addKey(Key key) {
        if (key != null) {
            keys.add(key);
        }
    }

    public void removeKey(Key key) {
        keys.remove(key);
    }

    public void removeEnemy(Enemy enemy) {
        enemies.remove(enemy);
        checkCleared();
    }

    private void checkCleared() {
        if (type.spawnsEnemies()) {
            cleared = enemies.isEmpty();
        }
    }

    public Door getDoor(String doorId) {
        for (Door door : doors) {
            if (door.getId().equals(doorId)) {
                return door;
            }
        }
        return null;
    }

    public Door getDoor(Direction direction) {
        for (Door door : doors) {
            if (door.getDirection() == direction) {
                return door;
            }
        }
        return null;
    }

    public void setSpawnPoint(float x, float y) {
        spawnPoint.set(x, y);
    }

    public String getId() { return id; }
    public RoomType getType() { return type; }
    public RoomSize getSize() { return size; }
    public Tier getTier() { return tier; }
    public Vector2 getSpawnPoint() { return spawnPoint; }
    public int getWidth() { return size.getWidth(); }
    public int getHeight() { return size.getHeight(); }

    public List<Enemy> getEnemies() {
        return new ArrayList<>(enemies);
    }

    public List<Door> getDoors() {
        return new ArrayList<>(doors);
    }

    public List<Key> getKeys() {
        return new ArrayList<>(keys);
    }

    public boolean isCleared() { return cleared; }
    public void setCleared(boolean cleared) { this.cleared = cleared; }
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
}
