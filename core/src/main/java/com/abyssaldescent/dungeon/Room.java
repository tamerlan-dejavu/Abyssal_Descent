package com.abyssaldescent.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Room {

    private static final int MAX_DOORS_DEFAULT  = 2;
    private static final int MAX_DOORS_STARTING = 3;

    private final String   id;
    private final RoomType type;
    private final Tier     tier;
    private final List<Door> doors;
    private boolean cleared;

    public Room(String id, RoomType type, Tier tier) {
        this.id      = id;
        this.type    = type;
        this.tier    = tier;
        this.doors   = new ArrayList<Door>();
        this.cleared = (type == RoomType.STARTING || type == RoomType.SAVE_ROOM);
    }

    public boolean canAddDoor() {
        int max = (type == RoomType.STARTING) ? MAX_DOORS_STARTING : MAX_DOORS_DEFAULT;
        return doors.size() < max;
    }

    public void addDoor(Door door) {
        if (!canAddDoor()) {
            throw new IllegalStateException("Room " + id + " already has max doors");
        }
        doors.add(door);
    }

    public String             getId()      { return id; }
    public RoomType           getType()    { return type; }
    public Tier               getTier()    { return tier; }
    public List<Door>         getDoors()   { return Collections.unmodifiableList(doors); }
    public boolean            isCleared()  { return cleared; }
    public void               setCleared(boolean cleared) { this.cleared = cleared; }
}
