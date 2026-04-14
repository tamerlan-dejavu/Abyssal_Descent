package com.abyssaldescent.entity;


public interface PlayerState {
    void enter(PlayerContext context);

    void exit(PlayerContext context);

    PlayerState update(PlayerContext context, float deltaTime);

    String getName();
}
