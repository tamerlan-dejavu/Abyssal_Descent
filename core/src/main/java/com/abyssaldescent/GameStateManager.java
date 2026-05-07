package com.abyssaldescent;

import com.abyssaldescent.config.DifficultySettings;
import com.abyssaldescent.entity.player.CharacterType;
import com.abyssaldescent.entity.player.PlayerSlot;
import com.abyssaldescent.entity.player.PlayerStatus;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.TierChangedEvent;
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
    private int   floorNumber          = 1;
    private float enemyDamageMultiplier = 1.0f;

    public PlayerSlot getKarinSlot() { return karinSlot; }
    
    public void activateKarin() {
        karinSlot.reset();
        karinSlot.setActive(true);
    }

    public int getFloorNumber() { return floorNumber; }

    public void setFloorNumber(int newFloor) {
        int previous = this.floorNumber;
        this.floorNumber = newFloor;
        EventBus.getInstance().post(new TierChangedEvent(previous, newFloor));
    }
    public void setPlayerStatus(CharacterType character, PlayerStatus newStatus) {
        PlayerStatus previous = karinSlot.getStatus();
        if (previous == newStatus) return;

        karinSlot.setStatus(newStatus);
        EventBus.getInstance().post(new PlayerStatusChangedEvent(character, previous, newStatus));
    }

    public float getEnemyDamageMultiplier() { return enemyDamageMultiplier; }

    public void initWithDifficulty(DifficultySettings difficulty) {
        int maxHp = Math.round(CharacterType.KARIN.getMaxHp() * difficulty.hpMultiplier);
        karinSlot.setEffectiveMaxHp(maxHp);
        karinSlot.setCurrentHp(maxHp);
        enemyDamageMultiplier = difficulty.enemyDamageMultiplier;
    }

    public void resetForNewRun() {
        floorNumber = 1;
        karinSlot.reset();
    }

    static void resetInstance() {
        instance = null;
    }
}
