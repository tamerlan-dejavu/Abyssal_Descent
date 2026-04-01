package com.abyssaldescent.event;

import com.abyssaldescent.core.entity.CharacterType;
import com.abyssaldescent.core.entity.PlayerStatus;

/**
 * Posted by {@link com.abyssaldescent.GameStateManager} when a character's
 * {@link PlayerStatus} transitions (e.g. ALIVE → GHOST on death, GHOST → INVINCIBLE after revival).
 *
 * <p>The HUD subscribes to update the ghost overlay; the AudioManager reacts to
 * {@code GHOST} transitions by muting that player's sound effects.
 */
public final class PlayerStatusChangedEvent extends GameEvent {

    private final CharacterType  character;
    private final PlayerStatus   previousStatus;
    private final PlayerStatus   newStatus;

    public PlayerStatusChangedEvent(CharacterType character,
                                    PlayerStatus previousStatus,
                                    PlayerStatus newStatus) {
        super();
        this.character      = character;
        this.previousStatus = previousStatus;
        this.newStatus      = newStatus;
    }

    /** Which character changed status. */
    public CharacterType getCharacter() { return character; }

    public PlayerStatus getPreviousStatus() { return previousStatus; }

    public PlayerStatus getNewStatus() { return newStatus; }
}
