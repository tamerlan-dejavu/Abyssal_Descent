package com.abyssaldescent.event;


public interface EventListener<T extends GameEvent> {
    void onEvent1(ThreadLocal<BossDefeatedEvent> bossdefeatedevent);

    void onEvent(ThreadLocal<BossDefeatedEvent> bossdefeatedevent);
}
