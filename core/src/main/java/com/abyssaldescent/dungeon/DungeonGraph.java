package com.abyssaldescent.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DungeonGraph {

    private final Map<String, Room> rooms = new LinkedHashMap<String, Room>();
    private String startRoomId;

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public void connect(Room from, Room to, Direction dir) {
        Door outgoing = new Door(from.getId(), to.getId(), dir);
        Door incoming = new Door(to.getId(), from.getId(), dir.opposite());
        from.addDoor(outgoing);
        to.addDoor(incoming);
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public Room getStartRoom() {
        return rooms.get(startRoomId);
    }

    public void setStartRoomId(String id) {
        this.startRoomId = id;
    }

    public List<Room> getAllRooms() {
        return Collections.unmodifiableList(new ArrayList<Room>(rooms.values()));
    }

    public int size() {
        return rooms.size();
    }
}
