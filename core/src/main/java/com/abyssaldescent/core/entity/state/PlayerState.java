package com.abyssaldescent.core.entity.state;

/**
 * State pattern — represents a discrete state of the player character (Karin).
 * Each state controls how the player responds to input and updates per frame.
 */
public interface PlayerState {

    /** Called once when the player enters this state. */
    void enter(PlayerContext context);

    /** Called once when the player exits this state. */
    void exit(PlayerContext context);

    /** Called every frame. Returns the next state (may return {@code this}). */
    PlayerState update(PlayerContext context, float deltaTime);

    /** Human-readable name for debugging / logging. */
    String getName();
}
