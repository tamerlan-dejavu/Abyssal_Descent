package com.abyssaldescent.ui.feedback;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.abyssaldescent.entity.PlayerStats;
import com.abyssaldescent.event.TypedEvent;
import com.abyssaldescent.event.TypedEventBus;

import java.util.ArrayList;
import java.util.List;

public class DamageNumberPool {

    private static final int POOL_SIZE = 30;

    public static final class EnemyDamageInfo {
        public final float   worldX;
        public final float   worldY;
        public final int     amount;
        public final boolean isCrit;

        public EnemyDamageInfo(float worldX, float worldY, int amount, boolean isCrit) {
            this.worldX = worldX;
            this.worldY = worldY;
            this.amount = amount;
            this.isCrit = isCrit;
        }
    }

    private final List<DamageNumber> pool = new ArrayList<DamageNumber>(POOL_SIZE);
    private final BitmapFont fontNormal;
    private final BitmapFont fontCrit;

    public DamageNumberPool(TypedEventBus eventBus) {
        for (int i = 0; i < POOL_SIZE; i++) pool.add(new DamageNumber());

        fontNormal = new BitmapFont();
        fontNormal.getData().setScale(1.1f);
        fontCrit = new BitmapFont();
        fontCrit.getData().setScale(1.6f);

        eventBus.subscribe(TypedEvent.Type.PLAYER_DAMAGED, event -> {
            Object payload = event.getPayload();
            if (payload instanceof PlayerStats.DamageInfo) {
                PlayerStats.DamageInfo info = (PlayerStats.DamageInfo) payload;
                spawn(info.worldX, info.worldY, info.amount, DamageNumber.Kind.DAMAGE);
            }
        });

        eventBus.subscribe(TypedEvent.Type.ENEMY_DAMAGED, event -> {
            Object payload = event.getPayload();
            if (payload instanceof EnemyDamageInfo) {
                EnemyDamageInfo info = (EnemyDamageInfo) payload;
                DamageNumber.Kind kind = info.isCrit
                        ? DamageNumber.Kind.CRIT
                        : DamageNumber.Kind.DAMAGE;
                spawn(info.worldX, info.worldY, info.amount, kind);
            }
        });

        eventBus.subscribe(TypedEvent.Type.PLAYER_HEALED, event -> {
            Object payload = event.getPayload();
            if (payload instanceof PlayerStats.DamageInfo) {
                PlayerStats.DamageInfo info = (PlayerStats.DamageInfo) payload;
                spawn(info.worldX, info.worldY, info.amount, DamageNumber.Kind.HEAL);
            }
        });
    }

    public void update(float delta) {
        for (DamageNumber dn : pool) dn.update(delta);
    }

    public void render(SpriteBatch batch) {
        for (DamageNumber dn : pool) dn.render(batch, fontNormal, fontCrit);
    }

    public void dispose() {
        fontNormal.dispose();
        fontCrit.dispose();
    }

    private void spawn(float x, float y, int amount, DamageNumber.Kind kind) {
        for (DamageNumber dn : pool) {
            if (!dn.isActive()) {
                dn.init(x, y + 20f, amount, kind);
                return;
            }
        }
    }
}
