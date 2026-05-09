package com.abyssaldescent.command;

import com.abyssaldescent.entity.player.PlayerContext;

public final class JumpCommand implements Command {
    public static final JumpCommand INSTANCE = new JumpCommand();

    private JumpCommand() {}

    @Override
    public void execute(PlayerContext context) {
        context.setJumpRequested(true);
    }
}
