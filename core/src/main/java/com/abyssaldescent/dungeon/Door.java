package com.abyssaldescent.dungeon;

public final class Door {

    private final String    fromRoomId;
    private final String    toRoomId;
    private final Direction direction;
    private boolean         locked;

    public Door(String fromRoomId, String toRoomId, Direction direction) {
        this.fromRoomId = fromRoomId;
        this.toRoomId   = toRoomId;
        this.direction  = direction;
        this.locked     = false;
    }

    public String    getFromRoomId() { return fromRoomId; }
    public String    getToRoomId()   { return toRoomId; }
    public Direction getDirection()  { return direction; }
    public boolean   isLocked()      { return locked; }
    public void      setLocked(boolean locked) { this.locked = locked; }
}
