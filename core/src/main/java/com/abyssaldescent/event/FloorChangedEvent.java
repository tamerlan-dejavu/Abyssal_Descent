package com.abyssaldescent.event;

/**
 * Posted by {@link com.abyssaldescent.GameStateManager} whenever the active floor number changes.
 * Listeners (HUD, AudioManager, NarrativeManager) react to update music, minimap, etc.
 */
public final class FloorChangedEvent extends GameEvent {

    private final int previousFloor;
    private final int newFloor;

    public FloorChangedEvent(int previousFloor, int newFloor) {
        super();
        this.previousFloor = previousFloor;
        this.newFloor      = newFloor;
    }

    /** Floor number before the transition. */
    public int getPreviousFloor() { return previousFloor; }

    /** Floor number after the transition (1–25). */
    public int getNewFloor() { return newFloor; }
}
