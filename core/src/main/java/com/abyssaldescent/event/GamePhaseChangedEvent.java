package com.abyssaldescent.event;

import com.abyssaldescent.GamePhase;

/**
 * Fired by {@code GameController} whenever the game lifecycle phase changes
 * (e.g. MENU → PLAYING, PLAYING → PAUSED, PLAYING → GAME_OVER).
 */
public final class GamePhaseChangedEvent extends GameEvent {

    private final GamePhase previousPhase;
    private final GamePhase newPhase;

    public GamePhaseChangedEvent(GamePhase previousPhase, GamePhase newPhase) {
        super();
        this.previousPhase = previousPhase;
        this.newPhase = newPhase;
    }

    public GamePhase getPreviousPhase() { return previousPhase; }

    public GamePhase getNewPhase() { return newPhase; }
}
