package com.abyssaldescent.core.entity.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

// Общий интерфейс для всех объектов, излучающих свет
public interface EntityView {
    Vector2 getPosition();
    boolean isVisible();
    Color getLightColor();
    float getLightDistance();
}