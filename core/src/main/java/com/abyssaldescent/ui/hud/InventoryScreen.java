package com.abyssaldescent.ui.hud;

import com.abyssaldescent.combat.chips.BerserkChip;
import com.abyssaldescent.combat.chips.ChipDecorator;
import com.abyssaldescent.combat.chips.ChipInventory;
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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Full-screen inventory overlay opened with TAB, closed with TAB or ESC.
 * Displays the four chip slots in a 2×2 grid and shows a description panel
 * for the currently selected slot (click to select).
 */
public final class InventoryScreen {

    private static final int   SLOT_COLS  = 2;
    private static final int   SLOT_ROWS  = 2;
    private static final float SLOT_SIZE  = 150f;
    private static final float SLOT_GAP   = 24f;
    private static final float PANEL_W    = 380f;
    private static final float BORDER     = 3f;

    private boolean open         = false;
    private int     selectedSlot = -1;

    private float screenW;
    private float screenH;

    private final ChipInventory chipInventory;
    private final BitmapFont    fontMedium;
    private final BitmapFont    fontSmall;

    public InventoryScreen(ChipInventory chipInventory,
                           BitmapFont fontMedium,
                           BitmapFont fontSmall) {
        this.chipInventory = chipInventory;
        this.fontMedium    = fontMedium;
        this.fontSmall     = fontSmall;
        this.screenW       = Gdx.graphics.getWidth();
        this.screenH       = Gdx.graphics.getHeight();
    }

    public boolean isOpen()  { return open; }

    public void toggle() {
        open = !open;
        if (!open) selectedSlot = -1;
    }

    public void close() {
        open         = false;
        selectedSlot = -1;
    }

    public void resize(float w, float h) {
        screenW = w;
        screenH = h;
    }

    /**
     * Converts a raw screen-space click (y grows downwards) to world-space and
     * checks whether it lands inside any chip slot.
     */
    public void handleClick(float rawX, float rawY) {
        if (!open) return;
        float wx = rawX;
        float wy = screenH - rawY;   // flip Y: libGDX camera origin is bottom-left

        float gridW  = SLOT_COLS * SLOT_SIZE + (SLOT_COLS - 1) * SLOT_GAP;
        float gridH  = SLOT_ROWS * SLOT_SIZE + (SLOT_ROWS - 1) * SLOT_GAP;
        float totalW = gridW + SLOT_GAP * 2f + PANEL_W;
        float gridX  = screenW / 2f - totalW / 2f;
        float gridY  = screenH / 2f - gridH / 2f;

        for (int row = 0; row < SLOT_ROWS; row++) {
            for (int col = 0; col < SLOT_COLS; col++) {
                int   idx = row * SLOT_COLS + col;
                float sx  = gridX + col * (SLOT_SIZE + SLOT_GAP);
                float sy  = gridY + (SLOT_ROWS - 1 - row) * (SLOT_SIZE + SLOT_GAP);
                if (wx >= sx && wx <= sx + SLOT_SIZE && wy >= sy && wy <= sy + SLOT_SIZE) {
                    selectedSlot = (selectedSlot == idx) ? -1 : idx;  // toggle
                    return;
                }
            }
        }
    }

