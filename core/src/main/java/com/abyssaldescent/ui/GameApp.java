package com.abyssaldescent.ui;

import com.abyssaldescent.ui.screen.MainMenuScreen;
import com.abyssaldescent.ui.screen.UiManager;
import com.badlogic.gdx.Game;

public class GameApp extends Game {

    @Override
    public void create() {
        UiManager.getInstance().init(this);
        setScreen(new MainMenuScreen());
    }
}
