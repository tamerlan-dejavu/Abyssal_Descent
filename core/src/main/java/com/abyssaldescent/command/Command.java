package com.abyssaldescent.command;

import com.abyssaldescent.entity.player.PlayerContext;

public interface Command {
    void execute(PlayerContext context);
}
