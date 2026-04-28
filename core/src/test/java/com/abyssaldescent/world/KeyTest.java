package com.abyssaldescent.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyTest {
    private Key key;

    @BeforeEach
    void setUp() {
        key = new Key(1, 5.0f, 10.0f);
    }

    @Test
    void key_starts_not_collected() {
        assertFalse(key.isCollected());
    }

    @Test
    void collect_marks_key_collected() {
        key.collect();
        assertTrue(key.isCollected());
    }

    @Test
    void get_key_id() {
        assertEquals(1, key.getId());
    }

    @Test
    void get_key_position() {
        assertEquals(5.0f, key.getPosition().x);
        assertEquals(10.0f, key.getPosition().y);
    }

    @Test
    void position_is_mutable() {
        key.getPosition().x = 20.0f;
        assertEquals(20.0f, key.getPosition().x);
    }
}
