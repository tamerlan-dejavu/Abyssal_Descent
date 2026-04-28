package com.abyssaldescent.world;

import java.util.HashMap;
import java.util.Map;

public final class RoomManager {
    private final Map<String, Room> rooms = new HashMap<>();
    private Room currentRoom;

    public void addRoom(Room room) {
        if (room != null) {
            rooms.put(room.getId(), room);
        }
    }

    public void setCurrentRoom(String roomId) {
        Room room = rooms.get(roomId);
        if (room != null) {
            this.currentRoom = room;
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
}
