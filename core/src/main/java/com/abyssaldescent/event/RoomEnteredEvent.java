package com.abyssaldescent.event;

import com.abyssaldescent.world.Direction;

public final class RoomEnteredEvent extends GameEvent {
    private final String roomId;
    private final Direction entryDirection; // direction of the door the player walked through (null = start)

    public RoomEnteredEvent(String roomId, Direction entryDirection) {
        this.roomId = roomId;
        this.entryDirection = entryDirection;
    }

    public RoomEnteredEvent(String roomId) {
        this(roomId, null);
    }

    public String getRoomId() { return roomId; }

    /** Direction of the door used to enter this room (null for the very first room). */
    public Direction getEntryDirection() { return entryDirection; }
}
