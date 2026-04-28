package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;
import com.abyssaldescent.combat.strategy.MeleeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChipFactoryTest {

    private ChipFactory factory;
    private CombatStrategy baseStrategy;

    @BeforeEach
    void setUp() {
        factory = new ChipFactory();
        baseStrategy = new MeleeStrategy();
    }

    @Test
    void factory_creates_damage_chip() {
        assertInstanceOf(DamageChip.class, factory.create(ChipType.DAMAGE, baseStrategy));
    }

    @Test
    void factory_creates_vampire_chip() {
        assertInstanceOf(VampireChip.class, factory.create(ChipType.VAMPIRE, baseStrategy));
    }

    @Test
    void factory_creates_shield_chip() {
        assertInstanceOf(ShieldChip.class, factory.create(ChipType.SHIELD, baseStrategy));
    }

    @Test
    void factory_creates_berserk_chip() {
        assertInstanceOf(BerserkChip.class, factory.create(ChipType.BERSERK, baseStrategy));
    }

    @Test
    void factory_creates_dash_chip() {
        assertInstanceOf(DashChip.class, factory.create(ChipType.DASH, baseStrategy));
    }

    @Test
    void factory_creates_double_jump_chip() {
        assertInstanceOf(DoubleJumpChip.class, factory.create(ChipType.DOUBLE_JUMP, baseStrategy));
    }

    @Test
    void factory_creates_fire_aura_chip() {
        assertInstanceOf(FireAuraChip.class, factory.create(ChipType.FIRE_AURA, baseStrategy));
    }

    @Test
    void factory_creates_health_chip() {
        assertInstanceOf(HealthChip.class, factory.create(ChipType.HEALTH, baseStrategy));
    }

    @Test
    void factory_creates_ice_arrow_chip() {
        assertInstanceOf(IceArrowChip.class, factory.create(ChipType.ICE_ARROW, baseStrategy));
    }

    @Test
    void factory_creates_speed_chip() {
        assertInstanceOf(SpeedChip.class, factory.create(ChipType.SPEED, baseStrategy));
    }

    @Test
    void created_chip_delegates_damage_to_wrapped_strategy() {
        ChipDecorator chip = factory.create(ChipType.DAMAGE, baseStrategy);
        int baseDamage = 10;
        int chipDamage = chip.calculateDamage(baseDamage);
        assertTrue(chipDamage > baseDamage, "Damage chip should increase damage");
    }

    @Test
    void factory_throws_on_null_chip_type() {
        assertThrows(IllegalArgumentException.class, () -> factory.create(null, baseStrategy));
    }

    @Test
    void factory_throws_on_null_wrapped() {
        assertThrows(IllegalArgumentException.class, () -> factory.create(ChipType.DAMAGE, null));
    }
}
