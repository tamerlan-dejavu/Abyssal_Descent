package com.abyssaldescent.event;

public final class RoomEnteredEvent extends GameEvent {
    private final String roomId;

    public RoomEnteredEvent(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}
