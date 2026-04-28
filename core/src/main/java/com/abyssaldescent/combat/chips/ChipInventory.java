package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;

import java.util.ArrayList;
import java.util.List;

public final class ChipInventory {
    public static final int SLOT_COUNT = 4;
    private final ChipDecorator[] slots = new ChipDecorator[SLOT_COUNT];
    private final List<ChipInventoryObserver> observers = new ArrayList<>();

    public ChipInventory() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i] = null;
        }
    }

    public void addObserver(ChipInventoryObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void removeObserver(ChipInventoryObserver observer) {
        observers.remove(observer);
    }

    public boolean equip(int slot, ChipDecorator chip) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return false;
        }
        if (chip == null) {
            return false;
        }
        slots[slot] = chip;
        notifyChipEquipped(slot, chip);
        return true;
    }

    public ChipDecorator remove(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return null;
        }
        ChipDecorator removed = slots[slot];
        slots[slot] = null;
        if (removed != null) {
            notifyChipRemoved(slot);
        }
        return removed;
    }

    public ChipDecorator getSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return null;
        }
        return slots[slot];
    }

    public boolean isFull() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots[i] == null) {
                return false;
            }
        }
        return true;
    }

    public int getFirstEmptySlot() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots[i] == null) {
                return i;
            }
        }
        return -1;
    }

    public CombatStrategy buildStrategy(CombatStrategy base) {
        if (base == null) {
            throw new IllegalArgumentException("Base strategy cannot be null");
        }
        CombatStrategy result = base;
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slots[i] != null) {
                result = wrapWithChip(result, slots[i]);
            }
        }
        return result;
    }

    private CombatStrategy wrapWithChip(CombatStrategy base, ChipDecorator chip) {
        if (chip instanceof BerserkChip) {
            return new BerserkChip(base);
        } else if (chip instanceof DamageChip) {
            return new DamageChip(base);
        } else if (chip instanceof DashChip) {
            return new DashChip(base);
        } else if (chip instanceof DoubleJumpChip) {
            return new DoubleJumpChip(base);
        } else if (chip instanceof FireAuraChip) {
            return new FireAuraChip(base);
        } else if (chip instanceof HealthChip) {
            return new HealthChip(base);
        } else if (chip instanceof IceArrowChip) {
            return new IceArrowChip(base);
        } else if (chip instanceof ShieldChip) {
            return new ShieldChip(base);
        } else if (chip instanceof SpeedChip) {
            return new SpeedChip(base);
        } else if (chip instanceof VampireChip) {
            return new VampireChip(base);
        }
        return base;
    }

    private void notifyChipEquipped(int slot, ChipDecorator chip) {
        for (ChipInventoryObserver observer : observers) {
            observer.onChipEquipped(slot, chip);
        }
    }

    private void notifyChipRemoved(int slot) {
        for (ChipInventoryObserver observer : observers) {
            observer.onChipRemoved(slot);
        }
    }
}
