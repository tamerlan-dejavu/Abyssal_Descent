package com.abyssaldescent;

/**
 * High-level game lifecycle phase managed by {@link GameController}.
 */
public enum GamePhase {
    /** Main menu / title screen, no run active. */
    MENU,
    /** A run is in progress and the world is being simulated. */
    PLAYING,
    /** A run is in progress but currently paused. */
    PAUSED,
    /** Karin has died, the current run is over. */
    GAME_OVER,
    /** Karin defeated Maltarion-Echo, the current run is won. */
    VICTORY
}
