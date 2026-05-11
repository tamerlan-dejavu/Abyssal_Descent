package com.abyssaldescent.ui.screen;

import com.abyssaldescent.config.DifficultySettings;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public final class UiManager {

    private static UiManager instance;
    private Game game;

    private UiManager() {}

    public static UiManager getInstance() {
        if (instance == null) instance = new UiManager();
        return instance;
    }

    public void init(Game game) {
        this.game = game;
    }

    public void showMainMenu() {
        navigateTo(new MainMenuScreen());
    }

    public void showDifficulty() {
        navigateTo(new DifficultyScreen());
    }

    public void showSettings() {
        navigateTo(new SettingsScreen());
    }

    public void startNewGame(DifficultySettings difficulty) {
        navigateTo(new GameScreen(difficulty));
    }

    public void continueGame() {
        navigateTo(new GameScreen(DifficultySettings.NORMAL));
    }

    public void showGameOver(GameOverStats stats) {
        navigateTo(new GameOverScreen(stats));
    }

    public void showGameOver() {
        navigateTo(new GameOverScreen(new GameOverStats(1, 0, 3)));
    }

    public void showEnding(RunStats stats) {
        navigateTo(new EndingScreen(stats));
    }

    /**
     * Posts navigation to the next frame via Gdx.app.postRunnable so it never
     * runs inside an active render() call, eliminating use-after-dispose crashes.
     */
    private void navigateTo(final Screen next) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                Screen prev = game.getScreen();
                game.setScreen(next);
                if (prev != null) {
                    prev.dispose();
                }
            }
        });
    }

    static void resetInstance() {
        instance = null;
    }
}
