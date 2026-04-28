package com.abyssaldescent.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KeyInventoryTest {
    private KeyInventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new KeyInventory();
    }

    @Test
    void inventory_starts_empty() {
        assertEquals(0, inventory.getKeyCount());
    }

    @Test
    void add_key_increases_count() {
        inventory.addKey(1);
        assertEquals(1, inventory.getKeyCount());
    }

    @Test
    void has_key_returns_true_after_add() {
        inventory.addKey(1);
        assertTrue(inventory.hasKey(1));
    }

    @Test
    void has_key_returns_false_for_nonexistent() {
        assertFalse(inventory.hasKey(1));
    }

    @Test
    void remove_key_decreases_count() {
        inventory.addKey(1);
        inventory.removeKey(1);
        assertEquals(0, inventory.getKeyCount());
    }

    @Test
    void remove_key_makes_has_key_false() {
        inventory.addKey(1);
        inventory.removeKey(1);
        assertFalse(inventory.hasKey(1));
    }

    @Test
    void add_multiple_keys() {
        inventory.addKey(1);
        inventory.addKey(2);
        inventory.addKey(3);
        assertEquals(3, inventory.getKeyCount());
        assertTrue(inventory.hasKey(1));
        assertTrue(inventory.hasKey(2));
        assertTrue(inventory.hasKey(3));
    }

    @Test
    void get_all_keys() {
        inventory.addKey(1);
        inventory.addKey(2);
        var keys = inventory.getAllKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains(1));
        assertTrue(keys.contains(2));
    }

    @Test
    void get_all_keys_returns_copy() {
        inventory.addKey(1);
        var keys1 = inventory.getAllKeys();
        var keys2 = inventory.getAllKeys();
        assertNotSame(keys1, keys2);
    }

    @Test
    void clear_empties_inventory() {
        inventory.addKey(1);
        inventory.addKey(2);
        inventory.clear();
        assertEquals(0, inventory.getKeyCount());
    }
}
