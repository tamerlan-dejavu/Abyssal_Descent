package com.abyssaldescent.render;

import com.badlogic.gdx.math.Vector2;

public final class SpriteOrientation {
    public enum Facing { RIGHT, LEFT }
    private Facing facing = Facing.RIGHT;

    public void update(Vector2 facingVector) {
        update(facingVector.x);
    }

    public void update(float facingX) {
        if (facingX > 0.01f) {
            facing = Facing.RIGHT;
        } else if (facingX < -0.01f) {
            facing = Facing.LEFT;
        }
    }

    public Facing getFacing() { return facing; }

    public boolean isFlipX() { return facing == Facing.LEFT; }
}