    /** Renders the full-screen overlay. Shapes and batch must both be in ended state. */
    public void render(SpriteBatch batch, ShapeRenderer shapes) {
        if (!open) return;

        float gridW  = SLOT_COLS * SLOT_SIZE + (SLOT_COLS - 1) * SLOT_GAP;
        float gridH  = SLOT_ROWS * SLOT_SIZE + (SLOT_ROWS - 1) * SLOT_GAP;
        float totalW = gridW + SLOT_GAP * 2f + PANEL_W;
        float gridX  = screenW / 2f - totalW / 2f;
        float gridY  = screenH / 2f - gridH / 2f;
        float panelX = gridX + gridW + SLOT_GAP * 2f;

        // ── shape pass ───────────────────────────────────────────────────────
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        // dim overlay
        shapes.setColor(0f, 0f, 0f, 0.75f);
        shapes.rect(0, 0, screenW, screenH);

        // slot backgrounds + borders
        for (int row = 0; row < SLOT_ROWS; row++) {
            for (int col = 0; col < SLOT_COLS; col++) {
                int   idx  = row * SLOT_COLS + col;
                float sx   = gridX + col  * (SLOT_SIZE + SLOT_GAP);
                float sy   = gridY + (SLOT_ROWS - 1 - row) * (SLOT_SIZE + SLOT_GAP);
                ChipDecorator chip = chipInventory.getSlot(idx);

                shapes.setColor(0.12f, 0.12f, 0.18f, 1f);
                shapes.rect(sx, sy, SLOT_SIZE, SLOT_SIZE);

                if (idx == selectedSlot) {
                    shapes.setColor(1f, 1f, 0f, 1f);
                } else if (chip != null) {
                    shapes.setColor(0.55f, 0.55f, 0.55f, 1f);
                } else {
                    shapes.setColor(0.28f, 0.28f, 0.28f, 1f);
                }
                shapes.rect(sx,                      sy + SLOT_SIZE - BORDER, SLOT_SIZE, BORDER);
                shapes.rect(sx,                      sy,                       SLOT_SIZE, BORDER);
                shapes.rect(sx,                      sy,                       BORDER,    SLOT_SIZE);
                shapes.rect(sx + SLOT_SIZE - BORDER, sy,                       BORDER,    SLOT_SIZE);
            }
        }

        // description panel background
        shapes.setColor(0.08f, 0.08f, 0.14f, 1f);
        shapes.rect(panelX, gridY, PANEL_W, gridH);

        shapes.end();

        // ── text pass ────────────────────────────────────────────────────────
        batch.begin();

        // title
        fontMedium.getData().setScale(1.8f);
        fontMedium.setColor(0f, 0.8f, 1f, 1f);
        fontMedium.draw(batch, "INVENTORY",
                screenW / 2f - 60f, gridY + gridH + 50f);

        // slot labels + chip names
        fontSmall.getData().setScale(1.1f);
        for (int row = 0; row < SLOT_ROWS; row++) {
            for (int col = 0; col < SLOT_COLS; col++) {
                int   idx  = row * SLOT_COLS + col;
                float sx   = gridX + col  * (SLOT_SIZE + SLOT_GAP);
                float sy   = gridY + (SLOT_ROWS - 1 - row) * (SLOT_SIZE + SLOT_GAP);
                ChipDecorator chip = chipInventory.getSlot(idx);

                fontSmall.setColor(Color.LIGHT_GRAY);
                fontSmall.draw(batch, "SLOT " + (idx + 1), sx + 8f, sy + SLOT_SIZE - 8f);

                if (chip != null) {
                    ChipType type = typeOf(chip);
                    fontSmall.setColor(Color.WHITE);
                    fontSmall.draw(batch,
                            type != null ? type.name() : chip.getName(),
                            sx + 8f, sy + SLOT_SIZE / 2f + 10f);
                    fontSmall.setColor(isActiveChip(chip) ? Color.YELLOW : new Color(0.6f, 0.6f, 0.6f, 1f));
                    fontSmall.draw(batch,
                            isActiveChip(chip) ? "ACTIVE" : "PASSIVE",
                            sx + 8f, sy + SLOT_SIZE / 2f - 10f);
                } else {
                    fontSmall.setColor(0.4f, 0.4f, 0.4f, 1f);
                    fontSmall.draw(batch, "empty", sx + 8f, sy + SLOT_SIZE / 2f + 10f);
                }
            }
        }

        // description panel
        float tx = panelX + 14f;
        float ty = gridY + gridH - 14f;

        if (selectedSlot >= 0) {
            ChipDecorator chip = chipInventory.getSlot(selectedSlot);
            fontMedium.getData().setScale(1.4f);
            fontMedium.setColor(Color.YELLOW);
            fontMedium.draw(batch,
                    chip != null ? "SLOT " + (selectedSlot + 1) : "Empty Slot",
                    tx, ty);

            if (chip != null) {
                ChipType type = typeOf(chip);
                fontSmall.getData().setScale(1.1f);
                fontSmall.setColor(Color.WHITE);
                fontSmall.draw(batch, "Type:  " + (type != null ? type.name() : "?"), tx, ty - 34f);
                fontSmall.setColor(Color.LIGHT_GRAY);
                fontSmall.draw(batch, descriptionFor(type), tx, ty - 62f);
                fontSmall.setColor(isActiveChip(chip) ? Color.YELLOW : new Color(0.7f, 0.7f, 0.7f, 1f));
                fontSmall.draw(batch, isActiveChip(chip) ? "[active — press slot key]" : "[passive]", tx, ty - 90f);
            }
        } else {
            fontSmall.getData().setScale(1.1f);
            fontSmall.setColor(0.45f, 0.45f, 0.45f, 1f);
            fontSmall.draw(batch, "Click a slot to see details", tx, ty - 20f);
        }

        // hint bar
        fontMedium.getData().setScale(1.1f);
        fontMedium.setColor(0.45f, 0.45f, 0.45f, 1f);
        fontMedium.draw(batch, "[TAB] or [ESC]  close inventory", 20f, 46f);

        batch.end();
    }

    public void dispose() { /* fonts managed by HudRenderer */ }

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

    private static String descriptionFor(ChipType type) {
        if (type == null) return "";
        switch (type) {
            case DAMAGE:      return "+5 damage per hit";
            case VAMPIRE:     return "Heals on every hit";
            case SHIELD:      return "Absorbs incoming damage";
            case BERSERK:     return "2x damage for 8 s (active)";
            case DASH:        return "Enhanced dash ability";
            case DOUBLE_JUMP: return "Enables double jump";
            case FIRE_AURA:   return "AoE fire damage aura";
            case HEALTH:      return "Increases maximum HP";
            case ICE_ARROW:   return "Slows enemies on hit";
            case SPEED:       return "Applies slow on enemies";
            default:          return "";
        }
    }
}
