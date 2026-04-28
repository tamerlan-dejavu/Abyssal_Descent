package com.abyssaldescent.world;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerInteractionEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerInteractionSystemTest {
    private PlayerInteractionSystem system;
    private RoomManager roomManager;
    private DoorOpeningSystem doorOpeningSystem;
    private EventBus eventBus;
    private Room room;
    private Door door;
    private KeyInventory keyInventory;

    @BeforeEach
    void setUp() {
        roomManager = new RoomManager();
        keyInventory = new KeyInventory();
        eventBus = EventBus.getInstance();
        eventBus.clear();
        doorOpeningSystem = new DoorOpeningSystem(keyInventory, roomManager, eventBus);
        system = new PlayerInteractionSystem(roomManager, doorOpeningSystem, eventBus);

        room = new Room("room1");
        door = new Door("door1", 1);
        room.addDoor(door);
        roomManager.addRoom(room);
        roomManager.setCurrentRoom("room1");
    }

    @Test
    void interaction_opens_door_with_key() {
        keyInventory.addKey(1);
        PlayerInteractionEvent event = new PlayerInteractionEvent(5.0f, 5.0f, 2.0f);
        eventBus.post(event);
        assertTrue(door.isOpen());
    }

    @Test
    void interaction_fails_without_key() {
        PlayerInteractionEvent event = new PlayerInteractionEvent(5.0f, 5.0f, 2.0f);
        eventBus.post(event);
        assertFalse(door.isOpen());
    }

    @Test
    void interaction_does_nothing_without_current_room() {
        roomManager = new RoomManager();
        system = new PlayerInteractionSystem(roomManager, doorOpeningSystem, eventBus);
        keyInventory.addKey(1);
        PlayerInteractionEvent event = new PlayerInteractionEvent(5.0f, 5.0f, 2.0f);
        eventBus.post(event);
        assertFalse(door.isOpen());
    }

    @Test
    void interaction_stops_after_opening_first_door() {
        keyInventory.addKey(1);
        keyInventory.addKey(2);
        Door door2 = new Door("door2", 2);
        room.addDoor(door2);

        PlayerInteractionEvent event = new PlayerInteractionEvent(5.0f, 5.0f, 2.0f);
        eventBus.post(event);

        assertTrue(door.isOpen() || door2.isOpen());
        assertFalse(door.isOpen() && door2.isOpen());
    }

    @Test
    void interaction_ignores_already_open_doors() {
        door.open();
        keyInventory.addKey(1);
        Door door2 = new Door("door2", 1);
        room.addDoor(door2);

        PlayerInteractionEvent event = new PlayerInteractionEvent(5.0f, 5.0f, 2.0f);
        eventBus.post(event);

        assertTrue(door2.isOpen());
    }
}
