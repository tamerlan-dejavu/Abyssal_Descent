package com.abyssaldescent.event;

public final class KeyPickedUpEvent extends GameEvent {
    private final int currentKeys;
    private final int totalKeys;

    public KeyPickedUpEvent(int currentKeys, int totalKeys) {
        super();
        this.currentKeys = currentKeys;
        this.totalKeys   = totalKeys;
    }

    public int getCurrentKeys() { return currentKeys; }
    public int getTotalKeys()   { return totalKeys; }
}
