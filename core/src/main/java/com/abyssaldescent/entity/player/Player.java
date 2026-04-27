package com.abyssaldescent.entity.player;

import com.abyssaldescent.combat.strategy.CombatStrategy;
import com.abyssaldescent.combat.strategy.MeleeStrategy;
import com.abyssaldescent.command.Command;
import com.abyssaldescent.entity.state.IdleState;
import com.abyssaldescent.entity.state.PlayerState;

import java.util.ArrayDeque;
import java.util.Queue;


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

    public void pushCommand(Command command) {
        commandQueue.add(command);
    }


    public void update(float deltaTime) {
        while (!commandQueue.isEmpty()) {
            commandQueue.poll().execute(context);
        }

        PlayerState next = currentState.update(context, deltaTime);
        if (next != currentState) {
            currentState.exit(context);
            currentState = next;
            currentState.enter(context);
        }
        if (combatStrategy instanceof MeleeStrategy) {
            ((MeleeStrategy) combatStrategy).update(deltaTime);
        }

        context.consumeInputFlags();
    }

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
