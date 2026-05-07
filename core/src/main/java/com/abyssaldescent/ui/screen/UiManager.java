package com.abyssaldescent.ui.screen;

import com.abyssaldescent.config.DifficultySettings;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

/**
 * Singleton facade that owns all screen-navigation logic.
 * Call {@link #init(Game)} once from {@code GameApp.create()}.
 */
public final class UiManager {

    private static UiManager instance;
    private Game game;

    private UiManager() {}

    public static UiManager getInstance() {
        if (instance == null) instance = new UiManager();
        return instance;
    }

    /** Must be called before any navigation method. */
    public void init(Game game) {
        this.game = game;
    }

    public void showMainMenu() {
        navigate(new MainMenuScreen());
    }

    public void showDifficulty() {
        navigate(new DifficultyScreen());
    }

    public void showSettings() {
        navigate(new SettingsScreen());
    }

    public void startNewGame(DifficultySettings difficulty) {
        navigate(new GameScreen(difficulty));
    }

    public void continueGame() {
        navigate(new GameScreen(DifficultySettings.NORMAL));
    }

    public void showGameOver() {
        navigate(new GameOverScreen());
    }

    private void navigate(Screen next) {
        Screen prev = game.getScreen();
        game.setScreen(next);
        if (prev != null) prev.dispose();
    }

    static void resetInstance() {
        instance = null;
    }
}
