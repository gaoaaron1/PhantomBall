package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.boltstorms.phantomball.PhantomBallGame;

public class MenuScreen extends ScreenAdapter {

    private final PhantomBallGame game;
    private ShapeRenderer sr;

    public MenuScreen(PhantomBallGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        // Tap/click to start
        if (Gdx.input.justTouched()) {
            game.setScreen(new GameScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Minimal “menu look” (we'll add text later)
        sr.begin(ShapeRenderer.ShapeType.Filled);
        // A big button-ish rectangle
        sr.setColor(1f, 1f, 1f, 0.12f);
        sr.rect(60, 180, Gdx.graphics.getWidth() - 120, 220);

        // A “ball” indicator
        sr.setColor(1f, 1f, 1f, 0.8f);
        sr.circle(Gdx.graphics.getWidth() * 0.5f, 290, 26);
        sr.end();
    }

    @Override
    public void dispose() {
        if (sr != null) sr.dispose();
    }
}
