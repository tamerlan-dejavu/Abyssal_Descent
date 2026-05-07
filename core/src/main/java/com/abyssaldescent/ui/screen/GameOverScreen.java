package com.abyssaldescent.ui.screen;

import com.abyssaldescent.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public final class GameOverScreen implements Screen {

    private static final Color BG_DARK    = new Color(0.04f, 0.00f, 0.06f, 1f);
    private static final Color TITLE_RED  = new Color(0.80f, 0.08f, 0.08f, 1f);
    private static final Color PANEL_FILL = new Color(0.08f, 0.04f, 0.10f, 0.85f);

    private static final float PANEL_W = 700f;
    private static final float PANEL_H = 420f;

    private final GameOverStats stats;

    private SpriteBatch   batch;
    private ShapeRenderer shapes;
    private BitmapFont    fontTitle;
    private BitmapFont    fontBody;
    private GlyphLayout   layout;

    private MenuButton returnButton;

    public GameOverScreen(GameOverStats stats) {
        this.stats = stats;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);

        batch  = new SpriteBatch();
        shapes = new ShapeRenderer();
        layout = new GlyphLayout();

        fontTitle = new BitmapFont();
        fontTitle.getData().setScale(3.2f);

        fontBody = new BitmapFont();
        fontBody.getData().setScale(1.5f);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float btnW = 320f;
        float btnH = 70f;
        float btnX = (sw - btnW) * 0.5f;
        float panelY = (sh - PANEL_H) * 0.5f;
        float btnY = panelY + 40f;

        returnButton = new MenuButton(
                "RETURN TO MENU",
                btnX, btnY, btnW, btnH,
                () -> {
                    GameStateManager.getInstance().resetForNewRun();
                    UiManager.getInstance().showMainMenu();
                }
        );
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(BG_DARK.r, BG_DARK.g, BG_DARK.b, 1f);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();

        float mx = Gdx.input.getX();
        float my = sh - Gdx.input.getY();
        returnButton.update(mx, my);

        if (Gdx.input.justTouched()) {
            returnButton.handleClick(mx, my);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameStateManager.getInstance().resetForNewRun();
            UiManager.getInstance().showMainMenu();
            return;
        }

        float panelX = (sw - PANEL_W) * 0.5f;
        float panelY = (sh - PANEL_H) * 0.5f;

        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA,
                           com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(PANEL_FILL);
        shapes.rect(panelX, panelY, PANEL_W, PANEL_H);
        shapes.setColor(0.35f, 0.15f, 0.40f, 0.9f);
        shapes.rect(panelX + 40f, panelY + 130f, PANEL_W - 80f, 2f);
        returnButton.renderBackground(shapes);
        shapes.end();

        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.begin();

        fontTitle.setColor(TITLE_RED);
        layout.setText(fontTitle, "VOID CLAIMS YOU");
        float titleX = (sw - layout.width) * 0.5f;
        float titleY = panelY + PANEL_H - 50f;
        fontTitle.draw(batch, layout, titleX, titleY);

        drawStats(sw, panelY);

        returnButton.renderLabel(batch, fontBody);

        batch.end();
    }

    private void drawStats(float sw, float panelY) {
        fontBody.setColor(Color.LIGHT_GRAY);

        String tierLine = "Tier reached:     " + tierName(stats.floorReached);
        layout.setText(fontBody, tierLine);
        fontBody.draw(batch, layout, (sw - layout.width) * 0.5f, panelY + PANEL_H - 130f);

        String respLine = "Respawns used:  " + stats.respawnsUsed + " / " + stats.maxRespawns;
        layout.setText(fontBody, respLine);
        fontBody.draw(batch, layout, (sw - layout.width) * 0.5f, panelY + PANEL_H - 195f);
    }

    private static String tierName(int floor) {
        switch (floor) {
            case 1:  return "Upper Ruins";
            case 2:  return "Sunken Crypts";
            case 3:  return "Void Core";
            default: return "Floor " + floor;
        }
    }

    @Override
    public void resize(int w, int h) {
        float btnW = 320f;
        float btnH = 70f;
        float btnX = (w - btnW) * 0.5f;
        float panelY = (h - PANEL_H) * 0.5f;
        float btnY   = panelY + 40f;
        returnButton = new MenuButton(
                "RETURN TO MENU",
                btnX, btnY, btnW, btnH,
                () -> {
                    GameStateManager.getInstance().resetForNewRun();
                    UiManager.getInstance().showMainMenu();
                }
        );
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (batch     != null) batch.dispose();
        if (shapes    != null) shapes.dispose();
        if (fontTitle != null) fontTitle.dispose();
        if (fontBody  != null) fontBody.dispose();
    }
}
