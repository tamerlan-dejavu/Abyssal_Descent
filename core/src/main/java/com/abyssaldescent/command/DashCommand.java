package com.abyssaldescent.command;

import com.abyssaldescent.entity.PlayerContext;

public final class DashCommand implements Command {

    public static final DashCommand INSTANCE = new DashCommand();

    private DashCommand() {}

    @Override
    public void execute(PlayerContext context) {
        context.setDashRequested(true);
    }
}
