package com.abyssaldescent.combat.chips;

public interface ChipInventoryObserver {
    void onChipEquipped(int slot, ChipDecorator chip);
    void onChipRemoved(int slot);
}
