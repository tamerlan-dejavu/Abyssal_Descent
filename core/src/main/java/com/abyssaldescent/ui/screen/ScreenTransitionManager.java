package com.abyssaldescent.ui.screen;

import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

public class ScreenTransitionManager {

    public ScreenTransitionManager(TypedEventBus eventBus, RunStats stats) {
        eventBus.subscribe(TypedEvent.Type.GAME_OVER, event -> {
            UiManager.getInstance().showGameOver();
        });

        eventBus.subscribe(TypedEvent.Type.BOSS_DEFEATED, event -> {
            UiManager.getInstance().showEnding(stats);
        });
    }
}
