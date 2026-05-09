package com.abyssaldescent.entity.player;

import com.abyssaldescent.command.AttackCommand;
import com.abyssaldescent.command.DashCommand;
import com.abyssaldescent.command.InteractCommand;
import com.abyssaldescent.command.JumpCommand;
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
    private boolean jumpPressed;
    private boolean dashPressed;
    private boolean interactPressed;
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
        // Horizontal movement only — no W/S
        float ix = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) ix -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) ix += 1;
        player.pushCommand(new MoveCommand(ix, 0));

        if (jumpPressed) {
            player.pushCommand(JumpCommand.INSTANCE);
            jumpPressed = false;
        }
        if (dashPressed) {
            player.pushCommand(DashCommand.INSTANCE);
            dashPressed = false;
        }
        if (attackPressed) {
            aimAttackAtCursor();
            player.pushCommand(AttackCommand.INSTANCE);
            attackPressed = false;
        }
        if (interactPressed) {
            player.pushCommand(InteractCommand.INSTANCE);
            interactPressed = false;
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
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
            jumpPressed = true;
            return true;
        }
        if (keycode == Input.Keys.SPACE) {
            // Space = jump from idle/air, dash from walking (states resolve priority)
            jumpPressed = true;
            dashPressed = true;
            return true;
        }
        if (keycode == Input.Keys.E) {
            interactPressed = true;
            return true;
        }
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
            return true;
        }
        return false;
    }
}
