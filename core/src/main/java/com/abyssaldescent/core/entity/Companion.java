package com.abyssaldescent.core.entity;

import com.badlogic.gdx.math.Vector2;

public class Companion {
    private String name;
    private Vector2 position;
    private boolean isGhost;

    public Companion(String name) {
        this.name = name;
        this.position = new Vector2(0, 0); // Начальная позиция
        this.isGhost = false; // По умолчанию он живой, а не призрак
    }

    // Метод, который запрашивает CompanionView
    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }

    // Метод, который запрашивает CompanionView
    public boolean isGhost() {
        return isGhost;
    }

    // Вызовем этот метод, когда Рэйн погибнет
    public void setGhost(boolean ghost) {
        this.isGhost = ghost;
    }
}