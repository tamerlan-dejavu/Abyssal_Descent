package com.abyssaldescent.combat;

import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomEnemyTracker {

    private final TypedEventBus      eventBus;
    private final Set<String>        livingEnemyIds   = new HashSet<String>();
    private String                   currentRoomId;
    private boolean                  roomAlreadyCleared = false;

    public RoomEnemyTracker(TypedEventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.subscribe(TypedEvent.Type.ENEMY_DIED, e -> {
            Object payload = e.getPayload();
            if (payload instanceof String) {
                livingEnemyIds.remove(payload);
                if (!roomAlreadyCleared && livingEnemyIds.isEmpty()) {
                    fireRoomCleared();
                }
            }
        });
    }

    public void onEnterRoom(String roomId, List<String> enemyIds) {
        currentRoomId      = roomId;
        roomAlreadyCleared = false;
        livingEnemyIds.clear();
        livingEnemyIds.addAll(enemyIds);
        if (livingEnemyIds.isEmpty()) fireRoomCleared();
    }

    private void fireRoomCleared() {
        roomAlreadyCleared = true;
        eventBus.post(TypedEvent.Type.ROOM_CLEARED, currentRoomId);
    }
}
