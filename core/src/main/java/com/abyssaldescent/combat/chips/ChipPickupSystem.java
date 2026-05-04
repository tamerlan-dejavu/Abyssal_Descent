package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.CombatManager;
import com.abyssaldescent.combat.strategy.CombatStrategy;
import com.abyssaldescent.event.ChipPickupEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;

public final class ChipPickupSystem {
    private final ChipFactory factory = new ChipFactory();
    private final ChipInventory inventory;
    private final CombatManager combatManager;
    private final EventBus eventBus;
    private final EventListener<ChipPickupEvent> pickupListener = this::onChipPickup;
    private CombatStrategy baseStrategy;

    public ChipPickupSystem(ChipInventory inventory, CombatManager combatManager, EventBus eventBus, CombatStrategy baseStrategy) {
        this.inventory = inventory;
        this.combatManager = combatManager;
        this.eventBus = eventBus;
        this.baseStrategy = baseStrategy;
        this.eventBus.subscribe(ChipPickupEvent.class, pickupListener);
    }

    void onChipPickup(ChipPickupEvent event) {
        ChipType chipType = event.getChipType();
        int slot = event.getPreferredSlot();

        if (slot < 0 || slot >= ChipInventory.SLOT_COUNT) {
            slot = inventory.getFirstEmptySlot();
            if (slot < 0) {
                return;
            }
        }

        ChipDecorator chip = factory.create(chipType, baseStrategy);
        if (inventory.equip(slot, chip)) {
            CombatStrategy newStrategy = inventory.buildStrategy(baseStrategy);
            combatManager.setPlayerStrategy(newStrategy);
        }
    }

    public void setBaseStrategy(CombatStrategy baseStrategy) {
        this.baseStrategy = baseStrategy;
    }

    public void dispose() {
        eventBus.unsubscribe(ChipPickupEvent.class, pickupListener);
    }

}
