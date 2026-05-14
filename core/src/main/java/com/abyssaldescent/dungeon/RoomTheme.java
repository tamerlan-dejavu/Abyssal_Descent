package com.abyssaldescent.dungeon;

/**
 * Centralises all asset paths for dungeon content so GameScreen / DungeonManager
 * never hard-code strings.
 *
 * Folder layout expected in assets/:
 *
 *   rooms/
 *     upper_ruins/
 *       starting.png   (or .jpg)
 *       battle_arena.png
 *       save_room.png
 *       final.png
 *     flooded_catacombs/
 *       starting.png
 *       battle_arena.png
 *       save_room.png
 *       final.png
 *     maltarions_abyss/
 *       starting.png
 *       battle_arena.png
 *       save_room.png
 *       final.png
 *
 *   doors/
 *     door_north.png
 *     door_south.png
 *     door_east.png
 *     door_west.png
 *
 *   music/
 *     upper_ruins/
 *       starting.mp3
 *       battle_arena.mp3
 *       save_room.mp3
 *       final.mp3
 *     flooded_catacombs/
 *       starting.mp3
 *       battle_arena.mp3
 *       save_room.mp3
 *       final.mp3
 *     maltarions_abyss/
 *       starting.mp3
 *       battle_arena.mp3
 *       save_room.mp3
 *       final.mp3
 */
public final class RoomTheme {

    private RoomTheme() {}

    // ── backgrounds ───────────────────────────────────────────────────────────

    public static String getBackgroundPath(Tier tier, RoomType type) {
        return "rooms/" + tierFolder(tier) + "/" + roomFile(type) + ".png";
    }

    /** Legacy overload — returns battle_arena background for the given tier. */
    public static String getBackgroundPath(Tier tier) {
        return getBackgroundPath(tier, RoomType.BATTLE_ARENA);
    }

    // ── doors ─────────────────────────────────────────────────────────────────

    public static String getDoorTexturePath(Direction direction) {
        return "doors/door_" + direction.name().toLowerCase() + ".png";
    }

    // ── music ─────────────────────────────────────────────────────────────────

    public static String getMusicPath(Tier tier, RoomType type) {
        return "music/" + tierFolder(tier) + "/" + roomFile(type) + ".mp3";
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static String tierFolder(Tier tier) {
        switch (tier) {
            case UPPER_RUINS:       return "upper_ruins";
            case FLOODED_CATACOMBS: return "flooded_catacombs";
            case MALTARIONS_ABYSS:  return "maltarions_abyss";
            default:                return "upper_ruins";
        }
    }

    private static String roomFile(RoomType type) {
        switch (type) {
            case STARTING:     return "starting";
            case BATTLE_ARENA: return "battle_arena";
            case SAVE_ROOM:    return "save_room";
            case FINAL:        return "final";
            default:           return "battle_arena";
        }
    }
}
