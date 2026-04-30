package com.abyssaldescent.world;

import java.util.HashSet;
import java.util.Set;

public final class KeyInventory {
    private final Set<Integer> keys = new HashSet<>();

    public void addKey(int keyId) {
        keys.add(keyId);
    }

    public boolean hasKey(int keyId) {
        return keys.contains(keyId);
    }

    public void removeKey(int keyId) {
        keys.remove(keyId);
    }

    public int getKeyCount() {
        return keys.size();
    }

    public Set<Integer> getAllKeys() {
        return new HashSet<>(keys);
    }

    public void clear() {
        keys.clear();
    }
}
