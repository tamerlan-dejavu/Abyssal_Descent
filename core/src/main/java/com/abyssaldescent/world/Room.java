package com.abyssaldescent.world;

public final class Room {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getCenterX() { return x + width / 2; }
    public int getCenterY() { return y + height / 2; }

    /** Check if this room overlaps another (with 1-tile padding). */
    public boolean overlaps(Room other) {
        return x - 1 < other.x + other.width
            && x + width + 1 > other.x
            && y - 1 < other.y + other.height
            && y + height + 1 > other.y;
    }
}
