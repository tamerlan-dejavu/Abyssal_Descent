package com.abyssaldescent.event;

public final class TierChangedEvent extends GameEvent {
    private final int previousTier;
    private final int newTier;

    public TierChangedEvent(int previousTier, int newTier) {
        super();
        this.previousTier = previousTier;
        this.newTier = newTier;
    }

    public int getPreviousTier() { return previousTier; }
    public int getNewTier() { return newTier; }
}
