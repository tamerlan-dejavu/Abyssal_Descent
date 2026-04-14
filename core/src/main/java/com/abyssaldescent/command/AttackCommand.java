package com.abyssaldescent.command;

import com.abyssaldescent.entity.PlayerContext;

public final class AttackCommand implements Command {

    public static final AttackCommand INSTANCE = new AttackCommand();

    private AttackCommand() {}

    @Override
    public void execute(PlayerContext context) {
        context.setAttackRequested(true);
    }
}
