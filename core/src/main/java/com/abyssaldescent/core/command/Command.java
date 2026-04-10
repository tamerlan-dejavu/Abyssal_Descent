package com.abyssaldescent.core.command;

import com.abyssaldescent.core.entity.state.PlayerContext;

/**
 * Command pattern — encapsulates a player action that can be executed
 * (and potentially undone) against the player context.
 */
public interface Command {

    /** Executes the command, mutating the player context as needed. */
    void execute(PlayerContext context);
}
