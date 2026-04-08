package com.abyssaldescent;

import com.abyssaldescent.core.entity.CharacterType;
import com.abyssaldescent.core.entity.PlayerSlot;
import com.abyssaldescent.core.entity.PlayerStatus;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.GamePhaseChangedEvent;

/**
 * High-level orchestrator for the game lifecycle.
 *
 * <p>{@code GameController} sits on top of {@link GameStateManager} and the
 * {@link EventBus} and exposes the operations the UI / input layers need:
 * starting and ending a run, advancing floors, applying damage and healing,
 * pausing, and resolving death of Karin or Rayn.
 *
 * <p>The controller does not own world geometry, AI, or rendering — those
 * belong to dedicated subsystems. It is the single entry point for state
 * transitions that affect the run as a whole.
 */
public final class GameController {

    /** First (top-most) floor of the dungeon. */
    public static final int FIRST_FLOOR = 1;
    /** Last floor — the boss arena of Maltarion-Echo. */
    public static final int FINAL_FLOOR = 25;

    private final GameStateManager state;
    private final EventBus eventBus;

    private GamePhase phase = GamePhase.MENU;

    public GameController() {
        this(GameStateManager.getInstance(), EventBus.getInstance());
    }

    /** Test-friendly constructor allowing dependency injection. */
    GameController(GameStateManager state, EventBus eventBus) {
        this.state = state;
        this.eventBus = eventBus;
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Starts a fresh run from floor 1. Resets the game state, activates Karin
     * and her AI companion Rayn, and transitions the phase to
     * {@link GamePhase#PLAYING}.
     */
    public void startNewRun() {
        state.resetForNewRun();
        state.activateKarin();
        state.activateRayn();
        changePhase(GamePhase.PLAYING);
    }

    /** Pauses an active run. No-op if not currently playing. */
    public void pause() {
        if (phase == GamePhase.PLAYING) {
            changePhase(GamePhase.PAUSED);
        }
    }

    /** Resumes a paused run. No-op if not currently paused. */
    public void resume() {
        if (phase == GamePhase.PAUSED) {
            changePhase(GamePhase.PLAYING);
        }
    }

    /** Aborts the current run and returns to the menu phase. */
    public void abandonRun() {
        changePhase(GamePhase.MENU);
    }

    // ── Floor progression ────────────────────────────────────────────────────

    /**
     * Advances to the next floor. If the current floor is the final one,
     * triggers the {@link GamePhase#VICTORY} state instead.
     */
    public void advanceFloor() {
        requirePlaying();
        int current = state.getFloorNumber();
        if (current >= FINAL_FLOOR) {
            changePhase(GamePhase.VICTORY);
            return;
        }
        state.setFloorNumber(current + 1);
    }

    /**
     * Jumps directly to a specific floor (debug / cheat / save-load path).
     *
     * @throws IllegalArgumentException if {@code floor} is out of range.
     */
    public void goToFloor(int floor) {
        requirePlaying();
        if (floor < FIRST_FLOOR || floor > FINAL_FLOOR) {
            throw new IllegalArgumentException(
                    "Floor must be in [" + FIRST_FLOOR + ".." + FINAL_FLOOR + "], got " + floor);
        }
        state.setFloorNumber(floor);
    }

    // ── Combat resolution ────────────────────────────────────────────────────

    /**
     * Applies damage to the given character and resolves the consequences:
     * <ul>
     *   <li>Karin reaching 0 HP ends the run ({@link GamePhase#GAME_OVER}).</li>
     *   <li>Rayn reaching 0 HP enters {@link PlayerStatus#GHOST} until revived
     *       at an altar; the run continues.</li>
     * </ul>
     *
     * @param character target of the damage
     * @param damage    amount of damage (must be non-negative)
     */
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

    /** Heals the given character. Ghosts cannot be healed by this path. */
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

    /**
     * Revives Rayn at the altar (5 s activation handled by the world layer).
     * Restores 50% HP per the design document.
     */
    public void reviveCompanionAtAltar() {
        PlayerSlot rayn = state.getRaynSlot();
        if (!rayn.isActive() || rayn.getStatus() != PlayerStatus.GHOST) {
            return;
        }
        rayn.setCurrentHp(CharacterType.RAYN.getMaxHp() / 2);
        state.setPlayerStatus(CharacterType.RAYN, PlayerStatus.ALIVE);
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private void handleDeath(CharacterType character) {
        if (character == CharacterType.KARIN) {
            state.setPlayerStatus(CharacterType.KARIN, PlayerStatus.GHOST);
            changePhase(GamePhase.GAME_OVER);
        } else {
            // Rayn falls into a ghost state and waits for the altar.
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

    // ── Accessors ────────────────────────────────────────────────────────────

    public GamePhase getPhase() { return phase; }

    public GameStateManager getState() { return state; }

    public boolean isRunActive() {
        return phase == GamePhase.PLAYING || phase == GamePhase.PAUSED;
    }
}
