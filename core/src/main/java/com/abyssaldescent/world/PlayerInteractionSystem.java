package com.abyssaldescent.world;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerInteractionEvent;

public final class PlayerInteractionSystem {
    private final RoomManager roomManager;
    private final DoorOpeningSystem doorOpeningSystem;
    private final EventBus eventBus;

    public PlayerInteractionSystem(RoomManager roomManager, DoorOpeningSystem doorOpeningSystem, EventBus eventBus) {
        this.roomManager = roomManager;
        this.doorOpeningSystem = doorOpeningSystem;
        this.eventBus = eventBus;

        eventBus.subscribe(PlayerInteractionEvent.class, this::handleInteraction);
    }

    private void handleInteraction(PlayerInteractionEvent event) {
        Room currentRoom = roomManager.getCurrentRoom();
        if (currentRoom == null) {
            return;
        }

        for (Door door : currentRoom.getDoors()) {
            if (!door.isOpen()) {
                if (isPlayerNearDoor(event, door)) {
                    doorOpeningSystem.tryOpenDoor(door.getId());
                    return;
                }
            }
        }
    }

    private boolean isPlayerNearDoor(PlayerInteractionEvent event, Door door) {
        return true;
    }
}
