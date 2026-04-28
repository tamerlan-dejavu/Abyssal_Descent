package com.abyssaldescent.event;

public final class StatusEffectEvent extends GameEvent {
    private final String targetId;
    private final StatusEffect effect;
    private final float magnitude;
    private final float duration;
    private final String sourceId;

    public StatusEffectEvent(String targetId, StatusEffect effect,
                             float magnitude, float duration, String sourceId) {
        this.targetId = targetId;
        this.effect   = effect;
        this.magnitude = magnitude;
        this.duration  = duration;
        this.sourceId  = sourceId;
    }

    public String getTargetId()   { return targetId; }
    public StatusEffect getEffect() { return effect; }
    public float getMagnitude()   { return magnitude; }
    public float getDuration()    { return duration; }
    public String getSourceId()   { return sourceId; }
}
