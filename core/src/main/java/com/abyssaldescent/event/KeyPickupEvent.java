package com.abyssaldescent.event;

public final class KeyPickupEvent extends GameEvent {
    private final int keyId;

    public KeyPickupEvent(int keyId) {
        this.keyId = keyId;
    }

    public int getKeyId() {
        return keyId;
    }
}
