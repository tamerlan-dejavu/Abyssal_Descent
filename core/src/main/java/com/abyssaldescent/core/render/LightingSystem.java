package com.abyssaldescent.core.render;


import com.abyssaldescent.core.entity.view.EntityView;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.World;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import java.util.HashMap;
import java.util.Map;

public class LightingSystem {
    private final RayHandler rayHandler;
    private final Map<EntityView, PointLight> entityLights = new HashMap<>();

    public LightingSystem(World physicsWorld) {
        // Инициализация обработчика света
        this.rayHandler = new RayHandler(physicsWorld);
        
        // Настройка темноты: R, G, B, Alpha (0.1 - очень темно)
        this.rayHandler.setAmbientLight(0.1f, 0.1f, 0.2f, 0.5f);
        this.rayHandler.setBlurNum(3);
    }

    public void attachLightToEntity(EntityView view) {
        // Создаем источник света: 
        // (обработчик, лучи, цвет, дистанция, x, y)
        PointLight light = new PointLight(
            rayHandler, 
            128, 
            view.getLightColor(), 
            view.getLightDistance(), 
            view.getPosition().x, 
            view.getPosition().y
        );
        entityLights.put(view, light);
    }

    public void updateAndRender(OrthographicCamera camera) {
        for (Map.Entry<EntityView, PointLight> entry : entityLights.entrySet()) {
            EntityView view = entry.getKey();
            PointLight light = entry.getValue();

            light.setPosition(view.getPosition());
            light.setColor(view.getLightColor());
            light.setDistance(view.getLightDistance());
        }

        rayHandler.setCombinedMatrix(camera);
        rayHandler.updateAndRender();
    }

    public void dispose() {
        if (rayHandler != null) rayHandler.dispose();
    }
}