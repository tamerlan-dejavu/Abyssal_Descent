package com.abyssaldescent.event;

public final class RoomClearedEvent extends GameEvent {
    private final String roomId;

    public RoomClearedEvent(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }
}
