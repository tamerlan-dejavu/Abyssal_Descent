package com.abyssaldescent.entity.enemy;

import java.util.ArrayList;
import java.util.List;

public final class EnemyAttackEffectSystem {

    private static final float EFFECT_DURATION = 0.3f;
    private final List<EnemyAttackEffect> activeEffects = new ArrayList<>();

    public void spawnEffect(float x, float y) {
        activeEffects.add(new EnemyAttackEffect(x, y, EFFECT_DURATION));
    }

    public void update(float delta) {
        for (EnemyAttackEffect effect : activeEffects) {
            effect.update(delta);
        }
        activeEffects.removeIf(e -> !e.isActive());
    }

    public List<EnemyAttackEffect> getActiveEffects() {
        return activeEffects;
    }

    public void clear() {
        activeEffects.clear();
    }
}
