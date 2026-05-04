package com.abyssaldescent.world;

public enum RoomSize {
    SMALL(16,  10),
    MEDIUM(16, 10),
    LARGE(16,  10),
    HUGE(16,   10);

    private final int width;
    private final int height;

    RoomSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
