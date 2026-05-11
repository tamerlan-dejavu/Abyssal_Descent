package com.abyssaldescent.event;

public class TypedEvent {

    public enum Type {
        CHIP_SPAWNED,
        CHIP_COLLECTED,
        CHIP_ACTIVATED,
        CHIP_REPLACED,
        INVENTORY_CLEARED,

        ENEMY_DIED,
        ENEMY_DAMAGED,
        ROOM_CLEARED,

        SCREEN_FLASH,
        HUD_BANNER,
        PLAY_SOUND,

        PLAYER_DAMAGED,
        PLAYER_HEALED,
        PLAYER_DIED,
        PLAYER_RESPAWNED,
        PLAYER_ATTACK,
        GAME_OVER,
        BOSS_DEFEATED,
        FLOOR_CHANGED,

        KEY_PICKED_UP,
        KEY_USED,
    }

    private final Type type;
    private final Object payload;

    public TypedEvent(Type type, Object payload) {
        this.type    = type;
        this.payload = payload;
    }

    public Type getType()      { return type; }
    public Object getPayload() { return payload; }
}
