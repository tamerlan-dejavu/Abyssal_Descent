package com.abyssaldescent.combat;

import com.abyssaldescent.entity.CharacterType;
import com.abyssaldescent.entity.PlayerContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeleeStrategyTest {

    private MeleeStrategy strategy;
    private static final int BASE_DAMAGE = CharacterType.KARIN.getBaseDamage(); // 15

    @BeforeEach
    void setUp() {
        strategy = new MeleeStrategy();
    }

    @Test
    void firstHit_dealsBaseDamage() {
        int damage = strategy.calculateDamage(BASE_DAMAGE);
        assertEquals(BASE_DAMAGE, damage);
    }

    @Test
    void comboHits_dealIncreasedDamage() {
        strategy.calculateDamage(BASE_DAMAGE); // hit 1
        int hit2 = strategy.calculateDamage(BASE_DAMAGE); // hit 2 (within combo window)

        assertTrue(hit2 > BASE_DAMAGE, "Combo hits should deal more damage");
    }

    @Test
    void comboResets_afterWindow() {
        strategy.calculateDamage(BASE_DAMAGE);
        strategy.update(1.0f); // well past the 0.6s combo window

        int damage = strategy.calculateDamage(BASE_DAMAGE);
        assertEquals(BASE_DAMAGE, damage, "Combo should reset after window expires");
    }

    @Test
    void comboCount_tracksCorrectly() {
        assertEquals(0, strategy.getComboCount());

        strategy.calculateDamage(BASE_DAMAGE);
        assertEquals(1, strategy.getComboCount());

        strategy.calculateDamage(BASE_DAMAGE);
        assertEquals(2, strategy.getComboCount());

        strategy.calculateDamage(BASE_DAMAGE);
        assertEquals(3, strategy.getComboCount());
    }

    @Test
    void comboCount_capsAtMax() {
        strategy.calculateDamage(BASE_DAMAGE); // 1
        strategy.calculateDamage(BASE_DAMAGE); // 2
        strategy.calculateDamage(BASE_DAMAGE); // 3
        int hit4 = strategy.calculateDamage(BASE_DAMAGE); // should reset to 1

        assertEquals(1, strategy.getComboCount());
        assertEquals(BASE_DAMAGE, hit4);
    }

    @Test
    void range_matchesAttackRange() {
        assertEquals(PlayerContext.ATTACK_RANGE, strategy.getRange());
    }

    @Test
    void name_isMelee() {
        assertEquals("Melee", strategy.getName());
    }
}
