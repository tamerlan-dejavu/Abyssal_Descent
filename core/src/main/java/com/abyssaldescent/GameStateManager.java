package com.abyssaldescent;

import com.abyssaldescent.core.entity.CharacterType;
import com.abyssaldescent.core.entity.PlayerSlot;
import com.abyssaldescent.core.entity.PlayerStatus;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.FloorChangedEvent;
import com.abyssaldescent.event.PlayerStatusChangedEvent;


public final class GameStateManager {
    private static GameStateManager instance;

    private GameStateManager() {}

    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    private final PlayerSlot karinSlot = new PlayerSlot(CharacterType.KARIN);
    private final PlayerSlot raynSlot  = new PlayerSlot(CharacterType.RAYN);

    private int     floorNumber  = 1;
    private boolean multiplayer  = false;

    public PlayerSlot getKarinSlot() { return karinSlot; }
    public PlayerSlot getRaynSlot() { return raynSlot; }
    public PlayerSlot getSlot(CharacterType character) {
        return character == CharacterType.KARIN ? karinSlot : raynSlot;
    }

    public void activateKarin() {
        karinSlot.reset();
        karinSlot.setActive(true);
    }

    public void activateRayn() {
        raynSlot.reset();
        raynSlot.setActive(true);
    }

    public void deactivateRayn() {
        raynSlot.setActive(false);
    }

    public int getFloorNumber() { return floorNumber; }

    public void setFloorNumber(int newFloor) {
        int previous = this.floorNumber;
        this.floorNumber = newFloor;
        EventBus.getInstance().post(new FloorChangedEvent(previous, newFloor));
    }
    public void setPlayerStatus(CharacterType character, PlayerStatus newStatus) {
        PlayerSlot slot = getSlot(character);
        PlayerStatus previous = slot.getStatus();
        if (previous == newStatus) return;

        slot.setStatus(newStatus);
        EventBus.getInstance().post(new PlayerStatusChangedEvent(character, previous, newStatus));
    }

    public boolean isMultiplayer() { return multiplayer; }

    public void setMultiplayer(boolean multiplayer) { this.multiplayer = multiplayer; }
    public void resetForNewRun() {
        floorNumber = 1;
        karinSlot.reset();
        if (raynSlot.isActive()) raynSlot.reset();
    }
    static void resetInstance() {
        instance = null;
    }
}
