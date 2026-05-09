package com.abyssaldescent.entity.state;

import com.abyssaldescent.entity.player.PlayerContext;

public interface PlayerState {
    void enter(PlayerContext context);
    void exit(PlayerContext context);
    PlayerState update(PlayerContext context, float deltaTime);
    String getName();
}
