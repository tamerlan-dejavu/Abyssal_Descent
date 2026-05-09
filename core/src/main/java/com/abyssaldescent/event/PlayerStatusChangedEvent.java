package com.abyssaldescent.event;

import com.abyssaldescent.entity.player.CharacterType;
import com.abyssaldescent.entity.player.PlayerStatus;

public final class PlayerStatusChangedEvent extends GameEvent {
    private final CharacterType character;
    private final PlayerStatus previousStatus;
    private final PlayerStatus newStatus;

    public PlayerStatusChangedEvent(CharacterType character, PlayerStatus previousStatus,PlayerStatus newStatus) {
        super();
        this.character = character;
        this.previousStatus = previousStatus;
        this.newStatus  = newStatus;
    }

    public CharacterType getCharacter() { return character; }
    public PlayerStatus getPreviousStatus() { return previousStatus; }
    public PlayerStatus getNewStatus() { return newStatus; }
}
