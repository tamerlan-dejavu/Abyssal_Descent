package com.abyssaldescent.combat.chips;

/**
 * Типы чипов для улучшения персонажа.
 * Содержит как пассивные усиления, так и активные способности.
 */
public enum ChipType {
    // --- ПАССИВНЫЕ ---
    STRENGTH,      // +25% урон
    SPEED,         // +30% скорость
    HEALTH,        // +25 макс HP, восстанавливает 20 HP
    DOUBLE_JUMP,   // второй прыжок в воздухе
    DASH,          // откат рывка 0.5 с вместо 1 с
    VAMPIRE,       // каждый удар +3 HP

    // --- АКТИВНЫЕ ---
    FIRE_AURA,     // AoE 30 урона, 3 с (откат 15 с)
    SHIELD,        // блок следующего удара, 5 с (откат 20 с)
    BERSERK,       // +100% урон, -30% HP на 8 с (откат 30 с)
    ICE_ARROW;     // снаряд 25 урона + замедление 50% на 3 с

    // Метод для быстрой проверки, является ли чип активным
    public boolean isActive() {
        return this == FIRE_AURA || this == SHIELD || this == BERSERK || this == ICE_ARROW;
    }
}

