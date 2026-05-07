package com.abyssaldescent.ui.hud;

import com.abyssaldescent.combat.chips.BerserkChip;
import com.abyssaldescent.combat.chips.ChipDecorator;
import com.abyssaldescent.combat.chips.ChipInventoryObserver;
import com.abyssaldescent.combat.chips.ChipType;
import com.abyssaldescent.combat.chips.DamageChip;
import com.abyssaldescent.combat.chips.DashChip;
import com.abyssaldescent.combat.chips.DoubleJumpChip;
import com.abyssaldescent.combat.chips.FireAuraChip;
import com.abyssaldescent.combat.chips.HealthChip;
import com.abyssaldescent.combat.chips.IceArrowChip;
import com.abyssaldescent.combat.chips.ShieldChip;
import com.abyssaldescent.combat.chips.SpeedChip;
import com.abyssaldescent.combat.chips.VampireChip;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Renders the 4 chip slots at the bottom-centre of the screen (Observer pattern).
 * Active chips (on-demand abilities) get a yellow border; passive chips get grey.
 */
public final class HudChipSlotWidget implements ChipInventoryObserver {

    private static final int   SLOT_COUNT = 4;
    private static final float SLOT_SIZE  = 60f;
    private static final float SLOT_GAP   = 10f;
    private static final float BORDER     = 3f;

    private final ChipDecorator[] slots = new ChipDecorator[SLOT_COUNT];
    private final BitmapFont font;

    public HudChipSlotWidget(BitmapFont font) {
        this.font = font;
    }

    @Override
    public void onChipEquipped(int slot, ChipDecorator chip) {
        if (slot >= 0 && slot < SLOT_COUNT) slots[slot] = chip;
    }

    @Override
    public void onChipRemoved(int slot) {
        if (slot >= 0 && slot < SLOT_COUNT) slots[slot] = null;
    }

    /** Activate an on-demand chip at the given 0-based slot index. */
    public void activateSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT || slots[slot] == null) return;
        if (slots[slot] instanceof BerserkChip) {
            ((BerserkChip) slots[slot]).activate();
        }
    }

    public void renderShapes(ShapeRenderer shapes, float screenW, float screenH) {
        float totalW = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
        float startX = screenW / 2f - totalW / 2f;
        float y = 20f;

        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = startX + i * (SLOT_SIZE + SLOT_GAP);
            ChipDecorator chip = slots[i];

            // slot background
            shapes.setColor(0.1f, 0.1f, 0.1f, 0.85f);
            shapes.rect(x, y, SLOT_SIZE, SLOT_SIZE);

            // border colour: yellow = active chip, grey = passive, dark = empty
            if (chip != null) {
                if (isActiveChip(chip)) {
                    shapes.setColor(1f, 1f, 0f, 1f);           // #FFFF00
                } else {
                    shapes.setColor(0.502f, 0.502f, 0.502f, 1f); // #808080
                }
            } else {
                shapes.setColor(0.25f, 0.25f, 0.25f, 1f);
            }
            shapes.rect(x,                      y + SLOT_SIZE - BORDER, SLOT_SIZE, BORDER);
            shapes.rect(x,                      y,                       SLOT_SIZE, BORDER);
            shapes.rect(x,                      y,                       BORDER,    SLOT_SIZE);
            shapes.rect(x + SLOT_SIZE - BORDER, y,                       BORDER,    SLOT_SIZE);
        }
    }

    public void renderText(SpriteBatch batch, float screenW, float screenH) {
        font.getData().setScale(0.9f);
        float totalW = SLOT_COUNT * SLOT_SIZE + (SLOT_COUNT - 1) * SLOT_GAP;
        float startX = screenW / 2f - totalW / 2f;
        float y = 20f;

        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = startX + i * (SLOT_SIZE + SLOT_GAP);
            ChipDecorator chip = slots[i];

            // slot number key hint
            font.setColor(Color.LIGHT_GRAY);
            font.draw(batch, String.valueOf(i + 1), x + 4f, y + SLOT_SIZE - 4f);

            if (chip != null) {
                ChipType type = typeOf(chip);
                String label = type != null
                        ? type.name().substring(0, Math.min(5, type.name().length()))
                        : "?";
                font.setColor(isActiveChip(chip) ? Color.YELLOW : Color.WHITE);
                font.draw(batch, label, x + 4f, y + SLOT_SIZE / 2f + 8f);
            } else {
                font.setColor(0.4f, 0.4f, 0.4f, 1f);
                font.draw(batch, "---", x + 8f, y + SLOT_SIZE / 2f + 8f);
            }
        }
    }

    public void dispose() { /* font managed by HudRenderer */ }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static boolean isActiveChip(ChipDecorator chip) {
        return chip instanceof BerserkChip
            || chip instanceof DashChip
            || chip instanceof DoubleJumpChip
            || chip instanceof ShieldChip;
    }

    private static ChipType typeOf(ChipDecorator chip) {
        if (chip instanceof DamageChip)     return ChipType.DAMAGE;
        if (chip instanceof VampireChip)    return ChipType.VAMPIRE;
        if (chip instanceof ShieldChip)     return ChipType.SHIELD;
        if (chip instanceof BerserkChip)    return ChipType.BERSERK;
        if (chip instanceof DashChip)       return ChipType.DASH;
        if (chip instanceof DoubleJumpChip) return ChipType.DOUBLE_JUMP;
        if (chip instanceof FireAuraChip)   return ChipType.FIRE_AURA;
        if (chip instanceof HealthChip)     return ChipType.HEALTH;
        if (chip instanceof IceArrowChip)   return ChipType.ICE_ARROW;
        if (chip instanceof SpeedChip)      return ChipType.SPEED;
        return null;
    }
}
