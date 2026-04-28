package com.abyssaldescent.event;

import com.abyssaldescent.combat.chips.ChipType;

public final class ChipEquippedEvent extends GameEvent {
    private final int slot;
    private final ChipType chipType;

    public ChipEquippedEvent(int slot, ChipType chipType) {
        this.slot = slot;
        this.chipType = chipType;
    }

    public int getSlot() {
        return slot;
    }

    public ChipType getChipType() {
        return chipType;
    }
}
