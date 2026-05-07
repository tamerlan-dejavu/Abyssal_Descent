package com.abyssaldescent.event;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class TypedEventBus {

    private static TypedEventBus instance;
    private final Map<TypedEvent.Type, List<Consumer<TypedEvent>>> listeners =
            new EnumMap<>(TypedEvent.Type.class);

    private TypedEventBus() {}

    public static TypedEventBus getInstance() {
        if (instance == null) instance = new TypedEventBus();
        return instance;
    }

    public void subscribe(TypedEvent.Type type, Consumer<TypedEvent> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public void unsubscribe(TypedEvent.Type type, Consumer<TypedEvent> listener) {
        List<Consumer<TypedEvent>> list = listeners.get(type);
        if (list != null) list.remove(listener);
    }

    public void post(TypedEvent.Type type, Object payload) {
        List<Consumer<TypedEvent>> list = listeners.get(type);
        if (list == null || list.isEmpty()) return;
        TypedEvent event = new TypedEvent(type, payload);
        for (Consumer<TypedEvent> c : list) c.accept(event);
    }

    public void post(TypedEvent.Type type) {
        post(type, null);
    }

    public void clear() {
        listeners.clear();
    }

    static void resetInstance() {
        instance = null;
    }
}
