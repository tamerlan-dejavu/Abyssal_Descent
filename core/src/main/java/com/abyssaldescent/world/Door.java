package com.abyssaldescent.world;

public final class Door {
    private final String id;
    private final int requiredKeyId;
    private final Direction direction;
    private final String targetRoomId;
    private final boolean locked;
    private boolean isOpen;

    public Door(String id, int requiredKeyId) {
        this(id, requiredKeyId, Direction.NORTH, null, requiredKeyId > 0);
    }

    public Door(String id, int requiredKeyId, Direction direction, String targetRoomId) {
        this(id, requiredKeyId, direction, targetRoomId, requiredKeyId > 0);
    }

    public Door(String id, int requiredKeyId, Direction direction, String targetRoomId, boolean locked) {
        this.id = id;
        this.requiredKeyId = requiredKeyId;
        this.direction = direction;
        this.targetRoomId = targetRoomId;
        this.locked = locked;
        this.isOpen = !locked;
    }

    public boolean canOpen(Key key) {
        if (!locked) return true;
        return key != null && key.getId() == requiredKeyId;
    }

    public void open() {
        this.isOpen = true;
    }

    public String getId() { return id; }
    public int getRequiredKeyId() { return requiredKeyId; }
    public Direction getDirection() { return direction; }
    public String getTargetRoomId() { return targetRoomId; }
    public boolean isLocked() { return locked; }
    public boolean isOpen() { return isOpen; }
}
