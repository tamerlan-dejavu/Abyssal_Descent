package com.abyssaldescent.ui.hud;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.entity.player.CharacterType;
import com.abyssaldescent.entity.player.PlayerSlot;
import com.abyssaldescent.event.DamageEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.HealthChangedEvent;
import com.abyssaldescent.event.RespawnUsedEvent;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class StatusWindow {

    public static final float W = 500f;
    public static final float H = 500f;

    private static final float PAD        = 14f;
    private static final float BAR_H      = 26f;
    private static final float ICON_SIZE  = 14f;
    private static final float ICON_GAP   = 8f;
    private static final int   MAX_LIVES  = 3;

    private int   currentHp;
    private int   maxHp;
    private int   baseDamage;
    private int   respawnsRemaining = MAX_LIVES;
    private int   respawnsMax       = MAX_LIVES;
    private float damageFade        = 0f;

    private final BitmapFont fontTitle;
    private final BitmapFont fontBody;

    private final EventListener<HealthChangedEvent> hpListener     = this::onHealthChanged;
    private final EventListener<DamageEvent>        damageListener = this::onDamage;
    private final EventListener<RespawnUsedEvent>   respawnListener = this::onRespawnUsed;

    public StatusWindow(BitmapFont fontTitle, BitmapFont fontBody) {
        this.fontTitle = fontTitle;
        this.fontBody  = fontBody;
        syncFromGameState();
        EventBus.getInstance().subscribe(HealthChangedEvent.class, hpListener);
        EventBus.getInstance().subscribe(DamageEvent.class, damageListener);
        EventBus.getInstance().subscribe(RespawnUsedEvent.class, respawnListener);
    }

    private void syncFromGameState() {
        PlayerSlot slot = GameStateManager.getInstance().getKarinSlot();
        currentHp  = slot.getCurrentHp();
        maxHp      = slot.getMaxHp();
        baseDamage = CharacterType.KARIN.getBaseDamage();
    }

    private void onHealthChanged(HealthChangedEvent e) {
        currentHp = e.getCurrentHp();
        maxHp     = e.getMaxHp();
    }

    private void onDamage(DamageEvent e) {
        if ("PLAYER".equals(e.getTargetId())) damageFade = 1f;
    }

    private void onRespawnUsed(RespawnUsedEvent e) {
        respawnsRemaining = e.getRemainingRespawns();
        respawnsMax       = e.getMaxRespawns();
    }

    public void update(float delta) {
        if (damageFade > 0f) damageFade = Math.max(0f, damageFade - delta * 2f);
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        float borderAlpha = 0.3f + damageFade * 0.6f;

        shapes.setColor(0.04f, 0.04f, 0.1f, 0.88f);
        shapes.rect(x, y, W, H);

        shapes.setColor(damageFade > 0f ? 0.9f : 0.15f,
                damageFade > 0f ? 0.1f : 0.15f,
                damageFade > 0f ? 0.1f : 0.35f,
                borderAlpha);
        float bw = 2f;
        shapes.rect(x, y, W, bw);
        shapes.rect(x, y + H - bw, W, bw);
        shapes.rect(x, y, bw, H);
        shapes.rect(x + W - bw, y, bw, H);

        float barX  = x + PAD;
        float barY  = y + H - 100f;
        float barW  = W - PAD * 2f;
        float pct   = maxHp > 0 ? (float) currentHp / maxHp : 0f;

        shapes.setColor(0.12f, 0.06f, 0.06f, 1f);
        shapes.rect(barX, barY, barW, BAR_H);
        shapes.setColor(1f, 0.267f, 0.267f, 1f);
        shapes.rect(barX, barY, barW * pct, BAR_H);

        float sepY = y + H - 155f;
        shapes.setColor(0.18f, 0.18f, 0.4f, 0.6f);
        shapes.rect(x + PAD, sepY, W - PAD * 2f, 1f);

        float sepY2 = y + H - 230f;
        shapes.setColor(0.18f, 0.18f, 0.4f, 0.6f);
        shapes.rect(x + PAD, sepY2, W - PAD * 2f, 1f);

        float iconX = x + PAD + 90f;
        float iconY = y + H - 285f + (20f - ICON_SIZE) * 0.5f;
        for (int i = 0; i < respawnsMax; i++) {
            if (i < respawnsRemaining) {
                shapes.setColor(1f, 0.267f, 0.267f, 1f);
            } else {
                shapes.setColor(0.22f, 0.05f, 0.05f, 1f);
            }
            shapes.rect(iconX + i * (ICON_SIZE + ICON_GAP), iconY, ICON_SIZE, ICON_SIZE);
        }
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        fontTitle.getData().setScale(2.0f);
        fontTitle.setColor(0.55f, 0.55f, 1f, 1f);
        fontTitle.draw(batch, "STATUS", x + PAD, y + H - 20f);

        fontBody.getData().setScale(1.6f);
        fontBody.setColor(Color.WHITE);
        fontBody.draw(batch, "HP  " + currentHp + " / " + maxHp,
                x + PAD, y + H - 68f);

        fontBody.getData().setScale(1.4f);
        fontBody.setColor(0.85f, 0.85f, 0.85f, 1f);
        fontBody.draw(batch, "Base DMG:  " + baseDamage,
                x + PAD, y + H - 170f);

        fontBody.setColor(1f, 0.4f, 0.4f, 1f);
        fontBody.draw(batch, "LIVES:",
                x + PAD, y + H - 258f);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(HealthChangedEvent.class, hpListener);
        EventBus.getInstance().unsubscribe(DamageEvent.class, damageListener);
        EventBus.getInstance().unsubscribe(RespawnUsedEvent.class, respawnListener);
    }
}
