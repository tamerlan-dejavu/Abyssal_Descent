package com.abyssaldescent.event;

import com.abyssaldescent.event.GameEvent;

public class BossHitEvent extends GameEvent {
    private final int amount;
    private final int currentHp;
    private final int maxHp;

    public BossHitEvent(int amount, int currentHp, int maxHp) {
        super();
        this.amount    = amount;
        this.currentHp = currentHp;
        this.maxHp     = maxHp;
    }

    public int getAmount()    { return amount; }
    public int getCurrentHp() { return currentHp; }
    public int getMaxHp()     { return maxHp; }
}