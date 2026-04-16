package com.abyssaldescent.world.tile;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

public class TileFactory {
    private static final Map<String, Tile> tiles = new HashMap<>();

    public static Tile getTile(String type, Texture texture) {
        if (!tiles.containsKey(type)) {
            tiles.put(type, new Tile(texture, type.equals("WALL")));
        }
        return tiles.get(type);
    }
}

class Tile {
    public Texture texture;
    public boolean isBlocking;

    public Tile(Texture texture, boolean isBlocking) {
        this.texture = texture;
        this.isBlocking = isBlocking;
    }
}
