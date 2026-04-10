package com.abyssaldescent;

import com.abyssaldescent.core.entity.CharacterType;
import com.abyssaldescent.core.entity.PlayerSlot;
import com.abyssaldescent.core.entity.PlayerStatus;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.GamePhaseChangedEvent;


public final class GameController {

    public static final int FIRST_FLOOR = 1;
    public static final int FINAL_FLOOR = 25;

    private final GameStateManager state;
    private final EventBus eventBus;

    private GamePhase phase = GamePhase.MENU;

    public GameController() {
        this(GameStateManager.getInstance(), EventBus.getInstance());
    }

    GameController(GameStateManager state, EventBus eventBus) {
        this.state = state;
        this.eventBus = eventBus;
    }

    
    public void startNewRun() {
        state.resetForNewRun();
        state.activateKarin();
        state.activateRayn();
        changePhase(GamePhase.PLAYING);
    }

    public void pause() {
        if (phase == GamePhase.PLAYING) {
            changePhase(GamePhase.PAUSED);
        }
    }
    public void resume() {
        if (phase == GamePhase.PAUSED) {
            changePhase(GamePhase.PLAYING);
        }
    }
    public void abandonRun() {
        changePhase(GamePhase.MENU);
    }


    public void advanceFloor() {
        requirePlaying();
        int current = state.getFloorNumber();
        if (current >= FINAL_FLOOR) {
            changePhase(GamePhase.VICTORY);
            return;
        }
        state.setFloorNumber(current + 1);
    }

   
    public void goToFloor(int floor) {
        requirePlaying();
        if (floor < FIRST_FLOOR || floor > FINAL_FLOOR) {
            throw new IllegalArgumentException(
                    "Floor must be in [" + FIRST_FLOOR + ".." + FINAL_FLOOR + "], got " + floor);
        }
        state.setFloorNumber(floor);
    }

    
    public void applyDamage(CharacterType character, int damage) {
        requirePlaying();
        if (damage < 0) {
            throw new IllegalArgumentException("damage must be >= 0, got " + damage);
        }
        PlayerSlot slot = state.getSlot(character);
        if (!slot.isActive() || slot.getStatus() == PlayerStatus.GHOST) {
            return;
        }
        if (slot.getStatus() == PlayerStatus.INVINCIBLE) {
            return;
        }
        slot.applyDamage(damage);

        if (slot.isDead()) {
            handleDeath(character);
        }
    }

    public void heal(CharacterType character, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0, got " + amount);
        }
        PlayerSlot slot = state.getSlot(character);
        if (!slot.isActive() || slot.getStatus() == PlayerStatus.GHOST) {
            return;
        }
        slot.heal(amount);
    }

    public void reviveCompanionAtAltar() {
        PlayerSlot rayn = state.getRaynSlot();
        if (!rayn.isActive() || rayn.getStatus() != PlayerStatus.GHOST) {
            return;
        }
        rayn.setCurrentHp(CharacterType.RAYN.getMaxHp() / 2);
        state.setPlayerStatus(CharacterType.RAYN, PlayerStatus.ALIVE);
    }


    private void handleDeath(CharacterType character) {
        if (character == CharacterType.KARIN) {
            state.setPlayerStatus(CharacterType.KARIN, PlayerStatus.GHOST);
            changePhase(GamePhase.GAME_OVER);
        } else {
            
            state.setPlayerStatus(CharacterType.RAYN, PlayerStatus.GHOST);
        }
    }

    private void changePhase(GamePhase next) {
        if (phase == next) return;
        GamePhase previous = phase;
        phase = next;
        eventBus.post(new GamePhaseChangedEvent(previous, next));
    }

    private void requirePlaying() {
        if (phase != GamePhase.PLAYING) {
            throw new IllegalStateException("Operation requires PLAYING phase, current=" + phase);
        }
    }

    public GamePhase getPhase() { return phase; }

    public GameStateManager getState() { return state; }

    public boolean isRunActive() {
        return phase == GamePhase.PLAYING || phase == GamePhase.PAUSED;
    }
}
