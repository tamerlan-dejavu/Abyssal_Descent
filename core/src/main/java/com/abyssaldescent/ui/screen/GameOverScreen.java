package com.abyssaldescent.ui.screen;

import com.abyssaldescent.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public final class GameOverScreen implements Screen {

    private final SpriteBatch batch;
    private final BitmapFont  fontLarge;
    private final BitmapFont  fontSmall;

    public GameOverScreen() {
        batch     = new SpriteBatch();
        fontLarge = new BitmapFont();
        fontLarge.getData().setScale(4f);
        fontSmall = new BitmapFont();
        fontSmall.getData().setScale(1.8f);
    }

    @Override
    public void show() {}

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0f, 0.08f, 1f);
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        batch.begin();
        fontLarge.setColor(Color.RED);
        fontLarge.draw(batch, "GAME  OVER", sw * 0.5f - 180f, sh * 0.5f + 60f);
        fontSmall.setColor(Color.LIGHT_GRAY);
        fontSmall.draw(batch, "Press  ENTER  to  return  to  menu",
                sw * 0.5f - 240f, sh * 0.5f - 30f);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            GameStateManager.getInstance().resetForNewRun();
            UiManager.getInstance().showMainMenu();
        }
    }

    @Override public void resize(int w, int h) {}
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        batch.dispose();
        fontLarge.dispose();
        fontSmall.dispose();
    }
}
