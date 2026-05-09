package com.abyssaldescent.world;

import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.enemy.EnemyContext;
import com.abyssaldescent.entity.enemy.EnemyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {
    private Room room;
    private Enemy enemy;
    private Door door;
    private Key key;

    @BeforeEach
    void setUp() {
        room = new Room("room1");
        enemy = new Enemy(new EnemyContext(EnemyType.SHADOW_GOBLIN, "enemy1", 0, 0));
        door = new Door("door1", 1);
        key = new Key(1, 5, 5);
    }

    @Test
    void room_starts_empty() {
        assertTrue(room.getEnemies().isEmpty());
        assertTrue(room.getDoors().isEmpty());
        assertTrue(room.getKeys().isEmpty());
    }

    @Test
    void add_enemy_increases_count() {
        room.addEnemy(enemy);
        assertEquals(1, room.getEnemies().size());
    }

    @Test
    void add_door_increases_count() {
        room.addDoor(door);
        assertEquals(1, room.getDoors().size());
    }

    @Test
    void add_key_increases_count() {
        room.addKey(key);
        assertEquals(1, room.getKeys().size());
    }

    @Test
    void remove_enemy_clears_room_when_empty() {
        room.addEnemy(enemy);
        room.removeEnemy(enemy);
        assertTrue(room.isCleared());
    }

    @Test
    void room_not_cleared_while_enemies_remain() {
        room.addEnemy(enemy);
        assertFalse(room.isCleared());
    }

    @Test
    void remove_key_removes_from_list() {
        room.addKey(key);
        room.removeKey(key);
        assertTrue(room.getKeys().isEmpty());
    }

    @Test
    void null_enemy_not_added() {
        room.addEnemy(null);
        assertTrue(room.getEnemies().isEmpty());
    }

    @Test
    void null_door_not_added() {
        room.addDoor(null);
        assertTrue(room.getDoors().isEmpty());
    }

    @Test
    void null_key_not_added() {
        room.addKey(null);
        assertTrue(room.getKeys().isEmpty());
    }

    @Test
    void get_room_id() {
        assertEquals("room1", room.getId());
    }
}
