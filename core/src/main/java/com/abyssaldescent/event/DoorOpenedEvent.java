package com.abyssaldescent.event;

public final class DoorOpenedEvent extends GameEvent {
    private final String doorId;
    private final int keyId;

    public DoorOpenedEvent(String doorId, int keyId) {
        this.doorId = doorId;
        this.keyId = keyId;
    }

    public String getDoorId() {
        return doorId;
    }

    public int getKeyId() {
        return keyId;
    }
}
