package com.abyssaldescent.ui;

import com.abyssaldescent.combat.chips.ChipDecorator;
import com.abyssaldescent.combat.chips.DamageChip;
import com.abyssaldescent.combat.strategy.CombatStrategy;
import com.abyssaldescent.combat.strategy.MeleeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChipSlotWidgetTest {

    private ChipSlotWidget widget;
    private CombatStrategy baseStrategy;

    @BeforeEach
    void setUp() {
        widget = new ChipSlotWidget();
        baseStrategy = new MeleeStrategy();
    }

    @Test
    void initial_labels_are_empty() {
        for (int i = 0; i < 4; i++) {
            assertEquals("Empty", widget.getSlotLabel(i));
        }
    }

    @Test
    void equip_updates_label() {
        DamageChip chip = new DamageChip(baseStrategy);
        widget.onChipEquipped(0, chip);
        assertEquals(chip.getName(), widget.getSlotLabel(0));
    }

    @Test
    void remove_resets_label_to_empty() {
        DamageChip chip = new DamageChip(baseStrategy);
        widget.onChipEquipped(0, chip);
        assertEquals(chip.getName(), widget.getSlotLabel(0));

        widget.onChipRemoved(0);
        assertEquals("Empty", widget.getSlotLabel(0));
    }

    @Test
    void all_four_slots_can_have_chips() {
        DamageChip chip = new DamageChip(baseStrategy);
        for (int i = 0; i < 4; i++) {
            widget.onChipEquipped(i, chip);
            assertEquals(chip.getName(), widget.getSlotLabel(i));
        }
    }

    @Test
    void get_all_labels_returns_copy() {
        DamageChip chip = new DamageChip(baseStrategy);
        widget.onChipEquipped(0, chip);

        String[] labels = widget.getAllLabels();
        assertEquals(4, labels.length);
        assertEquals(chip.getName(), labels[0]);
        assertEquals("Empty", labels[1]);
    }

    @Test
    void invalid_slot_returns_null() {
        assertNull(widget.getSlotLabel(-1));
        assertNull(widget.getSlotLabel(4));
    }
}
