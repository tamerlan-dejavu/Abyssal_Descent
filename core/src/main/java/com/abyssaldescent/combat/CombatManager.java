package com.abyssaldescent.combat;

import com.abyssaldescent.combat.chips.ChipDecorator;
import com.abyssaldescent.combat.chips.DoubleJumpChip;
import com.abyssaldescent.combat.strategy.CombatStrategy;
import com.abyssaldescent.entity.enemy.Enemy;
import com.abyssaldescent.entity.player.CharacterType;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.PlayerAttackEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CombatManager {
    public static final float ATTACK_HALF_CONE_COS = 0.0f;
    private final List<Enemy> enemies = new ArrayList<>();
    private final EventBus eventBus;
    private final EventListener<PlayerAttackEvent> attackListener = this::onPlayerAttack;
    private CombatStrategy playerStrategy;
    private int playerBaseDamage = CharacterType.KARIN.getBaseDamage();
    private DamageCallback damageCallback;

    public CombatManager(EventBus eventBus, CombatStrategy playerStrategy) {
        this.eventBus = eventBus;
        this.playerStrategy = playerStrategy;
        this.eventBus.subscribe(PlayerAttackEvent.class, attackListener);
    }

    public void setDamageCallback(DamageCallback callback) {
        this.damageCallback = callback;
    }

    public void setPlayerStrategy(CombatStrategy strategy) { this.playerStrategy = strategy; }

    public void setPlayerBaseDamage(int baseDamage) { this.playerBaseDamage = baseDamage; }

    public void addEnemy(Enemy enemy) { enemies.add(enemy); }

    public void clearEnemies() { enemies.clear(); }

    public List<Enemy> getEnemies() { return enemies; }

    public void update(float deltaTime) {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            e.update(deltaTime);
            if (e.isDead() && e.getContext().getStateTimer() > 1.0f) {
                it.remove();
            }
        }
    }

    void onPlayerAttack(PlayerAttackEvent event) {
        float ox = event.getOriginX();
        float oy = event.getOriginY();
        float dx = event.getDirectionX();
        float dy = event.getDirectionY();
        float range = Math.max(event.getRange(), playerStrategy.getRange());

        boolean pierce = playerStrategy instanceof DoubleJumpChip || unwrapContainsPierce(playerStrategy);

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            float ex = enemy.getX() - ox;
            float ey = enemy.getY() - oy;
            float distSq = ex * ex + ey * ey;
            if (distSq > range * range) continue;

            float dist = (float) Math.sqrt(distSq);
            if (dist < 0.0001f) {
                applyDamage(enemy);
                if (!pierce) return;
                continue;
            }
            float dot = (ex / dist) * dx + (ey / dist) * dy;
            if (dot >= ATTACK_HALF_CONE_COS) {
                applyDamage(enemy);
                if (!pierce) return;
            }
        }
    }

    private void applyDamage(Enemy enemy) {
        int dmg = playerStrategy.calculateDamage(playerBaseDamage);
        enemy.takeDamage(dmg);
        if (damageCallback != null) {
            damageCallback.onDamage(enemy.getX(), enemy.getY());
        }
    }

    private static boolean unwrapContainsPierce(CombatStrategy strategy) {
        CombatStrategy current = strategy;
        while (current instanceof ChipDecorator) {
            if (current instanceof DoubleJumpChip) return true;
            current = ((ChipDecorator) current).unwrap();
        }
        return false;
    }

    public void dispose() {
        eventBus.unsubscribe(PlayerAttackEvent.class, attackListener);
    }
}
