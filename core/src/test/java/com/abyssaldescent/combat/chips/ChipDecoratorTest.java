package com.abyssaldescent.combat.chips;

import com.abyssaldescent.combat.strategy.CombatStrategy;
import com.abyssaldescent.combat.strategy.MeleeStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChipDecoratorTest {

    private CombatStrategy baseStrategy;

    @BeforeEach
    void setUp() {
        baseStrategy = new MeleeStrategy();
    }

    @Test
    void damage_chip_adds_bonus() {
        DamageChip chip = new DamageChip(baseStrategy);
        int baseDamage = baseStrategy.calculateDamage(10);
        int chipDamage = chip.calculateDamage(10);
        assertEquals(baseDamage + DamageChip.BONUS_DAMAGE, chipDamage);
    }

    @Test
    void damage_chip_name_appends_fire() {
        DamageChip chip = new DamageChip(baseStrategy);
        String name = chip.getName();
        assertTrue(name.contains("+Fire"));
    }

    @Test
    void berserk_chip_doubles_damage_when_active() {
        BerserkChip chip = new BerserkChip(baseStrategy);
        chip.activate();

        int baseDamage = baseStrategy.calculateDamage(10);
        int chipDamage = chip.calculateDamage(10);

        assertEquals(Math.round(baseDamage * BerserkChip.DAMAGE_MULTIPLIER), chipDamage);
    }

    @Test
    void berserk_chip_no_bonus_when_inactive() {
        BerserkChip chip = new BerserkChip(baseStrategy);

        int baseDamage = baseStrategy.calculateDamage(10);
        int chipDamage = chip.calculateDamage(10);

        assertEquals(baseDamage, chipDamage);
    }

    @Test
    void berserk_chip_expires_after_duration() {
        BerserkChip chip = new BerserkChip(baseStrategy);
        chip.activate();
        assertTrue(chip.isActive());

        chip.update(BerserkChip.DURATION + 0.1f);
        assertFalse(chip.isActive());
    }

    @Test
    void vampire_chip_heal_per_hit_constant() {
        VampireChip chip = new VampireChip(baseStrategy);
        assertEquals(VampireChip.HEAL_PER_HIT, chip.getHealPerHit());
    }

    @Test
    void vampire_chip_name_appends_vampire() {
        VampireChip chip = new VampireChip(baseStrategy);
        String name = chip.getName();
        assertTrue(name.contains("+Vampire"));
    }

    @Test
    void shield_chip_consume_absorbs_then_false() {
        ShieldChip chip = new ShieldChip(baseStrategy);
        chip.activate();
        assertTrue(chip.isShieldActive());

        assertTrue(chip.consumeShield());
        assertFalse(chip.isShieldActive());
        assertFalse(chip.consumeShield());
    }

    @Test
    void shield_chip_cooldown_gated() {
        ShieldChip chip = new ShieldChip(baseStrategy);
        chip.activate();
        assertTrue(chip.isShieldActive());

        chip.update(0.1f);
        chip.activate();
        assertTrue(chip.isShieldActive(), "Second activate should not fire while active");

        chip.consumeShield();
        chip.activate();
        assertFalse(chip.isShieldActive(), "Second activate should fail while cooldown active");

        chip.update(ShieldChip.COOLDOWN + 0.1f);
        chip.activate();
        assertTrue(chip.isShieldActive(), "Activate should work after cooldown");
    }

    @Test
    void decorator_chain_stacks_correctly() {
        DamageChip dmgChip = new DamageChip(baseStrategy);
        VampireChip vmpChip = new VampireChip(dmgChip);

        int baseDamage = baseStrategy.calculateDamage(10);
        int chainedDamage = vmpChip.calculateDamage(10);

        assertEquals(baseDamage + DamageChip.BONUS_DAMAGE, chainedDamage);
        assertTrue(vmpChip.getName().contains("Vampire"));
        assertTrue(vmpChip.getName().contains("Fire"));
    }

    @Test
    void double_jump_chip_increases_range() {
        DoubleJumpChip chip = new DoubleJumpChip(baseStrategy);
        float baseRange = baseStrategy.getRange();
        float chipRange = chip.getRange();
        assertEquals(baseRange + DoubleJumpChip.RANGE_BONUS, chipRange);
    }

    @Test
    void speed_chip_name_appends_slow() {
        SpeedChip chip = new SpeedChip(baseStrategy);
        String name = chip.getName();
        assertTrue(name.contains("+Slow"));
    }
}
