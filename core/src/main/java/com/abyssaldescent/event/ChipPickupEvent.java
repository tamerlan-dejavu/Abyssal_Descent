package com.abyssaldescent.event;

import com.abyssaldescent.combat.chips.ChipType;

public final class ChipPickupEvent extends GameEvent {
    private final ChipType chipType;
    private final int preferredSlot;

    public ChipPickupEvent(ChipType chipType) {
        this(chipType, -1);
    }

    public ChipPickupEvent(ChipType chipType, int preferredSlot) {
        this.chipType = chipType;
        this.preferredSlot = preferredSlot;
    }

    public ChipType getChipType() {
        return chipType;
    }

    public int getPreferredSlot() {
        return preferredSlot;
    }
}
