package com.abyssaldescent.dungeon;

public final class Chest {

    private final String id;
    private boolean opened;
    private boolean hasKey;

    public Chest(String id, boolean hasKey) {
        this.id = id;
        this.hasKey = hasKey;
        this.opened = false;
    }

    public void open() {
        this.opened = true;
    }

    public String getId()        { return id; }
    public boolean isOpened()    { return opened; }
    public boolean hasKey()      { return hasKey; }
}
