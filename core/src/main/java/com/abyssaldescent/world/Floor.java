package com.abyssaldescent.world;

public final class Floor {
    private final int width;
    private final int height;
    private final int floorNumber;
    private final Tile[][] grid;
    private int spawnX;
    private int spawnY;
    private int exitX = -1;
    private int exitY = -1;

    public Floor(int width, int height, int floorNumber) {
        this.width = width;
        this.height = height;
        this.floorNumber = floorNumber;
        this.grid = new Tile[width][height];

        Tile wall = TileFactory.getInstance().get(TileType.WALL);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = wall;
            }
        }
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return TileFactory.getInstance().get(TileType.VOID);
        }
        return grid[x][y];
    }

    public void setTile(int x, int y, TileType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[x][y] = TileFactory.getInstance().get(type);
        }
    }

    public boolean isWalkable(int x, int y) {
        return getTile(x, y).isWalkable();
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getFloorNumber() { return floorNumber; }

    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }
    public void setSpawn(int x, int y) { this.spawnX = x; this.spawnY = y; }

    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }
    public void setExit(int x, int y) { this.exitX = x; this.exitY = y; }

    public int countTiles(TileType type) {
        Tile target = TileFactory.getInstance().get(type);
        int count = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == target) count++;
            }
        }
        return count;
    }
}
