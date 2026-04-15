package com.abyssaldescent.entity.enemy;

import com.abyssaldescent.entity.enemy.ai.DeadState;
import com.abyssaldescent.entity.enemy.ai.EnemyState;
import com.abyssaldescent.entity.enemy.ai.EnemyStrategy;
import com.abyssaldescent.entity.enemy.ai.IdleState;
import com.abyssaldescent.event.EnemyDeathEvent;
import com.abyssaldescent.event.EventBus;

public final class Enemy {
    private final EnemyContext context;
    private final EnemyStrategy strategy;
    private EnemyState currentState;

    public Enemy(EnemyContext context, EnemyStrategy strategy) {
        this.context = context;
        this.strategy = strategy;
        this.currentState = IdleState.INSTANCE;
        this.currentState.enter(context);
    }

    public void update(float deltaTime) {
        if (currentState == DeadState.INSTANCE) {
            currentState.update(context, strategy, deltaTime);
            return;
        }

        EnemyState next = currentState.update(context, strategy, deltaTime);
        if (next != currentState) {
            currentState.exit(context);
            currentState = next;
            currentState.enter(context);
        }
    }

    public void takeDamage(int damage) {
        if (currentState == DeadState.INSTANCE) return;
        context.applyDamage(damage);
        if (context.isDead()) {
            currentState.exit(context);
            currentState = DeadState.INSTANCE;
            currentState.enter(context);
            EventBus.getInstance().post(new EnemyDeathEvent(
                    context.getId(),
                    context.getType().name(),
                    context.getPosition().x,
                    context.getPosition().y
            ));
        }
    }

    public void observeTarget(float targetX, float targetY, boolean visible) {
        context.setTargetPosition(targetX, targetY);
        context.setTargetVisible(visible);
    }

    public boolean isDead() { return currentState == DeadState.INSTANCE; }

    public String getId() { return context.getId(); }
    public EnemyType getType() { return context.getType(); }
    public EnemyContext getContext() { return context; }
    public EnemyStrategy getStrategy() { return strategy; }
    public EnemyState getCurrentState() { return currentState; }
    public String getStateName() { return currentState.getName(); }

    public float getX() { return context.getPosition().x; }
    public float getY() { return context.getPosition().y; }
    public int getCurrentHp() { return context.getCurrentHp(); }
}
