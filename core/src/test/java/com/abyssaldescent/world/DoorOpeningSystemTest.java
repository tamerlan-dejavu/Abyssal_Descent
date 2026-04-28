package com.abyssaldescent.world;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.KeyPickupEvent;
import com.abyssaldescent.event.DoorOpenedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoorOpeningSystemTest {
    private DoorOpeningSystem system;
    private KeyInventory keyInventory;
    private RoomManager roomManager;
    private EventBus eventBus;
    private Room room;
    private Door door;

    @BeforeEach
    void setUp() {
        keyInventory = new KeyInventory();
        roomManager = new RoomManager();
        eventBus = EventBus.getInstance();
        eventBus.clear();
        system = new DoorOpeningSystem(keyInventory, roomManager, eventBus);

        room = new Room("room1");
        door = new Door("door1", 1);
        room.addDoor(door);
        roomManager.addRoom(room);
        roomManager.setCurrentRoom("room1");
    }

    @Test
    void try_open_door_fails_without_key() {
        assertFalse(system.tryOpenDoor("door1"));
        assertFalse(door.isOpen());
    }

    @Test
    void try_open_door_succeeds_with_key() {
        keyInventory.addKey(1);
        assertTrue(system.tryOpenDoor("door1"));
        assertTrue(door.isOpen());
    }

    @Test
    void try_open_door_fails_when_no_current_room() {
        roomManager = new RoomManager();
        system = new DoorOpeningSystem(keyInventory, roomManager, eventBus);
        assertFalse(system.tryOpenDoor("door1"));
    }

    @Test
    void try_open_door_fails_for_nonexistent_door() {
        keyInventory.addKey(1);
        assertFalse(system.tryOpenDoor("nonexistent"));
    }

    @Test
    void try_open_already_open_door_returns_false() {
        door.open();
        keyInventory.addKey(1);
        assertFalse(system.tryOpenDoor("door1"));
    }

    @Test
    void key_pickup_event_adds_key() {
        KeyPickupEvent event = new KeyPickupEvent(1);
        eventBus.post(event);
        assertTrue(keyInventory.hasKey(1));
    }

    @Test
    void multiple_key_pickups() {
        eventBus.post(new KeyPickupEvent(1));
        eventBus.post(new KeyPickupEvent(2));
        assertTrue(keyInventory.hasKey(1));
        assertTrue(keyInventory.hasKey(2));
        assertEquals(2, keyInventory.getKeyCount());
    }
}
