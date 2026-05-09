package com.abyssaldescent.event;

public final class HealthChangedEvent extends GameEvent {
    private final int currentHp;
    private final int maxHp;

    public HealthChangedEvent(int currentHp, int maxHp) {
        super();
        this.currentHp = currentHp;
        this.maxHp     = maxHp;
    }

    public int getCurrentHp() { return currentHp; }
    public int getMaxHp()     { return maxHp; }
}
