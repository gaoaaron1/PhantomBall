package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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

    private OrthographicCamera cam;
    private Viewport viewport;

    // Pause UI
    private Rectangle pauseBtn;
    private Rectangle resumeBtn;
    private Rectangle exitBtn;

    private Texture bg;

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

        world = new WorldController();
        world.resize((int) Const.VIRTUAL_W, (int) Const.VIRTUAL_H);

        float btnSize = 64f;

        pauseBtn = new Rectangle(
                Const.VIRTUAL_W - btnSize - 20,
                Const.VIRTUAL_H - btnSize - 20,
                btnSize,
                btnSize
        );

        resumeBtn = new Rectangle(
                Const.VIRTUAL_W * 0.5f - 140,
                Const.VIRTUAL_H * 0.5f + 20,
                280,
                80
        );

        exitBtn = new Rectangle(
                Const.VIRTUAL_W * 0.5f - 140,
                Const.VIRTUAL_H * 0.5f - 80,
                280,
                80
        );

        sr.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        bg = new Texture(Gdx.files.internal("bg_Fireplace.png"));
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        sr.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);
    }

    /**
     * Camera follows the player (ball), clamped so it never shows outside the world bounds.
     * If your "world" is bigger than Const.VIRTUAL_W/H later, swap these bounds with your world size.
     */
    private void updateCameraFollow() {
        Vector2 ballPos = world.getBallPos();
        if (ballPos == null) return;

        float halfW = viewport.getWorldWidth() * 0.5f;
        float halfH = viewport.getWorldHeight() * 0.5f;

        float camX = MathUtils.clamp(ballPos.x, halfW, Const.VIRTUAL_W - halfW);
        float camY = MathUtils.clamp(ballPos.y, halfH, Const.VIRTUAL_H - halfH);

        cam.position.set(camX, camY, 0f);
        cam.update();
    }

    @Override
    public void render(float delta) {

        // Input (tap/click)
        if (Gdx.input.justTouched()) {
            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (world.isPaused()) {
                if (resumeBtn.contains(touch)) {
                    world.setPaused(false);
                } else if (exitBtn.contains(touch)) {
                    world.setPaused(false);
                    game.setScreen(new MenuScreen(game));
                    return;
                }
            } else {
                if (pauseBtn.contains(touch)) {
                    world.setPaused(true);
                } else {
                    world.onTap();
                }
            }
        }

        world.update(delta);

        // ✅ Camera follows player AFTER world update
        updateCameraFollow();

        // ✅ Keep renderers synced with camera
        sr.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);

        // Clear
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background (drawn in world space; will move with camera)
        batch.begin();
        batch.draw(bg, 0, 0, Const.VIRTUAL_W, Const.VIRTUAL_H);
        batch.end();

        // ✅ World draws props with ShapeRenderer AND player with SpriteBatch
        world.draw(sr, batch);

        // HUD + pause icon + name (currently also in world space)
        batch.begin();

        String state = world.isGhostState() ? "GHOST" : "MORTAL";
        font.draw(batch, "Score: " + world.getScore(), 20, Const.VIRTUAL_H - 20);
        font.draw(batch, "State: " + state, 20, Const.VIRTUAL_H - 60);

        // Pause button icon
        font.draw(batch, "||", pauseBtn.x + 18, pauseBtn.y + 46);

        // Player name above ball
        Vector2 pos = world.getBallPos();
        String name = PlayerProfile.getPlayerName();
        font.draw(batch, name, pos.x - 20, pos.y + world.getBallRadius() + 30);

        if (world.isDead()) {
            font.draw(batch, "TAP TO RESTART", 20, Const.VIRTUAL_H - 100);
        }

        batch.end();

        // Pause overlay
        if (world.isPaused()) {
            sr.begin(ShapeRenderer.ShapeType.Filled);

            sr.setColor(0f, 0f, 0f, 0.55f);
            sr.rect(0, 0, Const.VIRTUAL_W, Const.VIRTUAL_H);

            sr.setColor(1f, 1f, 1f, 0.12f);
            sr.rect(resumeBtn.x, resumeBtn.y, resumeBtn.width, resumeBtn.height);

            sr.setColor(1f, 1f, 1f, 0.12f);
            sr.rect(exitBtn.x, exitBtn.y, exitBtn.width, exitBtn.height);

            sr.end();

            batch.begin();

            font.draw(batch, "PAUSED",
                    Const.VIRTUAL_W * 0.5f - 60,
                    Const.VIRTUAL_H * 0.5f + 160);

            font.draw(batch, "RESUME",
                    resumeBtn.x + 85,
                    resumeBtn.y + 55);

            font.draw(batch, "EXIT TO MENU",
                    exitBtn.x + 55,
                    exitBtn.y + 55);

            batch.end();
        }
    }

    @Override
    public void dispose() {
        if (world != null) world.dispose(); // frees PhantomPlayer textures
        if (bg != null) bg.dispose();
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
