package com.abyssaldescent.dungeon;

public final class RoomTheme {

    private static final String PATH_UPPER_RUINS  = "ui/backgrounds/upper_ruins_arena.jpg";
    private static final String PATH_CATACOMBS    = "ui/backgrounds/catacombs_arena.jpg";
    private static final String PATH_MALTARION    = "ui/backgrounds/maltarion_arena.jpg";

    private RoomTheme() {}

    public static String getBackgroundPath(Tier tier) {
        switch (tier) {
            case UPPER_RUINS:       return PATH_UPPER_RUINS;
            case FLOODED_CATACOMBS: return PATH_CATACOMBS;
            case MALTARIONS_ABYSS:  return PATH_MALTARION;
            default:                return PATH_UPPER_RUINS;
        }
    }
}
