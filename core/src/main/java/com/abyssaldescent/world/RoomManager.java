package com.abyssaldescent.world;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.RoomEnteredEvent;
import com.abyssaldescent.event.TierTransitionEvent;

import java.util.HashMap;
import java.util.Map;

public final class RoomManager {
    private final Map<String, Room> rooms = new HashMap<>();
    private Room currentRoom;
    private Dungeon dungeon;
    private EventBus eventBus;

    public RoomManager() {
    }

    public RoomManager(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void loadDungeon(Dungeon dungeon) {
        this.dungeon = dungeon;
        rooms.clear();
        for (Room room : dungeon.getAllRooms()) {
            rooms.put(room.getId(), room);
        }
        if (dungeon.getStartRoomId() != null) {
            setCurrentRoom(dungeon.getStartRoomId());
        }
    }

    public void addRoom(Room room) {
        if (room != null) {
            rooms.put(room.getId(), room);
        }
    }

    public void setCurrentRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) return;

        Tier oldTier = currentRoom != null ? currentRoom.getTier() : null;
        this.currentRoom = room;
        room.setVisited(true);

        if (eventBus != null) {
            eventBus.post(new RoomEnteredEvent(room.getId()));
            if (oldTier != null && oldTier != room.getTier()) {
                eventBus.post(new TierTransitionEvent(oldTier, room.getTier()));
            }
        }
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public boolean hasRoom(String roomId) {
        return rooms.containsKey(roomId);
    }

    public int getRoomCount() {
        return rooms.size();
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public boolean openDoor(String doorId, Key key) {
        if (currentRoom == null || key == null) {
            return false;
        }

        for (Door door : currentRoom.getDoors()) {
            if (door.getId().equals(doorId) && !door.isOpen()) {
                if (door.canOpen(key)) {
                    door.open();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean transitionThroughDoor(String doorId) {
        if (currentRoom == null) return false;
        Door door = currentRoom.getDoor(doorId);
        if (door == null || !door.isOpen()) return false;
        if (door.getTargetRoomId() == null) return false;
        setCurrentRoom(door.getTargetRoomId());
        return true;
    }
}
