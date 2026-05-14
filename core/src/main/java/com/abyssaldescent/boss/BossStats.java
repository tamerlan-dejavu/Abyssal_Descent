package com.abyssaldescent.boss;

import com.abyssaldescent.config.DifficultySettings;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.BossDefeatedEvent;
import com.abyssaldescent.event.BossHitEvent;
import com.abyssaldescent.event.BossPhaseChangedEvent;

/**
 * Здоровье и фазы босса Малтарион-Эхо.
 *
 * GDD:
 *   Базовый HP = 500 (масштабируется с сложностью через GameConfig)
 *   Фаза 1: HP > 50% — дистанционные атаки
 *   Фаза 2: HP ≤ 50% — вызов Разломных Рыцарей + гравитационные волны
 *
 * Паттерн: Observer — публикует события в EventBus при смене фазы и смерти.
 */
public class BossStats {

    private static final int BASE_HP           = 500;
    private static final float PHASE2_THRESHOLD = 0.50f;   // 50% HP

    private final int   maxHp;
    private int         currentHp;
    private BossPhase   phase;
    private boolean     active = false;

    // flash-анимация при ударе
    private float hitFlashTimer = 0f;
    private static final float HIT_FLASH_DURATION = 0.15f;

    private final EventBus eventBus;
    private DifficultySettings difficulty;

    // ─────────────────────────────────────────────────────────────────────────

   public BossStats(EventBus eventBus, DifficultySettings difficulty) {
    this.eventBus = eventBus;
    this.difficulty = difficulty;

    this.maxHp = (int)(BASE_HP * difficulty.hpMultiplier);

    this.currentHp = maxHp;
    this.phase = BossPhase.PHASE_1;
}

    // ── Активация ─────────────────────────────────────────────────────────────

    /** Вызвать когда игрок входит в комнату босса. */
    public void activate() {
        active    = true;
        currentHp = maxHp;
        phase     = BossPhase.PHASE_1;
        eventBus.post(new BossPhaseChangedEvent(BossPhase.PHASE_1, maxHp, currentHp));
    }

    // ── Урон ──────────────────────────────────────────────────────────────────

    public void takeDamage(int amount) {
        if (!active || phase == BossPhase.DEAD) return;

        currentHp = Math.max(0, currentHp - amount);
        hitFlashTimer = HIT_FLASH_DURATION;

        eventBus.post(new BossHitEvent(amount, currentHp, maxHp));

        // Проверка смены фазы
        checkPhaseTransition();

        if (currentHp == 0) handleDeath();
    }

    // ── Обновление ────────────────────────────────────────────────────────────

    public void update(float delta) {
        if (hitFlashTimer > 0) hitFlashTimer -= delta;
    }

    // ── Геттеры ───────────────────────────────────────────────────────────────

    public int       getCurrentHp()  { return currentHp; }
    public int       getMaxHp()      { return maxHp; }
    public BossPhase getPhase()      { return phase; }
    public boolean   isActive()      { return active; }
    public boolean   isFlashing()    { return hitFlashTimer > 0; }
    public float     getHpRatio()    { return maxHp > 0 ? (float) currentHp / maxHp : 0f; }

    // ── Private ───────────────────────────────────────────────────────────────

    private void checkPhaseTransition() {
        if (phase == BossPhase.PHASE_1 && getHpRatio() <= PHASE2_THRESHOLD) {
            phase = BossPhase.PHASE_2;
            eventBus.post(new BossPhaseChangedEvent(BossPhase.PHASE_2, maxHp, currentHp));
        }
    }

    private void handleDeath() {
        phase  = BossPhase.DEAD;
        active = false;
        eventBus.post(new BossDefeatedEvent());
    }
}