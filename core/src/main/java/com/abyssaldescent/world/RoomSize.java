package com.abyssaldescent.world;

public enum RoomSize {
    SMALL(15, 10),
    MEDIUM(22, 14),
    LARGE(32, 18),
    HUGE(40, 24);

    private final int width;
    private final int height;

    RoomSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
