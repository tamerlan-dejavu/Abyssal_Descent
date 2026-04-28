package com.abyssaldescent.ui;

import com.abyssaldescent.combat.chips.ChipDecorator;
import com.abyssaldescent.combat.chips.ChipInventoryObserver;

public final class ChipSlotWidget implements ChipInventoryObserver {
    private static final int SLOT_COUNT = 4;
    private final String[] labels = new String[SLOT_COUNT];

    public ChipSlotWidget() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            labels[i] = "Empty";
        }
    }

    @Override
    public void onChipEquipped(int slot, ChipDecorator chip) {
        if (slot >= 0 && slot < SLOT_COUNT && chip != null) {
            labels[slot] = chip.getName();
        }
    }

    @Override
    public void onChipRemoved(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            labels[slot] = "Empty";
        }
    }

    public String getSlotLabel(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return null;
        }
        return labels[slot];
    }

    public String[] getAllLabels() {
        String[] result = new String[SLOT_COUNT];
        System.arraycopy(labels, 0, result, 0, SLOT_COUNT);
        return result;
    }
}
