package com.abyssaldescent.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoorTest {
    private Door door;
    private Key matchingKey;
    private Key wrongKey;

    @BeforeEach
    void setUp() {
        door = new Door("door1", 1);
        matchingKey = new Key(1, 0, 0);
        wrongKey = new Key(2, 0, 0);
    }

    @Test
    void door_starts_closed() {
        assertFalse(door.isOpen());
    }

    @Test
    void open_sets_door_open() {
        door.open();
        assertTrue(door.isOpen());
    }

    @Test
    void can_open_with_matching_key() {
        assertTrue(door.canOpen(matchingKey));
    }

    @Test
    void cannot_open_with_wrong_key() {
        assertFalse(door.canOpen(wrongKey));
    }

    @Test
    void cannot_open_with_null_key() {
        assertFalse(door.canOpen(null));
    }

    @Test
    void get_door_id() {
        assertEquals("door1", door.getId());
    }

    @Test
    void get_required_key_id() {
        assertEquals(1, door.getRequiredKeyId());
    }
}
