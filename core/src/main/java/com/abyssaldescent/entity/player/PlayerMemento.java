package com.abyssaldescent.entity.player;

public final class PlayerMemento {
    private final float x;
    private final float y;
    private final int   hp;

    public PlayerMemento(float x, float y, int hp) {
        this.x  = x;
        this.y  = y;
        this.hp = hp;
    }

    public float getX()  { return x; }
    public float getY()  { return y; }
    public int   getHp() { return hp; }
}
