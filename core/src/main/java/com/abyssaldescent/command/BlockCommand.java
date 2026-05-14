package com.abyssaldescent.command;

import com.abyssaldescent.entity.player.PlayerContext;

public final class BlockCommand implements Command {

    public static final BlockCommand INSTANCE = new BlockCommand();

    private BlockCommand() {}

    @Override
    public void execute(PlayerContext ctx) {
        ctx.setBlockActive(true);
    }
}
