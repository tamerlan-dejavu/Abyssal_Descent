package com.abyssaldescent.core.command;

import com.abyssaldescent.core.entity.state.PlayerContext;

/**
 * Requests a melee attack. The state machine will transition to
 * {@link com.abyssaldescent.core.entity.state.AttackingState} if conditions are met.
 */
public final class AttackCommand implements Command {

    public static final AttackCommand INSTANCE = new AttackCommand();

    private AttackCommand() {}

    @Override
    public void execute(PlayerContext context) {
        context.setAttackRequested(true);
    }
}
