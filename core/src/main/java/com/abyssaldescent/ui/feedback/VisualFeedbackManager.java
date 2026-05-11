package com.abyssaldescent.ui.feedback;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.abyssaldescent.event.TypedEventBus;

public class VisualFeedbackManager {

    private final DamageNumberPool damageNumbers;
    private final HitFlash         hitFlash;
    private final Knockback        knockback;
    private final ScreenFlash      screenFlash;
    private final AttackSlash      attackSlash;

    public VisualFeedbackManager(TypedEventBus eventBus, ShapeRenderer shapeRenderer) {
        damageNumbers = new DamageNumberPool(eventBus);
        hitFlash      = new HitFlash(eventBus);
        knockback     = new Knockback(eventBus);
        screenFlash   = new ScreenFlash(eventBus);
        attackSlash   = new AttackSlash(eventBus, shapeRenderer);
    }

    public void update(float delta) {
        damageNumbers.update(delta);
        hitFlash.update(delta);
        knockback.update(delta);
        screenFlash.update(delta);
        attackSlash.update(delta);
    }

    public void renderLines() {
        attackSlash.render();
    }

    public void renderBatch(SpriteBatch batch) {
        damageNumbers.render(batch);
    }

    public void renderScreenFlash() {
        screenFlash.render();
    }

    public void triggerLevelTransition() {
        screenFlash.trigger(ScreenFlash.FlashType.BLACK, 0.8f);
    }

    public HitFlash  getHitFlash()  { return hitFlash; }
    public Knockback getKnockback() { return knockback; }

    public void resize(int w, int h) {
        screenFlash.resize(w, h);
    }

    public void dispose() {
        damageNumbers.dispose();
        screenFlash.dispose();
    }
}
