package com.abyssaldescent.core.entity.view;

import com.abyssaldescent.core.entity.Companion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class CompanionView implements EntityView {
    private final Companion rayn; // Ссылка на реальный объект

    public CompanionView(Companion rayn) {
        this.rayn = rayn;
    }

    @Override
    public Vector2 getPosition() {
        return rayn.getPosition(); // Делегируем реальному объекту
    }

    @Override
    public boolean isVisible() {
        return true; // Рэйн всегда излучает свет
    }

    @Override
    public Color getLightColor() {
        // Логика Proxy: меняем свет в зависимости от состояния (State)
        if (rayn.isGhost()) {
            return new Color(0.2f, 0.5f, 0.9f, 0.6f); // Жутковатый синий свет призрака
        }
        return new Color(1.0f, 0.8f, 0.4f, 0.9f); // Теплый оранжевый свет (живой Рэйн)
    }

    @Override
    public float getLightDistance() {
        return rayn.isGhost() ? 3f : 6f; // Призрак светит тусклее
    }
}