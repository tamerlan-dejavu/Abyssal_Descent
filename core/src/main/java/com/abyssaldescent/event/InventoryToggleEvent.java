package com.abyssaldescent.event;

public final class InventoryToggleEvent extends GameEvent {
    private final boolean open;

    public InventoryToggleEvent(boolean open) {
        super();
        this.open = open;
    }

    public boolean isOpen() { return open; }
}
