package com.abyssaldescent.world;

import com.abyssaldescent.entity.enemy.Enemy;
import java.util.ArrayList;
import java.util.List;

public final class Room {
    private final String id;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Door> doors = new ArrayList<>();
    private final List<Key> keys = new ArrayList<>();
    private boolean cleared;

    public Room(String id) {
        this.id = id;
        this.cleared = false;
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
        cleared = enemies.isEmpty();
    }

    public String getId() {
        return id;
    }

    public List<Enemy> getEnemies() {
        return new ArrayList<>(enemies);
    }

    public List<Door> getDoors() {
        return new ArrayList<>(doors);
    }

    public List<Key> getKeys() {
        return new ArrayList<>(keys);
    }

    public boolean isCleared() {
        return cleared;
    }

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }
}
