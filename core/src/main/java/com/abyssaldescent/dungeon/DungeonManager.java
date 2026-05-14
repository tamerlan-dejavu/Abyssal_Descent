package com.abyssaldescent.dungeon;

import com.abyssaldescent.audio.MusicPlayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public final class DungeonManager {

    private static DungeonManager instance;

    private DungeonGraph graph;
    private Room         currentRoom;
    private Texture      backgroundTexture;
    private MusicPlayer  musicPlayer;
    private int          keysCollected;

    private DungeonManager() {}

    public static DungeonManager getInstance() {
        if (instance == null) instance = new DungeonManager();
        return instance;
    }

    /** Must be called once before the first loadTier so music has a player to use. */
    public void setMusicPlayer(MusicPlayer player) {
        this.musicPlayer = player;
    }

    public void loadTier(Tier tier) {
        loadTier(tier, System.currentTimeMillis());
    }

    public void loadTier(Tier tier, long seed) {
        disposeCurrent();
        graph       = new DungeonGenerator(seed).generate(tier);
        currentRoom = graph.getStartRoom();
        keysCollected = 0;
        placeChests();
        applyRoomAssets(currentRoom);
    }

    public void transitionTo(String roomId) {
        Room next = graph.getRoom(roomId);
        if (next == null) {
            Gdx.app.error("DungeonManager", "Unknown room id: " + roomId);
            return;
        }
        currentRoom = next;
        applyRoomAssets(currentRoom);
    }

    public Room         getCurrentRoom()       { return currentRoom; }
    public DungeonGraph getGraph()             { return graph; }
    public Texture      getBackgroundTexture() { return backgroundTexture; }
    public int          getKeysCollected()     { return keysCollected; }
    public void         collectKey()           { keysCollected++; }
    public boolean      allKeysCollected()     { return keysCollected >= 4; }

    public void dispose() {
        disposeCurrent();
        instance = null;
    }

    // ── chest placement ───────────────────────────────────────────────────────

    private void placeChests() {
        java.util.List<Room> allRooms = graph.getAllRooms();
        java.util.Random rng = new java.util.Random();

        // Find one battle arena in each branch and add a chest with key
        java.util.List<Room> southArenas = new java.util.ArrayList<Room>();
        java.util.List<Room> eastArenas = new java.util.ArrayList<Room>();
        java.util.List<Room> westArenas = new java.util.ArrayList<Room>();

        for (Room room : allRooms) {
            if (room.getType() != RoomType.BATTLE_ARENA) continue;
            int gx = room.getGridX();
            if (gx == 0 && room.getGridY() < 0) southArenas.add(room);
            else if (gx > 0) eastArenas.add(room);
            else if (gx < 0) westArenas.add(room);
        }

        // Place one chest with key in each branch
        if (!southArenas.isEmpty()) {
            Room chosen = southArenas.get(rng.nextInt(southArenas.size()));
            chosen.setChest(new Chest(chosen.getId() + "_chest", true));
        }
        if (!eastArenas.isEmpty()) {
            Room chosen = eastArenas.get(rng.nextInt(eastArenas.size()));
            chosen.setChest(new Chest(chosen.getId() + "_chest", true));
        }
        if (!westArenas.isEmpty()) {
            Room chosen = westArenas.get(rng.nextInt(westArenas.size()));
            chosen.setChest(new Chest(chosen.getId() + "_chest", true));
        }
    }

    // ── asset loading ─────────────────────────────────────────────────────────

    private void applyRoomAssets(Room room) {
        loadBackground(room.getTier(), room.getType());
        if (musicPlayer != null) {
            musicPlayer.playTrack(RoomTheme.getMusicPath(room.getTier(), room.getType()));
        }
    }

    private void loadBackground(Tier tier, RoomType type) {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }

        String path = RoomTheme.getBackgroundPath(tier, type);
        if (Gdx.files.internal(path).exists()) {
            try {
                backgroundTexture = new Texture(Gdx.files.internal(path));
                backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return;
            } catch (RuntimeException e) {
                Gdx.app.error("DungeonManager", "Failed to load background: " + path, e);
            }
        }

        // Fallback: try the tier-wide arena background (legacy paths)
        String fallback = RoomTheme.getBackgroundPath(tier, RoomType.BATTLE_ARENA)
                .replace("rooms/", "ui/backgrounds/")
                .replace("/battle_arena.png", "_arena.jpg");
        if (Gdx.files.internal(fallback).exists()) {
            try {
                backgroundTexture = new Texture(Gdx.files.internal(fallback));
                backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return;
            } catch (RuntimeException e) {
                Gdx.app.error("DungeonManager", "Failed to load fallback background: " + fallback, e);
            }
        }

        Gdx.app.log("DungeonManager", "No background found for " + tier + "/" + type);
    }

    private void disposeCurrent() {
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
            backgroundTexture = null;
        }
        graph            = null;
        currentRoom      = null;
        keysCollected    = 0;
    }
}
