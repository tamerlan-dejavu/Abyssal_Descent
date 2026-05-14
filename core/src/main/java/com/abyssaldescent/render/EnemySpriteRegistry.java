package com.abyssaldescent.render;

import com.abyssaldescent.entity.enemy.EnemyType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;


public final class EnemySpriteRegistry {
    private final Map<EnemyType, Texture> textures = new EnumMap<>(EnemyType.class);

    public EnemySpriteRegistry() {
        for (EnemyType type : EnemyType.values()) {
            String path = "enemies/" + type.name().toLowerCase(Locale.ROOT) + ".png";
            if (Gdx.files.internal(path).exists()) {
                try {
                    Texture t = new Texture(path);
                    t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    textures.put(type, t);
                    Gdx.app.log("EnemySpriteRegistry", "Loaded: " + path);
                } catch (Exception e) {
                    Gdx.app.error("EnemySpriteRegistry", "Failed to load: " + path, e);
                }
            } else {
                Gdx.app.log("EnemySpriteRegistry", "Not found: " + path);
            }
        }
    }

    public Texture get(EnemyType type) { return textures.get(type); }
    public boolean has(EnemyType type) { return textures.containsKey(type); }

    public void dispose() {
        for (Texture t : textures.values()) t.dispose();
        textures.clear();
    }
}
