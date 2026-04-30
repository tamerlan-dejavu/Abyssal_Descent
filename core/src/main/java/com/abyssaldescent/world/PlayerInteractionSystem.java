package com.abyssaldescent.world;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.PlayerInteractionEvent;

public final class PlayerInteractionSystem {
    private static final float DOOR_WALL_MARGIN = 1.5f;

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
        if (currentRoom == null) return;

        Door nearest = findNearestDoor(currentRoom, event);
        if (nearest != null) {
            doorOpeningSystem.tryOpenDoor(nearest.getId());
        }
    }

    private Door findNearestDoor(Room room, PlayerInteractionEvent event) {
        float px = event.getPlayerX();
        float py = event.getPlayerY();
        float range = event.getInteractionRange();
        float rangeSq = range * range;

        Door best = null;
        float bestDist = Float.MAX_VALUE;

        for (Door door : room.getDoors()) {
            float dx = doorWorldX(door, room);
            float dy = doorWorldY(door, room);
            float distSq = (px - dx) * (px - dx) + (py - dy) * (py - dy);
            if (distSq <= rangeSq && distSq < bestDist) {
                bestDist = distSq;
                best = door;
            }
        }
        return best;
    }

    private float doorWorldX(Door door, Room room) {
        switch (door.getDirection()) {
            case EAST: return room.getWidth() - DOOR_WALL_MARGIN;
            case WEST: return DOOR_WALL_MARGIN;
            default:   return room.getWidth() * 0.5f;
        }
    }

    private float doorWorldY(Door door, Room room) {
        switch (door.getDirection()) {
            case NORTH: return room.getHeight() - DOOR_WALL_MARGIN;
            case SOUTH: return DOOR_WALL_MARGIN;
            default:    return room.getHeight() * 0.5f;
        }
    }
}
