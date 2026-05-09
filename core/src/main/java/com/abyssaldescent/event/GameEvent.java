package com.abyssaldescent.event;

public abstract class GameEvent {
    private final long timestamp;

    protected GameEvent() {
        this.timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }
    
}
