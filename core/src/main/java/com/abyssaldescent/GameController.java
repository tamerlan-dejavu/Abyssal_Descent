package com.abyssaldescent;

import com.abyssaldescent.entity.player.CharacterType;
import com.abyssaldescent.entity.player.PlayerSlot;
import com.abyssaldescent.entity.player.PlayerStatus;

import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.GamePhaseChangedEvent;

public final class GameController {
    public static final int FIRST_FLOOR = 1;
    public static final int FINAL_FLOOR = 3;
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
        if (!state.getKarinSlot().isActive() || state.getKarinSlot().getStatus() == PlayerStatus.GHOST) {
            return;
        }
        if (state.getKarinSlot().getStatus() == PlayerStatus.INVINCIBLE) {
            return;
        }
        state.getKarinSlot().applyDamage(damage);

        if (state.getKarinSlot().isDead()) {
            handleDeath(character);
        }
    }

    public void heal(CharacterType character, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0, got " + amount);
        }
        PlayerSlot slot = state.getKarinSlot();
        if (!slot.isActive() || slot.getStatus() == PlayerStatus.GHOST) {
            return;
        }
        slot.heal(amount);
    }


    private void handleDeath(CharacterType character) {
        state.setPlayerStatus(CharacterType.KARIN, PlayerStatus.GHOST);
        changePhase(GamePhase.GAME_OVER);
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
