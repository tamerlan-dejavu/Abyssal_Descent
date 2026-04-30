package com.abyssaldescent.world;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Dungeon {
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<Tier, String> bossRoomIds = new EnumMap<>(Tier.class);
    private String startRoomId;
    private String finalBossRoomId;

    public void addRoom(Room room) {
        if (room != null) {
            rooms.put(room.getId(), room);
        }
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public boolean hasRoom(String id) {
        return rooms.containsKey(id);
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public List<Room> getRoomsByTier(Tier tier) {
        List<Room> result = new ArrayList<>();
        for (Room room : rooms.values()) {
            if (room.getTier() == tier) {
                result.add(room);
            }
        }
        return result;
    }

    public int getRoomCount() {
        return rooms.size();
    }

    public void setStartRoomId(String id) { this.startRoomId = id; }
    public String getStartRoomId() { return startRoomId; }

    public void setBossRoomId(Tier tier, String id) { bossRoomIds.put(tier, id); }
    public String getBossRoomId(Tier tier) { return bossRoomIds.get(tier); }

    public void setFinalBossRoomId(String id) { this.finalBossRoomId = id; }
    public String getFinalBossRoomId() { return finalBossRoomId; }
}
