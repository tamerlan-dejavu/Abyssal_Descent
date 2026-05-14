package com.abyssaldescent.entity.player;

import com.abyssaldescent.command.AttackCommand;
import com.abyssaldescent.command.BlockCommand;
import com.abyssaldescent.command.DashCommand;
import com.abyssaldescent.command.InteractCommand;
import com.abyssaldescent.command.MoveCommand;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;


public final class PlayerInputHandler extends InputAdapter {
    private final Player player;
    private boolean attackPressed;
    private boolean blockPressed;
    private boolean dashPressed;
    private boolean interactPressed;

    public PlayerInputHandler(Player player) {
        this.player = player;
    }

    public void update() {
        float ix = 0, iy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) ix -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) ix += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) iy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) iy -= 1;
        player.pushCommand(new MoveCommand(ix, iy));

        if (dashPressed) {
            player.pushCommand(DashCommand.INSTANCE);
            dashPressed = false;
        }
        if (attackPressed) {
            player.pushCommand(AttackCommand.INSTANCE);
            attackPressed = false;
        }
        if (blockPressed) {
            player.pushCommand(BlockCommand.INSTANCE);
            blockPressed = false;
        }
        if (interactPressed) {
            player.pushCommand(InteractCommand.INSTANCE);
            interactPressed = false;
        }
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            attackPressed = true;
            return true;
        }
        if (button == Input.Buttons.RIGHT) {
            blockPressed = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.SPACE) {
            dashPressed = true;
            return true;
        }
        if (keycode == Input.Keys.E) {
            interactPressed = true;
            return true;
        }
        if (keycode == Input.Keys.K) {
            player.takeDamage(Integer.MAX_VALUE);
            return true;
        }
        if (keycode == Input.Keys.ESCAPE) {
            return true;
        }
        return false;
    }
}
