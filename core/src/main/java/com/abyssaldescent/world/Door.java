package com.abyssaldescent.world;

public final class Door {
    private final String id;
    private final int requiredKeyId;
    private boolean isOpen;

    public Door(String id, int requiredKeyId) {
        this.id = id;
        this.requiredKeyId = requiredKeyId;
        this.isOpen = false;
    }

    public boolean canOpen(Key key) {
        return key != null && key.getId() == requiredKeyId;
    }

    public void open() {
        this.isOpen = true;
    }

    public String getId() {
        return id;
    }

    public int getRequiredKeyId() {
        return requiredKeyId;
    }

    public boolean isOpen() {
        return isOpen;
    }
}
