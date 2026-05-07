package com.abyssaldescent.ui.screen;

import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

public class RunStats {

    private float runTimeSeconds = 0f;
    private int   currentFloor  = 1;
    private int   enemiesKilled = 0;
    private int   chipsCollected = 0;
    private boolean active = true;

    public RunStats(TypedEventBus eventBus) {
        eventBus.subscribe(TypedEvent.Type.ENEMY_DIED, e -> {
            if (active) enemiesKilled++;
        });
        eventBus.subscribe(TypedEvent.Type.CHIP_COLLECTED, e -> {
            if (active) chipsCollected++;
        });
        eventBus.subscribe(TypedEvent.Type.FLOOR_CHANGED, e -> {
            if (active && e.getPayload() instanceof Integer) {
                currentFloor = (Integer) e.getPayload();
            }
        });
        eventBus.subscribe(TypedEvent.Type.GAME_OVER,     e -> active = false);
        eventBus.subscribe(TypedEvent.Type.BOSS_DEFEATED, e -> active = false);
    }

    public void update(float delta) {
        if (active) runTimeSeconds += delta;
    }

    public float getRunTimeSeconds()  { return runTimeSeconds; }
    public int   getCurrentFloor()    { return currentFloor; }
    public int   getEnemiesKilled()   { return enemiesKilled; }
    public int   getChipsCollected()  { return chipsCollected; }

    public String getFormattedTime() {
        int totalSec = (int) runTimeSeconds;
        int minutes  = totalSec / 60;
        int seconds  = totalSec % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void reset() {
        runTimeSeconds = 0f;
        currentFloor   = 1;
        enemiesKilled  = 0;
        chipsCollected = 0;
        active         = true;
    }
}
