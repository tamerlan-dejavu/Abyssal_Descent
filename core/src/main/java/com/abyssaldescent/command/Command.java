package com.abyssaldescent.command;

import com.abyssaldescent.entity.PlayerContext;


public interface Command {
    void execute(PlayerContext context);
}
