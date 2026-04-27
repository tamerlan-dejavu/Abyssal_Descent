package com.abyssaldescent.command;

import com.abyssaldescent.entity.player.PlayerContext;

public final class InteractCommand implements Command {
    public static final InteractCommand INSTANCE = new InteractCommand();

    private InteractCommand() {}

    @Override
    public void execute(PlayerContext context) {
        context.setInteractRequested(true);
    }
}
