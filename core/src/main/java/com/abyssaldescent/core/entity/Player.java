package com.abyssaldescent.core.entity;

import com.abyssaldescent.core.combat.CombatStrategy;
import com.abyssaldescent.core.combat.MeleeStrategy;
import com.abyssaldescent.core.command.Command;
import com.abyssaldescent.core.entity.state.IdleState;
import com.abyssaldescent.core.entity.state.PlayerContext;
import com.abyssaldescent.core.entity.state.PlayerState;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Karin Veil — the player character.
 *
 * <p>Integrates three GoF patterns:
 * <ul>
 *   <li><b>State</b> — {@link PlayerState} controls behaviour per frame.</li>
 *   <li><b>Command</b> — input is buffered as {@link Command} objects.</li>
 *   <li><b>Strategy</b> — {@link CombatStrategy} defines the combat style.</li>
 * </ul>
 *
 * <p>The game loop calls {@link #pushCommand(Command)} from the input layer,
 * then {@link #update(float)} once per frame.
 */
public final class Player {

    private final PlayerContext context = new PlayerContext();
    private PlayerState currentState;
    private final Queue<Command> commandQueue = new ArrayDeque<>();

    private CombatStrategy combatStrategy;

    public Player() {
        this(0, 0);
    }

    public Player(float startX, float startY) {
        context.setPosition(startX, startY);
        this.combatStrategy = new MeleeStrategy();
        this.currentState = IdleState.INSTANCE;
        currentState.enter(context);
    }

    /** Enqueue a command from the input layer. Processed on next update. */
    public void pushCommand(Command command) {
        commandQueue.add(command);
    }

    /**
     * Main update tick — processes queued commands, then delegates to
     * the current state. Called once per frame by the game loop.
     */
    public void update(float deltaTime) {
        // 1. Execute all queued commands (they set flags on the context)
        while (!commandQueue.isEmpty()) {
            commandQueue.poll().execute(context);
        }

        // 2. Let the current state decide the next state
        PlayerState next = currentState.update(context, deltaTime);
        if (next != currentState) {
            currentState.exit(context);
            currentState = next;
            currentState.enter(context);
        }

        // 3. Update combat strategy timer (combo window tracking)
        if (combatStrategy instanceof MeleeStrategy) {
            ((MeleeStrategy) combatStrategy).update(deltaTime);
        }

        // 4. Clear one-shot input flags
        context.consumeInputFlags();
    }

    // ── Accessors ───────────────────────────────────────────────────────────

    public PlayerContext getContext() { return context; }

    public float getX() { return context.getPosition().x; }
    public float getY() { return context.getPosition().y; }

    public PlayerState getCurrentState() { return currentState; }
    public String getStateName() { return currentState.getName(); }

    public boolean isInvincible() { return context.isInvincible(); }

    public CombatStrategy getCombatStrategy() { return combatStrategy; }

    public void setCombatStrategy(CombatStrategy strategy) {
        this.combatStrategy = strategy;
    }
}
