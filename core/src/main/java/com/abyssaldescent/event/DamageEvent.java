package com.abyssaldescent.event;

public final class DamageEvent extends GameEvent {
    private final String targetId;
    private final int damage;
    private final String source;

    public DamageEvent(String targetId, int damage, String source) {
        this.targetId = targetId;
        this.damage = damage;
        this.source = source;
    }

    public String getTargetId() { return targetId; }
    public int getDamage() { return damage; }
    public String getSource() { return source; }
}
