package com.abyssaldescent.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.abyssaldescent.event.BossDefeatedEvent;

public final class EventBus {

    private static final ThreadLocal<BossDefeatedEvent> bossDefeatedEvent = ThreadLocal.withInitial(BossDefeatedEvent::new);
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

    
    public <T extends GameEvent> void post1(BossHitEvent event) {
        List<EventListener<?>> list = listeners.get(event.getClass());
        if (list == null || list.isEmpty()) {
            return;
        }
        for (EventListener<?> listener : list) {
            ((EventListener<T>) listener).onEvent(bossDefeatedEvent);
        }
    }

    public void clear() {
        listeners.clear();
    }

    public void clearFor(Class<? extends GameEvent> eventType) {
        listeners.remove(eventType);
    }

    public void post(BossPhaseChangedEvent event) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'post'");
    }

    public void post(BossHitEvent event) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'post'");
    }

    public void post(com.abyssaldescent.event.BossDefeatedEvent bossDefeatedEvent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'post'");
    }

    public void unsubscribe1(Class<BossEnteredEvent> class1, Object listener) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unsubscribe'");
    }

    public void unsubscribe(Class<BossEnteredEvent> class1, Object listener) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unsubscribe'");
    }
}
