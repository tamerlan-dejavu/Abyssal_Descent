package com.abyssaldescent.entity;

import com.abyssaldescent.command.AttackCommand;
import com.abyssaldescent.command.DashCommand;
import com.abyssaldescent.command.MoveCommand;
import com.abyssaldescent.event.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        EventBus.getInstance().clear();
        player = new Player(5, 5);
    }

    @Test
    void startsInIdleState() {
        assertSame(IdleState.INSTANCE, player.getCurrentState());
    }

    @Test
    void startsAtGivenPosition() {
        assertEquals(5, player.getX());
        assertEquals(5, player.getY());
    }

    @Test
    void moveCommand_transitionsToWalking() {
        player.pushCommand(new MoveCommand(1, 0));
        player.update(0.016f);

        assertSame(WalkingState.INSTANCE, player.getCurrentState());
    }

    @Test
    void attackCommand_transitionsToAttacking() {
        player.pushCommand(AttackCommand.INSTANCE);
        player.update(0.016f);

        assertSame(AttackingState.INSTANCE, player.getCurrentState());
    }

    @Test
    void dashCommand_transitionsToDashing() {
        player.pushCommand(DashCommand.INSTANCE);
        player.update(0.016f);

        assertSame(DashingState.INSTANCE, player.getCurrentState());
    }

    @Test
    void returnsToIdle_afterAttackDuration() {
        player.pushCommand(AttackCommand.INSTANCE);
        player.update(0.016f);
        assertSame(AttackingState.INSTANCE, player.getCurrentState());

        // Advance past attack duration
        player.update(0.5f);
        assertSame(IdleState.INSTANCE, player.getCurrentState());
    }

    @Test
    void returnsToIdle_afterDashDuration() {
        player.pushCommand(DashCommand.INSTANCE);
        player.update(0.016f);
        assertSame(DashingState.INSTANCE, player.getCurrentState());

        // Advance past dash duration
        player.update(0.5f);
        assertSame(IdleState.INSTANCE, player.getCurrentState());
    }

    @Test
    void inputFlags_areConsumedAfterUpdate() {
        player.pushCommand(AttackCommand.INSTANCE);
        player.update(0.016f);

        // Attack flag should be consumed
        assertFalse(player.getContext().isAttackRequested());
    }

    @Test
    void movement_changesPosition() {
        player.pushCommand(new MoveCommand(1, 0));
        player.update(0.016f); // transitions Idle -> Walking

        player.pushCommand(new MoveCommand(1, 0));
        player.update(0.1f);  // Walking applies movement

        assertTrue(player.getX() > 5, "Player should have moved right");
    }

    @Test
    void defaultCombatStrategy_isMelee() {
        assertEquals("Melee", player.getCombatStrategy().getName());
    }
}
