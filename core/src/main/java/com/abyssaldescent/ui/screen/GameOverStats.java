package com.abyssaldescent.ui.screen;

public final class GameOverStats {
    public final int floorReached;
    public final int respawnsUsed;
    public final int maxRespawns;

    public GameOverStats(int floorReached, int respawnsUsed, int maxRespawns) {
        this.floorReached  = floorReached;
        this.respawnsUsed  = respawnsUsed;
        this.maxRespawns   = maxRespawns;
    }
}
