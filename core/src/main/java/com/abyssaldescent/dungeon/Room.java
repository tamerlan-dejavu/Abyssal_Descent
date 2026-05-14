package com.abyssaldescent.dungeon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Room {

    private final String      id;
    private final RoomType    type;
    private final Tier        tier;
    private final List<Door>  doors;
    private boolean           cleared;
    private boolean           visited;

    // Grid position for minimap rendering (set by DungeonGenerator)
    private int gridX;
    private int gridY;

    // Chest for key (only in battle arenas)
    private Chest chest;

    public Room(String id, RoomType type, Tier tier) {
        this.id      = id;
        this.type    = type;
        this.tier    = tier;
        this.doors   = new ArrayList<Door>();
        this.cleared = (type == RoomType.STARTING || type == RoomType.SAVE_ROOM);
        this.visited = (type == RoomType.STARTING || type == RoomType.SAVE_ROOM);
        this.gridX   = 0;
        this.gridY   = 0;
        this.chest   = null;
    }

    public void addDoor(Door door) {
        // No limit — dungeon generator controls topology
        doors.add(door);
    }

    public String            getId()      { return id; }
    public RoomType          getType()    { return type; }
    public Tier              getTier()    { return tier; }
    public List<Door>        getDoors()   { return Collections.unmodifiableList(doors); }
    public boolean           isCleared()  { return cleared; }
    public void              setCleared(boolean v) { this.cleared = v; }
    public boolean           isVisited()  { return visited; }
    public void              setVisited(boolean v) { this.visited = v; }

    public int  getGridX()             { return gridX; }
    public int  getGridY()             { return gridY; }
    public void setGridPos(int x, int y) { this.gridX = x; this.gridY = y; }

    public Chest getChest()            { return chest; }
    public void  setChest(Chest c)     { this.chest = c; }
}
