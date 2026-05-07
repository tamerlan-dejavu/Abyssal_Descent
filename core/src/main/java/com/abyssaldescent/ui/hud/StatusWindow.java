package com.abyssaldescent.ui.hud;

import com.abyssaldescent.GameStateManager;
import com.abyssaldescent.entity.player.CharacterType;
import com.abyssaldescent.entity.player.PlayerSlot;
import com.abyssaldescent.event.DamageEvent;
import com.abyssaldescent.event.EventBus;
import com.abyssaldescent.event.EventListener;
import com.abyssaldescent.event.HealthChangedEvent;
import com.abyssaldescent.event.RespawnUsedEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public final class StatusWindow {

    public static final float W = 300f;
    public static final float H = 300f;

    private static final String ASSET_PATH = "ui/backgrounds/stats.png";

    private static final int MAX_LIVES = 3;

    private int   currentHp;
    private int   maxHp;
    private int   baseDamage;
    private int   respawnsRemaining = MAX_LIVES;
    private int   respawnsMax       = MAX_LIVES;
    private float damageFade        = 0f;

    private final Texture    bgTexture;
    private final BitmapFont font;

    private final EventListener<HealthChangedEvent> hpListener      = this::onHealthChanged;
    private final EventListener<DamageEvent>        damageListener  = this::onDamage;
    private final EventListener<RespawnUsedEvent>   respawnListener = this::onRespawnUsed;

    public StatusWindow(BitmapFont fontTitle, BitmapFont fontBody) {
        this.font = fontBody;
        bgTexture = tryLoad(ASSET_PATH);
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

    public void renderBackground(SpriteBatch batch, float x, float y) {
        if (bgTexture == null) return;
        batch.setColor(1f, 1f, 1f, 0.92f);
        batch.draw(bgTexture, x, y, W, H);
        batch.setColor(Color.WHITE);
    }

    public void renderShapes(ShapeRenderer shapes, float x, float y) {
        if (bgTexture == null) {
            float borderAlpha = 0.3f + damageFade * 0.6f;
            shapes.setColor(0.04f, 0.04f, 0.1f, 0.88f);
            shapes.rect(x, y, W, H);
            float bw = 2f;
            shapes.setColor(damageFade > 0f ? 0.9f : 0.15f,
                    damageFade > 0f ? 0.1f : 0.15f,
                    damageFade > 0f ? 0.1f : 0.35f,
                    borderAlpha);
            shapes.rect(x, y, W, bw);
            shapes.rect(x, y + H - bw, W, bw);
            shapes.rect(x, y, bw, H);
            shapes.rect(x + W - bw, y, bw, H);
        } else if (damageFade > 0f) {
            float bw = 3f;
            shapes.setColor(0.9f, 0.1f, 0.1f, damageFade * 0.8f);
            shapes.rect(x, y, W, bw);
            shapes.rect(x, y + H - bw, W, bw);
            shapes.rect(x, y, bw, H);
            shapes.rect(x + W - bw, y, bw, H);
        }
    }

    public void renderText(SpriteBatch batch, float x, float y) {
        float cx   = x + W * 0.5f - 19f;
        float yTop = y + H * 0.82f - 18f;
        float yMid = y + H * 0.55f - 3f;
        float yBot = y + H * 0.28f + 17f;

        font.getData().setScale(1.6f);

        font.setColor(1f, 0.35f, 0.35f, 1f);
        font.draw(batch, currentHp + " / " + maxHp, cx, yTop);

        font.setColor(1f, 0.85f, 0.3f, 1f);
        font.draw(batch, String.valueOf(baseDamage), cx, yMid);

        font.setColor(0.2f, 0.9f, 0.3f, 1f);
        font.draw(batch, respawnsRemaining + " / " + respawnsMax, cx, yBot);
    }

    public void dispose() {
        EventBus.getInstance().unsubscribe(HealthChangedEvent.class, hpListener);
        EventBus.getInstance().unsubscribe(DamageEvent.class, damageListener);
        EventBus.getInstance().unsubscribe(RespawnUsedEvent.class, respawnListener);
        if (bgTexture != null) bgTexture.dispose();
    }

    private static Texture tryLoad(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                Texture t = new Texture(Gdx.files.internal(path));
                t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return t;
            }
        } catch (Exception ignored) {}
        return null;
    }
}
