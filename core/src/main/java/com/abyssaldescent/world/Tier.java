package com.abyssaldescent.world;

public enum Tier {
    SURFACE_CAVERNS(1, "Surface Caverns", 0.25f, 0.25f, 0.20f),
    DROWNED_DEPTHS(2, "Drowned Depths", 0.10f, 0.30f, 0.45f),
    LAVA_SANCTUM(3, "Lava Sanctum", 0.45f, 0.20f, 0.15f);

    private final int depth;
    private final String displayName;
    private final float ambientR;
    private final float ambientG;
    private final float ambientB;

    Tier(int depth, String displayName, float r, float g, float b) {
        this.depth = depth;
        this.displayName = displayName;
        this.ambientR = r;
        this.ambientG = g;
        this.ambientB = b;
    }

    public int getDepth() { return depth; }
    public String getDisplayName() { return displayName; }
    public float getAmbientR() { return ambientR; }
    public float getAmbientG() { return ambientG; }
    public float getAmbientB() { return ambientB; }

    public Tier next() {
        switch (this) {
            case SURFACE_CAVERNS: return DROWNED_DEPTHS;
            case DROWNED_DEPTHS:  return LAVA_SANCTUM;
            default: return null;
        }
    }
}
