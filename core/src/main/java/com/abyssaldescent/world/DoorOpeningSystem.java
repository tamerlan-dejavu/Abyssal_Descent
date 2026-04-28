package com.abyssaldescent.world;

import com.abyssaldescent.event.DoorOpenedEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.KeyPickupEvent;

public final class DoorOpeningSystem {
    private final KeyInventory keyInventory;
    private final RoomManager roomManager;
    private final EventBus eventBus;

    public DoorOpeningSystem(KeyInventory keyInventory, RoomManager roomManager, EventBus eventBus) {
        this.keyInventory = keyInventory;
        this.roomManager = roomManager;
        this.eventBus = eventBus;

        eventBus.subscribe(KeyPickupEvent.class, this::handleKeyPickup);
    }

    public boolean tryOpenDoor(String doorId) {
        Room currentRoom = roomManager.getCurrentRoom();
        if (currentRoom == null) {
            return false;
        }

        Door door = findDoorById(currentRoom, doorId);
        if (door == null || door.isOpen()) {
            return false;
        }

        if (keyInventory.hasKey(door.getRequiredKeyId())) {
            door.open();
            eventBus.post(new DoorOpenedEvent(doorId, door.getRequiredKeyId()));
            return true;
        }

        return false;
    }

    private Door findDoorById(Room room, String doorId) {
        for (Door door : room.getDoors()) {
            if (door.getId().equals(doorId)) {
                return door;
            }
        }
        return null;
    }

    private void handleKeyPickup(KeyPickupEvent event) {
        keyInventory.addKey(event.getKeyId());
    }
}
