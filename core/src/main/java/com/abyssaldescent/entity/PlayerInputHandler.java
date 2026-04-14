package com.abyssaldescent.entity;

import com.abyssaldescent.command.AttackCommand;
import com.abyssaldescent.command.DashCommand;
import com.abyssaldescent.command.MoveCommand;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;


public final class PlayerInputHandler extends InputAdapter {

    private final Player player;

    private boolean attackPressed;
    private boolean dashPressed;

    public PlayerInputHandler(Player player) {
        this.player = player;
    }

    public void update() {        float ix = 0;
        float iy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) iy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) iy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) ix -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) ix += 1;

        player.pushCommand(new MoveCommand(ix, iy));

        // ── One-shot actions (buffered via touchDown / keyDown) ─────────────
        if (attackPressed) {
            player.pushCommand(AttackCommand.INSTANCE);
            attackPressed = false;
        }
        if (dashPressed) {
            player.pushCommand(DashCommand.INSTANCE);
            dashPressed = false;
        }
    }

    // ── InputProcessor callbacks ────────────────────────────────────────────

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            attackPressed = true;
            return true;
        }
        if (button == Input.Buttons.RIGHT) {
            dashPressed = true;
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
        return false;
    }
}
