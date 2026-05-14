package com.abyssaldescent.lwjgl3;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.abyssaldescent.ui.GameApp;

import java.util.ArrayList;
import java.util.List;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return;
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new GameApp(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("AbyssalDescent");
        configuration.useVsync(true);
        DisplayMode primary = Lwjgl3ApplicationConfiguration.getDisplayMode();
        configuration.setForegroundFPS(primary.refreshRate > 0 ? primary.refreshRate : 60);
        configuration.setFullscreenMode(primary);
        applyWindowIconIfAvailable(configuration, "libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }

    private static void applyWindowIconIfAvailable(Lwjgl3ApplicationConfiguration cfg, String... iconPaths) {
        ClassLoader cl = Lwjgl3Launcher.class.getClassLoader();
        List<String> available = new ArrayList<>();
        for (String path : iconPaths) {
            if (cl.getResource(path) != null) available.add(path);
        }
        if (!available.isEmpty()) {
            cfg.setWindowIcon(available.toArray(new String[0]));
        }
    }
}