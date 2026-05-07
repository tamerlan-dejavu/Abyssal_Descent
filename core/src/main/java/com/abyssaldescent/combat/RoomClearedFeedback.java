package com.abyssaldescent.combat;

import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;
import com.abyssaldescent.ui.feedback.ScreenFlash;

public class RoomClearedFeedback {

    public RoomClearedFeedback(TypedEventBus eventBus) {
        eventBus.subscribe(TypedEvent.Type.ROOM_CLEARED, e -> {
            eventBus.post(TypedEvent.Type.SCREEN_FLASH,
                    new ScreenFlash.ScreenFlashParams(ScreenFlash.FlashType.WHITE, 0.5f));
        });
    }
}
