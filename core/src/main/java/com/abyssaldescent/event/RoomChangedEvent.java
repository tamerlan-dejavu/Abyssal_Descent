package com.abyssaldescent.event;

public final class RoomChangedEvent extends GameEvent {
    private final String tierName;
    private final int    floorNumber;
    private final String roomId;

    public RoomChangedEvent(String tierName, int floorNumber, String roomId) {
        super();
        this.tierName    = tierName;
        this.floorNumber = floorNumber;
        this.roomId      = roomId;
    }

    public String getTierName()    { return tierName; }
    public int    getFloorNumber() { return floorNumber; }
    public String getRoomId()      { return roomId; }
}
