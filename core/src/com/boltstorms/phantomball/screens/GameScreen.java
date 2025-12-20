package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boltstorms.phantomball.PhantomBallGame;
import com.boltstorms.phantomball.gameplay.WorldController;
import com.boltstorms.phantomball.util.Const;
import com.boltstorms.phantomball.util.PlayerProfile;

public class GameScreen extends ScreenAdapter {

    private final PhantomBallGame game;

    private ShapeRenderer sr;
    private WorldController world;

    private SpriteBatch batch;
    private BitmapFont font;

    // ✅ NEW: fixed virtual camera/viewport
    private OrthographicCamera cam;
    private Viewport viewport;

    public GameScreen(PhantomBallGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        sr = new ShapeRenderer();

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.6f);

        cam = new OrthographicCamera();
        viewport = new FitViewport(Const.VIRTUAL_W, Const.VIRTUAL_H, cam);
        viewport.apply(true);

        // World uses virtual size ALWAYS (matches android + desktop)
        world = new WorldController();
        world.resize((int) Const.VIRTUAL_W, (int) Const.VIRTUAL_H);

        sr.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        sr.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);
    }

    @Override
    public void render(float delta) {
        // Input (tap/click)
        if (Gdx.input.justTouched()) {
            world.onTap();
        }

        // Update
        world.update(delta);

        // Clear
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Draw shapes
        sr.begin(ShapeRenderer.ShapeType.Filled);
        world.draw(sr);
        sr.end();

        // Draw HUD + player name
        batch.begin();

        String state = world.isGhostState() ? "GHOST" : "MORTAL";
        font.draw(batch, "Score: " + world.getScore(), 20, Const.VIRTUAL_H - 20);
        font.draw(batch, "State: " + state, 20, Const.VIRTUAL_H - 60);

        // ✅ Draw player name above ball
        Vector2 pos = world.getBallPos();
        String name = PlayerProfile.getPlayerName();
        font.draw(batch, name, pos.x - 20, pos.y + world.getBallRadius() + 30);

        if (world.isDead()) {
            font.draw(batch, "TAP TO RESTART", 20, Const.VIRTUAL_H - 100);
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
