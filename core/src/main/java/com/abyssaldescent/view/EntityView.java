package com.abyssaldescent.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public interface EntityView {
    Vector2 getPosition();
    boolean isVisible();
    Color getLightColor();
    float getLightDistance();
}
