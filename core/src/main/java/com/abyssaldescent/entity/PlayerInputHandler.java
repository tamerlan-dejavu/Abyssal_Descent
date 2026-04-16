package com.abyssaldescent.entity;

import com.abyssaldescent.command.AttackCommand;
import com.abyssaldescent.command.DashCommand;
import com.abyssaldescent.command.MoveCommand;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;


public final class PlayerInputHandler extends InputAdapter {
    private final Player player;
    private Camera camera;
    private boolean attackPressed;
    private boolean dashPressed;
    private int attackScreenX;
    private int attackScreenY;

    private final Vector3 unprojectScratch = new Vector3();

    public PlayerInputHandler(Player player) {
        this.player = player;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void update() {
        float ix = 0;
        float iy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) iy += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) iy -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) ix -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) ix += 1;

        player.pushCommand(new MoveCommand(ix, iy));

        if (attackPressed) {
            aimAttackAtCursor();
            player.pushCommand(AttackCommand.INSTANCE);
            attackPressed = false;
        }
        if (dashPressed) {
            player.pushCommand(DashCommand.INSTANCE);
            dashPressed = false;
        }
    }

    private void aimAttackAtCursor() {
        if (camera == null) return;
        unprojectScratch.set(attackScreenX, attackScreenY, 0);
        camera.unproject(unprojectScratch);
        float dx = unprojectScratch.x - player.getX();
        float dy = unprojectScratch.y - player.getY();
        player.getContext().setFacing(dx, dy);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            attackPressed = true;
            attackScreenX = screenX;
            attackScreenY = screenY;
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
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
            return true;
        }
        return false;
    }
}
