package com.abyssaldescent.event;


public final class FloorChangedEvent extends GameEvent {
    private final int previousFloor;
    private final int newFloor;

    public FloorChangedEvent(int previousFloor, int newFloor) {
        super();
        this.previousFloor = previousFloor;
        this.newFloor = newFloor;
    }

    public int getPreviousFloor() { return previousFloor; }

    public int getNewFloor() { return newFloor; }
}
