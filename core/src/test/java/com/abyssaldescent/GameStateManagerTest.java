package com.abyssaldescent;

import com.abyssaldescent.core.entity.CharacterType;
import com.abyssaldescent.core.entity.PlayerSlot;
import com.abyssaldescent.core.entity.PlayerStatus;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.FloorChangedEvent;
import com.abyssaldescent.event.PlayerStatusChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateManagerTest {

    @BeforeEach
    void setUp() {
        // Reset singleton and event bus before every test for isolation.
        GameStateManager.resetInstance();
        EventBus.getInstance().clear();
    }

    // ── Singleton ─────────────────────────────────────────────────────────────

    @Test
    void getInstance_returnsSameObject() {
        GameStateManager a = GameStateManager.getInstance();
        GameStateManager b = GameStateManager.getInstance();
        assertSame(a, b, "getInstance must return the same instance every time");
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    void initialFloor_isOne() {
        assertEquals(1, GameStateManager.getInstance().getFloorNumber());
    }

    // ── Karin slot (always present) ───────────────────────────────────────────

    @Test
    void karinSlot_hasCorrectCharacterType() {
        PlayerSlot karin = GameStateManager.getInstance().getKarinSlot();
        assertEquals(CharacterType.KARIN, karin.getCharacterType());
    }

    @Test
    void activateKarin_setsSlotActiveAndResetsHp() {
        GameStateManager gsm = GameStateManager.getInstance();
        PlayerSlot karin = gsm.getKarinSlot();

        karin.applyDamage(40);          // damage before activation
        gsm.activateKarin();

        assertTrue(karin.isActive());
        assertEquals(CharacterType.KARIN.getMaxHp(), karin.getCurrentHp(),
                "activateKarin must restore HP to max");
        assertEquals(PlayerStatus.ALIVE, karin.getStatus());
    }

    // ── Rayn slot (AI companion) ──────────────────────────────────────────────

    @Test
    void raynSlot_inactiveByDefault() {
        assertFalse(GameStateManager.getInstance().getRaynSlot().isActive());
    }

    @Test
    void raynSlot_hasCorrectCharacterType() {
        assertEquals(CharacterType.RAYN,
                GameStateManager.getInstance().getRaynSlot().getCharacterType());
    }

    @Test
    void activateRayn_setsSlotActiveAndResetsHp() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.activateRayn();

        PlayerSlot rayn = gsm.getRaynSlot();
        assertTrue(rayn.isActive());
        assertEquals(CharacterType.RAYN.getMaxHp(), rayn.getCurrentHp());
        assertEquals(PlayerStatus.ALIVE, rayn.getStatus());
    }

    // ── getSlot helper ────────────────────────────────────────────────────────

    @Test
    void getSlot_returnsCorrectSlotForEachCharacter() {
        GameStateManager gsm = GameStateManager.getInstance();
        assertSame(gsm.getKarinSlot(), gsm.getSlot(CharacterType.KARIN));
        assertSame(gsm.getRaynSlot(),  gsm.getSlot(CharacterType.RAYN));
    }

    // ── Floor changes ─────────────────────────────────────────────────────────

    @Test
    void setFloorNumber_updatesFloor() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.setFloorNumber(5);
        assertEquals(5, gsm.getFloorNumber());
    }

    @Test
    void setFloorNumber_firesFloorChangedEvent() {
        List<FloorChangedEvent> captured = new ArrayList<>();
        EventBus.getInstance().subscribe(FloorChangedEvent.class, captured::add);

        GameStateManager.getInstance().setFloorNumber(3);

        assertEquals(1, captured.size());
        assertEquals(1, captured.get(0).getPreviousFloor());
        assertEquals(3, captured.get(0).getNewFloor());
    }

    // ── Player-status mutations ───────────────────────────────────────────────

    @Test
    void setPlayerStatus_updatesSlotStatus() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.setPlayerStatus(CharacterType.KARIN, PlayerStatus.GHOST);
        assertEquals(PlayerStatus.GHOST, gsm.getKarinSlot().getStatus());
    }

    @Test
    void setPlayerStatus_firesStatusChangedEvent() {
        List<PlayerStatusChangedEvent> captured = new ArrayList<>();
        EventBus.getInstance().subscribe(PlayerStatusChangedEvent.class, captured::add);

        GameStateManager.getInstance().setPlayerStatus(CharacterType.RAYN, PlayerStatus.GHOST);

        assertEquals(1, captured.size());
        PlayerStatusChangedEvent evt = captured.get(0);
        assertEquals(CharacterType.RAYN,    evt.getCharacter());
        assertEquals(PlayerStatus.ALIVE,    evt.getPreviousStatus());
        assertEquals(PlayerStatus.GHOST,    evt.getNewStatus());
    }

    @Test
    void setPlayerStatus_noOpIfStatusUnchanged() {
        List<PlayerStatusChangedEvent> captured = new ArrayList<>();
        EventBus.getInstance().subscribe(PlayerStatusChangedEvent.class, captured::add);

        GameStateManager gsm = GameStateManager.getInstance();
        gsm.setPlayerStatus(CharacterType.KARIN, PlayerStatus.ALIVE); // already ALIVE

        assertTrue(captured.isEmpty(), "No event should fire when status does not change");
    }

    // ── resetForNewRun ────────────────────────────────────────────────────────

    @Test
    void resetForNewRun_resetsFloorAndHp() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.activateKarin();
        gsm.activateRayn();

        gsm.getKarinSlot().applyDamage(30);
        gsm.getRaynSlot().applyDamage(20);
        gsm.setFloorNumber(10);

        gsm.resetForNewRun();

        assertEquals(1, gsm.getFloorNumber());
        assertEquals(CharacterType.KARIN.getMaxHp(), gsm.getKarinSlot().getCurrentHp());
        assertEquals(CharacterType.RAYN.getMaxHp(),  gsm.getRaynSlot().getCurrentHp());
    }

    @Test
    void resetForNewRun_resetsRaynHpEvenWhenInactive() {
        GameStateManager gsm = GameStateManager.getInstance();
        gsm.getRaynSlot().applyDamage(20);

        gsm.resetForNewRun();

        assertEquals(CharacterType.RAYN.getMaxHp(), gsm.getRaynSlot().getCurrentHp());
    }
}
