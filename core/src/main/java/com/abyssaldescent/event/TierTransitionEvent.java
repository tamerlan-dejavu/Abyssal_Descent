package com.abyssaldescent.event;

import com.abyssaldescent.world.Tier;

public final class TierTransitionEvent extends GameEvent {
    private final Tier fromTier;
    private final Tier toTier;

    public TierTransitionEvent(Tier fromTier, Tier toTier) {
        this.fromTier = fromTier;
        this.toTier = toTier;
    }

    public Tier getFromTier() { return fromTier; }
    public Tier getToTier() { return toTier; }
}
