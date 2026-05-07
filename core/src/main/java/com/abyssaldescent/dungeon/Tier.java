package com.abyssaldescent.dungeon;

public enum Tier {
    UPPER_RUINS(1),
    FLOODED_CATACOMBS(2),
    MALTARIONS_ABYSS(3);

    public final int floorNumber;

    Tier(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    public static Tier fromFloor(int floor) {
        for (Tier t : values()) {
            if (t.floorNumber == floor) return t;
        }
        return UPPER_RUINS;
    }
}
