package com.abyssaldescent.core.command;

import com.abyssaldescent.core.entity.state.PlayerContext;

/**
 * Requests a dash (dodge-roll). The state machine will transition to
 * {@link com.abyssaldescent.core.entity.state.DashingState} if off cooldown.
 */
public final class DashCommand implements Command {

    public static final DashCommand INSTANCE = new DashCommand();

    private DashCommand() {}

    @Override
    public void execute(PlayerContext context) {
        context.setDashRequested(true);
    }
}
