package com.abyssaldescent;

import com.abyssaldescent.core.entity.CharacterType;
import com.abyssaldescent.core.entity.PlayerStatus;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.GamePhaseChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    private GameController controller;

    @BeforeEach
    void setUp() {
        GameStateManager.resetInstance();
        EventBus.getInstance().clear();
        controller = new GameController();
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    void initialPhase_isMenu() {
        assertEquals(GamePhase.MENU, controller.getPhase());
        assertFalse(controller.isRunActive());
    }

    // ── Run lifecycle ─────────────────────────────────────────────────────────

    @Test
    void startNewRun_activatesKarinAndCompanionAndEntersPlaying() {
        controller.startNewRun();

        assertEquals(GamePhase.PLAYING, controller.getPhase());
        assertTrue(controller.getState().getKarinSlot().isActive());
        assertTrue(controller.getState().getRaynSlot().isActive());
        assertEquals(GameController.FIRST_FLOOR,
                controller.getState().getFloorNumber());
    }

    @Test
    void startNewRun_firesPhaseChangedEvent() {
        List<GamePhaseChangedEvent> captured = new ArrayList<>();
        EventBus.getInstance().subscribe(GamePhaseChangedEvent.class, captured::add);

        controller.startNewRun();

        assertEquals(1, captured.size());
        assertEquals(GamePhase.MENU, captured.get(0).getPreviousPhase());
        assertEquals(GamePhase.PLAYING, captured.get(0).getNewPhase());
    }

    @Test
    void pauseAndResume_togglePhase() {
        controller.startNewRun();
        controller.pause();
        assertEquals(GamePhase.PAUSED, controller.getPhase());

        controller.resume();
        assertEquals(GamePhase.PLAYING, controller.getPhase());
    }

    @Test
    void pause_isNoOpOutsidePlaying() {
        controller.pause();
        assertEquals(GamePhase.MENU, controller.getPhase());
    }

    @Test
    void abandonRun_returnsToMenu() {
        controller.startNewRun();
        controller.abandonRun();
        assertEquals(GamePhase.MENU, controller.getPhase());
    }

    // ── Floor progression ────────────────────────────────────────────────────

    @Test
    void advanceFloor_incrementsFloor() {
        controller.startNewRun();
        controller.advanceFloor();
        assertEquals(2, controller.getState().getFloorNumber());
    }

    @Test
    void advanceFloor_onFinalFloor_triggersVictory() {
        controller.startNewRun();
        controller.goToFloor(GameController.FINAL_FLOOR);
        controller.advanceFloor();
        assertEquals(GamePhase.VICTORY, controller.getPhase());
    }

    @Test
    void advanceFloor_requiresPlayingPhase() {
        assertThrows(IllegalStateException.class, () -> controller.advanceFloor());
    }

    @Test
    void goToFloor_outOfRange_throws() {
        controller.startNewRun();
        assertThrows(IllegalArgumentException.class, () -> controller.goToFloor(0));
        assertThrows(IllegalArgumentException.class,
                () -> controller.goToFloor(GameController.FINAL_FLOOR + 1));
    }

    // ── Damage / death ────────────────────────────────────────────────────────

    @Test
    void applyDamage_reducesHp() {
        controller.startNewRun();
        controller.applyDamage(CharacterType.KARIN, 25);
        assertEquals(CharacterType.KARIN.getMaxHp() - 25,
                controller.getState().getKarinSlot().getCurrentHp());
    }

    @Test
    void applyDamage_negative_throws() {
        controller.startNewRun();
        assertThrows(IllegalArgumentException.class,
                () -> controller.applyDamage(CharacterType.KARIN, -1));
    }

    @Test
    void applyDamage_invincibleKarin_takesNoDamage() {
        controller.startNewRun();
        controller.getState().setPlayerStatus(CharacterType.KARIN, PlayerStatus.INVINCIBLE);
        controller.applyDamage(CharacterType.KARIN, 999);
        assertEquals(CharacterType.KARIN.getMaxHp(),
                controller.getState().getKarinSlot().getCurrentHp());
    }

    @Test
    void applyDamage_lethalToKarin_endsRun() {
        controller.startNewRun();
        controller.applyDamage(CharacterType.KARIN, CharacterType.KARIN.getMaxHp());

        assertEquals(GamePhase.GAME_OVER, controller.getPhase());
        assertEquals(PlayerStatus.GHOST,
                controller.getState().getKarinSlot().getStatus());
    }

    @Test
    void applyDamage_lethalToRayn_setsGhostButContinuesRun() {
        controller.startNewRun();
        controller.applyDamage(CharacterType.RAYN, CharacterType.RAYN.getMaxHp());

        assertEquals(GamePhase.PLAYING, controller.getPhase());
        assertEquals(PlayerStatus.GHOST,
                controller.getState().getRaynSlot().getStatus());
    }

    @Test
    void applyDamage_inactiveSlot_isIgnored() {
        // No run started — both slots inactive.
        assertThrows(IllegalStateException.class,
                () -> controller.applyDamage(CharacterType.RAYN, 50));
    }

    // ── Healing & revive ─────────────────────────────────────────────────────

    @Test
    void heal_restoresHp() {
        controller.startNewRun();
        controller.applyDamage(CharacterType.KARIN, 30);
        controller.heal(CharacterType.KARIN, 10);
        assertEquals(CharacterType.KARIN.getMaxHp() - 20,
                controller.getState().getKarinSlot().getCurrentHp());
    }

    @Test
    void heal_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.heal(CharacterType.KARIN, -1));
    }

    @Test
    void reviveCompanionAtAltar_restoresRaynToHalfHp() {
        controller.startNewRun();
        controller.applyDamage(CharacterType.RAYN, CharacterType.RAYN.getMaxHp());
        assertEquals(PlayerStatus.GHOST,
                controller.getState().getRaynSlot().getStatus());

        controller.reviveCompanionAtAltar();

        assertEquals(PlayerStatus.ALIVE,
                controller.getState().getRaynSlot().getStatus());
        assertEquals(CharacterType.RAYN.getMaxHp() / 2,
                controller.getState().getRaynSlot().getCurrentHp());
    }

    @Test
    void reviveCompanionAtAltar_noOpIfNotGhost() {
        controller.startNewRun();
        controller.reviveCompanionAtAltar();
        assertEquals(CharacterType.RAYN.getMaxHp(),
                controller.getState().getRaynSlot().getCurrentHp());
    }
}
