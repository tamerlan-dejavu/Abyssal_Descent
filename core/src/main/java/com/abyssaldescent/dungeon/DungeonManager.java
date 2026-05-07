package com.abyssaldescent.dungeon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public final class DungeonManager {

    private static DungeonManager instance;

    private DungeonGraph graph;
    private Room         currentRoom;
    private Texture      backgroundTexture;

    private DungeonManager() {}

    public static DungeonManager getInstance() {
        if (instance == null) {
            instance = new DungeonManager();
        }
        return instance;
    }

    public void loadTier(Tier tier) {
        loadTier(tier, System.currentTimeMillis());
    }

    public void loadTier(Tier tier, long seed) {
        disposeCurrent();
        graph       = new DungeonGenerator(seed).generate(tier);
        currentRoom = graph.getStartRoom();
        loadBackground(tier);
    }

    public void transitionTo(String roomId) {
        Room next = graph.getRoom(roomId);
        if (next == null) {
            Gdx.app.error("DungeonManager", "Unknown room id: " + roomId);
            return;
        }
        currentRoom = next;
    }

    public Room        getCurrentRoom()      { return currentRoom; }
    public DungeonGraph getGraph()           { return graph; }
    public Texture     getBackgroundTexture() { return backgroundTexture; }

    public void dispose() {
        disposeCurrent();
        instance = null;
    }

    private void loadBackground(Tier tier) {
        String path = RoomTheme.getBackgroundPath(tier);
        if (Gdx.files.internal(path).exists()) {
            try {
                backgroundTexture = new Texture(Gdx.files.internal(path));
                backgroundTexture.setFilter(
                        Texture.TextureFilter.Linear,
                        Texture.TextureFilter.Linear);
                return;
            } catch (RuntimeException e) {
                Gdx.app.error("DungeonManager", "Failed to load background: " + path, e);
            }
        }
        Gdx.app.log("DungeonManager", "Background not found: " + path);
    }

    private void disposeCurrent() {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }
        graph       = null;
        currentRoom = null;
    }
}
