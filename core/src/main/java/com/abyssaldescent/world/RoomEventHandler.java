package com.abyssaldescent.world;

import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.event.DoorOpenedEvent;
import com.abyssaldescent.event.EnemyDeathEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.RoomClearedEvent;

public final class RoomEventHandler {
    private final RoomManager roomManager;
    private final EventBus eventBus;

    public RoomEventHandler(RoomManager roomManager, EventBus eventBus) {
        this.roomManager = roomManager;
        this.eventBus = eventBus;

        eventBus.subscribe(EnemyDeathEvent.class, this::handleEnemyDeath);
        eventBus.subscribe(DoorOpenedEvent.class, this::handleDoorOpened);
    }

    private void handleEnemyDeath(EnemyDeathEvent event) {
        Room currentRoom = roomManager.getCurrentRoom();
        if (currentRoom == null) {
            return;
        }

        for (Enemy enemy : currentRoom.getEnemies()) {
            if (enemy.getId().equals(event.getEnemyId())) {
                currentRoom.removeEnemy(enemy);
                break;
            }
        }

        if (currentRoom.isCleared()) {
            eventBus.post(new RoomClearedEvent(currentRoom.getId()));
        }
    }

    private void handleDoorOpened(DoorOpenedEvent event) {
        Room currentRoom = roomManager.getCurrentRoom();
        if (currentRoom == null) return;

        Door door = currentRoom.getDoor(event.getDoorId());
        if (door == null || door.getTargetRoomId() == null) return;

        if (!currentRoom.getType().spawnsEnemies() || currentRoom.isCleared()) {
            roomManager.transitionThroughDoor(door.getId());
        }
    }
}
