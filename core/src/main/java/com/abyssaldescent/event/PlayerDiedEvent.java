package com.abyssaldescent.event;

public final class PlayerDiedEvent extends GameEvent {
    private final int respawnsLeft;

    public PlayerDiedEvent(int respawnsLeft) {
        this.respawnsLeft = respawnsLeft;
    }

    public int getRespawnsLeft() { return respawnsLeft; }
}
