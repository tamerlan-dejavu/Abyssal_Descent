package com.abyssaldescent.event;

import com.abyssaldescent.boss.BossPhase;
import com.abyssaldescent.event.GameEvent;

/** Босс перешёл в новую фазу. */
public class BossPhaseChangedEvent extends GameEvent {
    private final BossPhase newPhase;
    private final int       maxHp;
    private final int       currentHp;

    public BossPhaseChangedEvent(BossPhase newPhase, int maxHp, int currentHp) {
        super();
        this.newPhase  = newPhase;
        this.maxHp     = maxHp;
        this.currentHp = currentHp;
    }

    public BossPhase getNewPhase()  { return newPhase; }
    public int       getMaxHp()     { return maxHp; }
    public int       getCurrentHp() { return currentHp; }
}
