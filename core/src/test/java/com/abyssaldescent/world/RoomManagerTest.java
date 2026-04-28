package com.abyssaldescent.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomManagerTest {
    private RoomManager manager;
    private Room room1;
    private Room room2;
    private Door door;
    private Key key;

    @BeforeEach
    void setUp() {
        manager = new RoomManager();
        room1 = new Room("room1");
        room2 = new Room("room2");
        door = new Door("door1", 1);
        key = new Key(1, 0, 0);
    }

    @Test
    void manager_starts_with_no_rooms() {
        assertEquals(0, manager.getRoomCount());
    }

    @Test
    void add_room_increases_count() {
        manager.addRoom(room1);
        assertEquals(1, manager.getRoomCount());
    }

    @Test
    void add_multiple_rooms() {
        manager.addRoom(room1);
        manager.addRoom(room2);
        assertEquals(2, manager.getRoomCount());
    }

    @Test
    void set_current_room() {
        manager.addRoom(room1);
        manager.setCurrentRoom("room1");
        assertEquals(room1, manager.getCurrentRoom());
    }

    @Test
    void get_current_room_when_not_set() {
        assertNull(manager.getCurrentRoom());
    }

    @Test
    void get_room_by_id() {
        manager.addRoom(room1);
        assertEquals(room1, manager.getRoom("room1"));
    }

    @Test
    void get_nonexistent_room_returns_null() {
        assertNull(manager.getRoom("nonexistent"));
    }

    @Test
    void has_room() {
        manager.addRoom(room1);
        assertTrue(manager.hasRoom("room1"));
        assertFalse(manager.hasRoom("room2"));
    }

    @Test
    void null_room_not_added() {
        manager.addRoom(null);
        assertEquals(0, manager.getRoomCount());
    }

    @Test
    void open_door_with_key() {
        room1.addDoor(door);
        manager.addRoom(room1);
        manager.setCurrentRoom("room1");
        assertTrue(manager.openDoor("door1", key));
        assertTrue(door.isOpen());
    }

    @Test
    void open_door_fails_without_key() {
        room1.addDoor(door);
        manager.addRoom(room1);
        manager.setCurrentRoom("room1");
        Key wrongKey = new Key(2, 0, 0);
        assertFalse(manager.openDoor("door1", wrongKey));
        assertFalse(door.isOpen());
    }

    @Test
    void open_door_fails_when_no_current_room() {
        assertFalse(manager.openDoor("door1", key));
    }
}
