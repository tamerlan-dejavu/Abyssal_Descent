package com.abyssaldescent.entity.effect;

import java.util.ArrayList;
import java.util.List;

public final class BloodEffectSystem {

    private static final float EFFECT_DURATION = 0.5f;
    private final List<BloodEffect> activeEffects = new ArrayList<>();

    public void spawnEffect(float x, float y) {
        int textureIndex = (int) (Math.random() * 2);
        activeEffects.add(new BloodEffect(x, y, EFFECT_DURATION, textureIndex));
    }

    public void update(float delta) {
        for (BloodEffect effect : activeEffects) {
            effect.update(delta);
        }
        activeEffects.removeIf(e -> !e.isActive());
    }

    public List<BloodEffect> getActiveEffects() {
        return activeEffects;
    }

    public void clear() {
        activeEffects.clear();
    }
}
