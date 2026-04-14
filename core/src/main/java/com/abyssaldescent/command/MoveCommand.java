package com.abyssaldescent.command;

import com.abyssaldescent.entity.PlayerContext;


public final class MoveCommand implements Command {

    private final float dirX;
    private final float dirY;

    public MoveCommand(float dirX, float dirY) {
        this.dirX = dirX;
        this.dirY = dirY;
    }

    @Override
    public void execute(PlayerContext context) {
        context.setMoveInput(dirX, dirY);
    }

    public float getDirX() { return dirX; }
    public float getDirY() { return dirY; }
}
