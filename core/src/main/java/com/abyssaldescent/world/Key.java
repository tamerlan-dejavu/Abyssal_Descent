package com.abyssaldescent.world;

import com.badlogic.gdx.math.Vector2;

public final class Key {
    private final int id;
    private final Vector2 position = new Vector2();
    private boolean collected;

    public Key(int id, float x, float y) {
        this.id = id;
        this.position.set(x, y);
        this.collected = false;
    }

    public void collect() {
        this.collected = true;
    }

    public int getId() {
        return id;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isCollected() {
        return collected;
    }
}
