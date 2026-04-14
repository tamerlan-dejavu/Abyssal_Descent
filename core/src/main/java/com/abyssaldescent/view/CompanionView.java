package com.abyssaldescent.view;

import com.abyssaldescent.entity.Companion;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class CompanionView implements EntityView {
    private final Companion rayn;

    public CompanionView(Companion rayn) {
        this.rayn = rayn;
    }

    @Override
    public Vector2 getPosition() {
        return rayn.getPosition();
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public Color getLightColor() {
        if (rayn.isGhost()) {
            return new Color(0.2f, 0.5f, 0.9f, 0.6f);
        }
        return new Color(1.0f, 0.8f, 0.4f, 0.9f);
    }

    @Override
    public float getLightDistance() {
        return rayn.isGhost() ? 3f : 6f;
    }
}
