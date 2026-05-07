package com.abyssaldescent;

import com.abyssaldescent.entity.player.CharacterType;
import com.abyssaldescent.entity.player.Player;
import com.abyssaldescent.entity.player.PlayerMemento;
import com.abyssaldescent.entity.player.PlayerSlot;
import com.abyssaldescent.entity.player.PlayerStatus;

import com.abyssaldescent.event.DamageEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.GamePhaseChangedEvent;
import com.abyssaldescent.event.HealthChangedEvent;
import com.abyssaldescent.event.RespawnUsedEvent;
import com.abyssaldescent.event.RoomChangedEvent;

public final class GameController {
    public static final int FIRST_FLOOR  = 1;
    public static final int FINAL_FLOOR  = 3;
    private static final int MAX_RESPAWNS = 3;

    private final GameStateManager state;
    private final EventBus         eventBus;
    private GamePhase phase = GamePhase.MENU;

    private int            respawnsRemaining = MAX_RESPAWNS;
    private PlayerMemento  savedMemento;
    private Player         playerRef;

    private final EventListener<DamageEvent> damageListener = this::onDamageEvent;

    public GameController() {
        this(GameStateManager.getInstance(), EventBus.getInstance());
    }

    GameController(GameStateManager state, EventBus eventBus) {
        this.state    = state;
        this.eventBus = eventBus;
        eventBus.subscribe(DamageEvent.class, damageListener);
    }

    public void startNewRun() {
        state.resetForNewRun();
        state.activateKarin();
        respawnsRemaining = MAX_RESPAWNS;
        changePhase(GamePhase.PLAYING);
    }

    public void initPlayer(Player player) {
        this.playerRef = player;
        PlayerSlot slot = state.getKarinSlot();
        savedMemento = new PlayerMemento(player.getX(), player.getY(), slot.getMaxHp());
        eventBus.post(new HealthChangedEvent(slot.getCurrentHp(), slot.getMaxHp()));
        eventBus.post(new RespawnUsedEvent(respawnsRemaining, MAX_RESPAWNS));
        int floor = state.getFloorNumber();
        eventBus.post(new RoomChangedEvent(tierName(floor), floor, roomId(floor)));
    }

    public void pause() {
        if (phase == GamePhase.PLAYING) changePhase(GamePhase.PAUSED);
    }

    public void resume() {
        if (phase == GamePhase.PAUSED) changePhase(GamePhase.PLAYING);
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
        int next = state.getFloorNumber();
        eventBus.post(new RoomChangedEvent(tierName(next), next, roomId(next)));
    }

    public void goToFloor(int floor) {
        requirePlaying();
        if (floor < FIRST_FLOOR || floor > FINAL_FLOOR) {
            throw new IllegalArgumentException(
                    "Floor must be in [" + FIRST_FLOOR + ".." + FINAL_FLOOR + "], got " + floor);
        }
        state.setFloorNumber(floor);
        eventBus.post(new RoomChangedEvent(tierName(floor), floor, roomId(floor)));
    }

    public void applyDamage(CharacterType character, int damage) {
        requirePlaying();
        if (damage < 0) throw new IllegalArgumentException("damage must be >= 0, got " + damage);
        applyDamageToPlayer(damage);
    }

    public void heal(CharacterType character, int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount must be >= 0, got " + amount);
        PlayerSlot slot = state.getKarinSlot();
        if (!slot.isActive() || slot.getStatus() == PlayerStatus.GHOST) return;
        slot.heal(amount);
        eventBus.post(new HealthChangedEvent(slot.getCurrentHp(), slot.getMaxHp()));
    }

    public void dispose() {
        eventBus.unsubscribe(DamageEvent.class, damageListener);
    }

    private void onDamageEvent(DamageEvent e) {
        if (!"PLAYER".equals(e.getTargetId()) || phase != GamePhase.PLAYING) return;
        if (e.getDamage() <= 0) return;
        applyDamageToPlayer(e.getDamage());
    }

    private void applyDamageToPlayer(int damage) {
        PlayerSlot slot = state.getKarinSlot();
        if (!slot.isActive() || slot.getStatus() == PlayerStatus.GHOST) return;
        if (slot.getStatus() == PlayerStatus.INVINCIBLE) return;
        slot.applyDamage(damage);
        eventBus.post(new HealthChangedEvent(slot.getCurrentHp(), slot.getMaxHp()));
        if (slot.isDead()) handleDeath();
    }

    private void handleDeath() {
        if (respawnsRemaining > 0 && savedMemento != null && playerRef != null) {
            respawnsRemaining--;
            playerRef.restoreMemento(savedMemento);
            state.setPlayerStatus(CharacterType.KARIN, PlayerStatus.ALIVE);
            eventBus.post(new RespawnUsedEvent(respawnsRemaining, MAX_RESPAWNS));
            PlayerSlot slot = state.getKarinSlot();
            eventBus.post(new HealthChangedEvent(slot.getCurrentHp(), slot.getMaxHp()));
        } else {
            state.setPlayerStatus(CharacterType.KARIN, PlayerStatus.GHOST);
            changePhase(GamePhase.GAME_OVER);
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
            throw new IllegalStateException(
                    "Operation requires PLAYING phase, current=" + phase);
        }
    }

    public GamePhase getPhase()         { return phase; }
    public GameStateManager getState()  { return state; }
    public boolean isRunActive()        { return phase == GamePhase.PLAYING || phase == GamePhase.PAUSED; }

    private static String tierName(int floor) {
        switch (floor) {
            case 1: return "Upper Ruins";
            case 2: return "Sunken Crypts";
            case 3: return "Void Core";
            default: return "Floor " + floor;
        }
    }

    private static String roomId(int floor) {
        return "R-" + String.format("%02d", floor);
    }
}
