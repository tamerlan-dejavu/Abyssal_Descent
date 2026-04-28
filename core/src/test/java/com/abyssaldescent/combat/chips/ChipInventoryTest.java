package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;
import com.abyssaldescent.combat.strategy.MeleeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChipInventoryTest {

    private ChipInventory inventory;
    private CombatStrategy baseStrategy;

    @BeforeEach
    void setUp() {
        inventory = new ChipInventory();
        baseStrategy = new MeleeStrategy();
    }

    @Test
    void inventory_starts_with_all_slots_empty() {
        for (int i = 0; i < ChipInventory.SLOT_COUNT; i++) {
            assertNull(inventory.getSlot(i));
        }
    }

    @Test
    void equip_fills_slot_and_notifies_observer() {
        List<Integer> equippedSlots = new ArrayList<>();
        ChipInventoryObserver observer = new ChipInventoryObserver() {
            @Override
            public void onChipEquipped(int slot, ChipDecorator chip) {
                equippedSlots.add(slot);
            }
            @Override
            public void onChipRemoved(int slot) {}
        };
        inventory.addObserver(observer);

        DamageChip chip = new DamageChip(baseStrategy);
        assertTrue(inventory.equip(0, chip));
        assertEquals(1, equippedSlots.size());
        assertEquals(0, equippedSlots.get(0).intValue());
        assertSame(chip, inventory.getSlot(0));
    }

    @Test
    void remove_empties_slot_and_notifies_observer() {
        List<Integer> removedSlots = new ArrayList<>();
        ChipInventoryObserver observer = new ChipInventoryObserver() {
            @Override
            public void onChipEquipped(int slot, ChipDecorator chip) {}
            @Override
            public void onChipRemoved(int slot) {
                removedSlots.add(slot);
            }
        };
        inventory.addObserver(observer);

        DamageChip chip = new DamageChip(baseStrategy);
        inventory.equip(0, chip);
        removedSlots.clear();

        ChipDecorator removed = inventory.remove(0);
        assertEquals(1, removedSlots.size());
        assertEquals(0, removedSlots.get(0).intValue());
        assertSame(chip, removed);
        assertNull(inventory.getSlot(0));
    }

    @Test
    void equip_returns_false_when_slot_out_of_range() {
        DamageChip chip = new DamageChip(baseStrategy);
        assertFalse(inventory.equip(-1, chip));
        assertFalse(inventory.equip(ChipInventory.SLOT_COUNT, chip));
    }

    @Test
    void equip_returns_false_when_chip_null() {
        assertFalse(inventory.equip(0, null));
    }

    @Test
    void is_full_when_all_four_slots_occupied() {
        assertFalse(inventory.isFull());

        DamageChip chip1 = new DamageChip(baseStrategy);
        DamageChip chip2 = new DamageChip(baseStrategy);
        DamageChip chip3 = new DamageChip(baseStrategy);
        DamageChip chip4 = new DamageChip(baseStrategy);

        inventory.equip(0, chip1);
        assertFalse(inventory.isFull());

        inventory.equip(1, chip2);
        assertFalse(inventory.isFull());

        inventory.equip(2, chip3);
        assertFalse(inventory.isFull());

        inventory.equip(3, chip4);
        assertTrue(inventory.isFull());
    }

    @Test
    void get_first_empty_slot_returns_correct_index() {
        assertEquals(0, inventory.getFirstEmptySlot());

        DamageChip chip = new DamageChip(baseStrategy);
        inventory.equip(0, chip);
        assertEquals(1, inventory.getFirstEmptySlot());

        inventory.equip(1, chip);
        assertEquals(2, inventory.getFirstEmptySlot());
    }

    @Test
    void build_strategy_chains_all_equipped_chips() {
        DamageChip dmgChip = new DamageChip(baseStrategy);
        VampireChip vmpChip = new VampireChip(baseStrategy);

        inventory.equip(0, dmgChip);
        inventory.equip(1, vmpChip);

        CombatStrategy built = inventory.buildStrategy(baseStrategy);
        assertNotNull(built);
        assertNotSame(baseStrategy, built);

        int baseDmg = baseStrategy.calculateDamage(10);
        int builtDmg = built.calculateDamage(10);
        assertTrue(builtDmg > baseDmg, "Chained strategy should deal more damage");
    }

    @Test
    void build_strategy_with_empty_inventory_returns_base() {
        CombatStrategy built = inventory.buildStrategy(baseStrategy);
        assertSame(baseStrategy, built);
    }

    @Test
    void multiple_observers_all_notified() {
        List<Integer> observer1Slots = new ArrayList<>();
        List<Integer> observer2Slots = new ArrayList<>();

        ChipInventoryObserver obs1 = new ChipInventoryObserver() {
            @Override
            public void onChipEquipped(int slot, ChipDecorator chip) {
                observer1Slots.add(slot);
            }
            @Override
            public void onChipRemoved(int slot) {}
        };

        ChipInventoryObserver obs2 = new ChipInventoryObserver() {
            @Override
            public void onChipEquipped(int slot, ChipDecorator chip) {
                observer2Slots.add(slot);
            }
            @Override
            public void onChipRemoved(int slot) {}
        };

        inventory.addObserver(obs1);
        inventory.addObserver(obs2);

        DamageChip chip = new DamageChip(baseStrategy);
        inventory.equip(0, chip);

        assertEquals(1, observer1Slots.size());
        assertEquals(1, observer2Slots.size());
        assertEquals(0, observer1Slots.get(0).intValue());
        assertEquals(0, observer2Slots.get(0).intValue());
    }
}
