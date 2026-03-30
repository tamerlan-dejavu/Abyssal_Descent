package com.abyssaldescent.event;


public interface EventListener<T extends GameEvent> {
    void onEvent(T event);
}
