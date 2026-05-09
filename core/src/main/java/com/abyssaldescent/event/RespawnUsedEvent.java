package com.abyssaldescent.event;

public final class RespawnUsedEvent extends GameEvent {
    private final int remainingRespawns;
    private final int maxRespawns;

    public RespawnUsedEvent(int remainingRespawns, int maxRespawns) {
        super();
        this.remainingRespawns = remainingRespawns;
        this.maxRespawns       = maxRespawns;
    }

    public int getRemainingRespawns() { return remainingRespawns; }
    public int getMaxRespawns()       { return maxRespawns; }
}
