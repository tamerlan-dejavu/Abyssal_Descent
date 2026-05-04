package com.abyssaldescent.world;

public enum RoomSize {
    SMALL(20,  9),
    MEDIUM(28, 9),
    LARGE(38,  9),
    HUGE(50,   9);

    private final int width;
    private final int height;

    RoomSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
