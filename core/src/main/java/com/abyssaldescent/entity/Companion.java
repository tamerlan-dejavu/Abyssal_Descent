package com.abyssaldescent.entity;

import com.badlogic.gdx.math.Vector2;

public class Companion {
    private String name;
    private Vector2 position;
    private boolean isGhost;

    public Companion(String name) {
        this.name = name;
        this.position = new Vector2(0, 0);
        this.isGhost = false;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    public boolean isGhost() {
        return isGhost;
    }

    public void setGhost(boolean ghost) {
        this.isGhost = ghost;
    }
}
