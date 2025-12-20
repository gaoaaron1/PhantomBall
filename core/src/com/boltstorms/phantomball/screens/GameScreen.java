package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.boltstorms.phantomball.PhantomBallGame;
import com.boltstorms.phantomball.gameplay.WorldController;

public class GameScreen extends ScreenAdapter {

    private final PhantomBallGame game;

    private ShapeRenderer sr;
    private WorldController world;

    // NEW: UI rendering
    private SpriteBatch batch;
    private BitmapFont font;

    public GameScreen(PhantomBallGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();

        world = new WorldController();
        world.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // NEW: create UI tools
        batch = new SpriteBatch();
        font = new BitmapFont();              // default font
        font.getData().setScale(1.6f);        // bigger text
    }

    @Override
    public void render(float delta) {
        // Input
        if (Gdx.input.justTouched()) {
            world.onTap();
        }

        // Update
        world.update(delta);

        // Clear
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw shapes (game)
        sr.begin(ShapeRenderer.ShapeType.Filled);
        world.draw(sr);
        sr.end();

        // Draw text (HUD) AFTER shapes
        batch.begin();

        String state = world.isGhostState() ? "GHOST" : "MORTAL";
        font.draw(batch, "Score: " + world.getScore(), 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "State: " + state, 20, Gdx.graphics.getHeight() - 60);

        if (world.isDead()) {
            font.draw(batch, "TAP TO RESTART", 20, Gdx.graphics.getHeight() - 100);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
