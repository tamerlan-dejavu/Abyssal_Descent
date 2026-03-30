package com.abyssaldescent.event;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventBus {

    private static EventBus instance;
    private final Map<Class<?>, List<EventListener<?>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    public static EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public <T extends GameEvent> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public <T extends GameEvent> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> list = listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    
    public <T extends GameEvent> void post(T event) {
        List<EventListener<?>> list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) {
            return;
        }
        for (EventListener<?> listener : list) {
            ((EventListener<T>) listener).onEvent(event);
        }
    }

    public void clear() {
        listeners.clear();
    }

    public void clearFor(Class<? extends GameEvent> eventType) {
        listeners.remove(eventType);
    }
}
