package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boltstorms.phantomball.PhantomBallGame;
import com.boltstorms.phantomball.gameplay.WorldController;
import com.boltstorms.phantomball.util.Const;

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

    // Bottom HUD bar + card areas
    private Rectangle hudBar;
    private Rectangle blueCard;
    private Rectangle redCard;

    // Textures
    private Texture bg;
    private Texture blueCardTex;
    private Texture redCardTex;

    // UI animation
    private boolean touchDown = false;
    private int pressed = 0; // 0 none, 1 blue, 2 red
    private float blueScale = 1f;
    private float redScale = 1f;

    private float barH = 170f;
    private float playH;


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

        // ===== World setup =====
        world = new WorldController();

        // Height of the bottom HUD bar
        float barH = 170f;

        // HUD bar rectangle (screen-space)
        hudBar = new Rectangle(0, 0, Const.VIRTUAL_W, barH);

        // Playable world height (everything ABOVE the HUD)
        float playH = Const.VIRTUAL_H - barH;

        // IMPORTANT: world only simulates inside play area
        world.resize((int) Const.VIRTUAL_W, (int) playH);

        // ===== Pause UI =====
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

        // ===== Card layout (inside HUD bar) =====
        float cardAreaW = 210f;
        float cardAreaH = barH - 32f;
        float gap = 32f;

        float totalW = cardAreaW * 2f + gap;
        float startX = (Const.VIRTUAL_W - totalW) * 0.5f;
        float cardY = 16f;

        blueCard = new Rectangle(startX, cardY, cardAreaW, cardAreaH);
        redCard  = new Rectangle(startX + cardAreaW + gap, cardY, cardAreaW, cardAreaH);

        // ===== Background =====
        bg = new Texture(Gdx.files.internal("bg_Fireplace.png"));
        bg.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // ===== Card art =====
        blueCardTex = new Texture(Gdx.files.internal("BlueSpiritCard1.png"));
        redCardTex  = new Texture(Gdx.files.internal("RedSpiritCard1.png"));
        blueCardTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        redCardTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {

        smoothCardScales(delta);

        // ===== Input =====
        if (Gdx.input.justTouched()) {
            touchDown = true;

            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);
            pressed = 0;

            if (world.isPaused()) {
                if (resumeBtn.contains(touch)) world.setPaused(false);
                else if (exitBtn.contains(touch)) {
                    world.setPaused(false);
                    game.setScreen(new MenuScreen(game));
                    return;
                }
                touchDown = false;
                return;
            }

            if (pauseBtn.contains(touch)) {
                world.setPaused(true);
                touchDown = false;
                return;
            }

            if (hudBar.contains(touch)) {
                if (blueCard.contains(touch)) pressed = 1;
                else if (redCard.contains(touch)) pressed = 2;
            }
        }

        if (touchDown && !Gdx.input.isTouched()) {
            touchDown = false;

            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (pressed == 1 && blueCard.contains(touch)) world.summonBlue();
            if (pressed == 2 && redCard.contains(touch)) world.summonRed();

            pressed = 0;
        }

        // ===== Update =====
        world.update(delta);

        // ===== Render =====
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background
        batch.begin();
        batch.draw(bg, 0, 0, Const.VIRTUAL_W, Const.VIRTUAL_H);
        batch.end();

        // World
// Draw world ABOVE the HUD bar
        batch.setTransformMatrix(batch.getTransformMatrix().idt().translate(0, hudBar.height, 0));
        sr.setTransformMatrix(sr.getTransformMatrix().idt().translate(0, hudBar.height, 0));

        world.draw(sr, batch);

// Reset transforms for HUD
        batch.setTransformMatrix(batch.getTransformMatrix().idt());
        sr.setTransformMatrix(sr.getTransformMatrix().idt());


        // HUD
        drawHud();

        // Pause overlay
        if (world.isPaused()) {
            drawPauseOverlay();
        }
    }

    private void smoothCardScales(float delta) {
        float targetBlue = (pressed == 1) ? 0.94f : 1f;
        float targetRed  = (pressed == 2) ? 0.94f : 1f;

        float k = Math.min(1f, delta * 18f);
        blueScale += (targetBlue - blueScale) * k;
        redScale  += (targetRed  - redScale)  * k;
    }

    private void drawHud() {
        // Score + pause
        batch.begin();
        font.draw(batch, "Score: " + world.getScore(), 20, Const.VIRTUAL_H - 20);
        font.draw(batch, "||", pauseBtn.x + 18, pauseBtn.y + 46);
        batch.end();

        // Bottom bar
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.62f);
        sr.rect(hudBar.x, hudBar.y, hudBar.width, hudBar.height);
        sr.end();

        // Cards (ONLY the card art)
        drawCard(blueCardTex, blueCard, blueScale, world.isBlueUsed());
        drawCard(redCardTex,  redCard,  redScale,  world.isRedUsed());
    }

    private void drawCard(Texture tex, Rectangle area, float scale, boolean used) {
        // How much space to leave inside the card area
        float pad = 10f;

        float availW = area.width - pad * 2f;
        float availH = area.height - pad * 2f;

        // Texture aspect ratio (this is the important part)
        float texW = tex.getWidth();
        float texH = tex.getHeight();
        float aspect = texW / texH; // width = height * aspect

        // Fit by height first (so the card stays tall like your reference)
        float h = availH;
        float w = h * aspect;

        // If it becomes too wide, clamp by width instead
        if (w > availW) {
            w = availW;
            h = w / aspect;
        }

        // Press animation scale
        w *= scale;
        h *= scale;

        // Center inside the area
        float x = area.x + (area.width - w) * 0.5f;
        float y = area.y + (area.height - h) * 0.5f;

        batch.begin();
        batch.setColor(1f, 1f, 1f, used ? 0.30f : 1f);
        batch.draw(tex, x, y, w, h);
        batch.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    private void drawPauseOverlay() {
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.55f);
        sr.rect(0, 0, Const.VIRTUAL_W, Const.VIRTUAL_H);
        sr.end();

        batch.begin();
        font.draw(batch, "PAUSED",
                Const.VIRTUAL_W * 0.5f - 60,
                Const.VIRTUAL_H * 0.5f + 160);
        font.draw(batch, "RESUME", resumeBtn.x + 85, resumeBtn.y + 55);
        font.draw(batch, "EXIT TO MENU", exitBtn.x + 55, exitBtn.y + 55);
        batch.end();
    }

    @Override
    public void dispose() {
        if (world != null) world.dispose();
        if (bg != null) bg.dispose();
        if (blueCardTex != null) blueCardTex.dispose();
        if (redCardTex != null) redCardTex.dispose();
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
