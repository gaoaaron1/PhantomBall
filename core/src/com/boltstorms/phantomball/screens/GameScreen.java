package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boltstorms.phantomball.PhantomBallGame;
import com.boltstorms.phantomball.backgrounds.FireplaceBackground;
import com.boltstorms.phantomball.gameplay.EvilSpirit;
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
    private Texture blueCardTex;
    private Texture redCardTex;

    // Background
    private FireplaceBackground fireplaceBg;

    // UI animation
    private boolean touchDown = false;
    private int pressed = 0; // 0 none, 1 blue, 2 red
    private float blueScale = 1f;
    private float redScale = 1f;

    // Layout
    private float barH = 170f;
    private float playH = 0f;

    private Music bgMusic;
    private boolean musicPausedByWorld = false;

    // (WorldController owns the selection timer/state; this is optional to keep)
    private EvilSpirit selectedSpirit = null;

    public GameScreen(PhantomBallGame game) {
        this.game = game;
    }

    @Override
    public void show() {

        bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/spirithunter.mp3"));
        bgMusic.setLooping(true);
        bgMusic.setVolume(0.6f);
        bgMusic.play();

        sr = new ShapeRenderer();
        batch = new SpriteBatch();

        font = new BitmapFont();
        font.getData().setScale(1.6f);

        cam = new OrthographicCamera();
        viewport = new ExtendViewport(Const.VIRTUAL_W, Const.VIRTUAL_H, cam);
        viewport.apply(true);

        cam.update();
        batch.setProjectionMatrix(cam.combined);
        sr.setProjectionMatrix(cam.combined);

        world = new WorldController();

        pauseBtn = new Rectangle();
        resumeBtn = new Rectangle();
        exitBtn = new Rectangle();

        hudBar = new Rectangle();
        blueCard = new Rectangle();
        redCard = new Rectangle();

        fireplaceBg = new FireplaceBackground();

        blueCardTex = new Texture(Gdx.files.internal("BlueSpiritCard1.png"));
        redCardTex  = new Texture(Gdx.files.internal("RedSpiritCard1.png"));
        blueCardTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        redCardTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        rebuildUiLayout(true);
    }

    private void rebuildUiLayout(boolean firstTime) {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        barH = 170f;

        hudBar.set(0, 0, worldW, barH);

        playH = worldH - barH;

        if (firstTime) {
            world.resize((int) worldW, (int) playH);
        } else {
            world.setPlayBounds(worldW, playH);
        }

        // Pause button (top-right)
        float btnSize = 64f;
        pauseBtn.set(
                worldW - btnSize - 20f,
                worldH - btnSize - 20f,
                btnSize,
                btnSize
        );

        // Pause overlay buttons (center)
        resumeBtn.set(
                worldW * 0.5f - 140f,
                worldH * 0.5f + 20f,
                280f,
                80f
        );

        exitBtn.set(
                worldW * 0.5f - 140f,
                worldH * 0.5f - 80f,
                280f,
                80f
        );

        // Card layout (inside HUD)
        float cardAreaW = 210f;
        float cardAreaH = barH - 32f;
        float gap = 32f;

        float totalW = cardAreaW * 2f + gap;
        float startX = (worldW - totalW) * 0.5f;
        float cardY = 16f;

        blueCard.set(startX, cardY, cardAreaW, cardAreaH);
        redCard.set(startX + cardAreaW + gap, cardY, cardAreaW, cardAreaH);

        float blueCenterX = blueCard.x + blueCard.width * 0.5f;
        float redCenterX  = redCard.x  + redCard.width  * 0.5f;

        float spawnY = 60f;
        world.setSummonAnchors(blueCenterX, redCenterX, spawnY);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        cam.update();
        batch.setProjectionMatrix(cam.combined);
        sr.setProjectionMatrix(cam.combined);

        rebuildUiLayout(false);
    }

    @Override
    public void render(float delta) {

        // keep music in sync with world pause
        if (world.isPaused()) {
            if (bgMusic != null && bgMusic.isPlaying()) {
                bgMusic.pause();
                musicPausedByWorld = true;
            }
        } else {
            if (bgMusic != null && musicPausedByWorld) {
                bgMusic.play();
                musicPausedByWorld = false;
            }
        }

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

            // Tap spirit in WORLD area to show its level label
            // World is rendered with +barH transform, so convert by subtracting barH.
            if (touch.y > barH) {
                float worldX = touch.x;
                float worldY = touch.y - barH;

                if (world.tapAt(worldX, worldY)) {
                    selectedSpirit = world.getSelectedSpirit();
                    touchDown = false;
                    return; // don't also trigger summon
                }
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
        if (fireplaceBg != null) fireplaceBg.update(delta);

        // ===== Render =====
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Background
        batch.begin();
        if (fireplaceBg != null) {
            fireplaceBg.render(batch, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        }
        batch.end();

        // World draws ABOVE the HUD bar (world y=0 starts at screen y=barH)
        batch.setTransformMatrix(batch.getTransformMatrix().idt().translate(0, barH, 0));
        sr.setTransformMatrix(sr.getTransformMatrix().idt().translate(0, barH, 0));

        world.draw(sr, batch);

        // Draw label above selected spirit (inside world transform)
        EvilSpirit s = world.getSelectedSpirit();
        if (s != null) {
            batch.begin();
            float labelX = s.getPos().x - 18f;
            float labelY = s.getPos().y + s.getR() + 28f;
            font.draw(batch, "LV " + s.getLevel(), labelX, labelY);
            batch.end();
        }

        // Reset transforms for HUD drawing
        batch.setTransformMatrix(batch.getTransformMatrix().idt());
        sr.setTransformMatrix(sr.getTransformMatrix().idt());

        drawHud();

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
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        // ===== Score + pause =====
        batch.begin();
        font.draw(batch, "Score: " + world.getScore(), 20, worldH - 20);
        font.draw(batch, "||", pauseBtn.x + 18, pauseBtn.y + 46);
        batch.end();

        // ===== Bottom bar background =====
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.62f);
        sr.rect(0, 0, worldW, barH);
        sr.end();

        // ===== Cards =====
        drawCard(blueCardTex, blueCard, blueScale, world.isBlueUsed(), world.getBlueLevel());
        drawCard(redCardTex,  redCard,  redScale,  world.isRedUsed(),  world.getRedLevel());

        // ===== XP bars (horizontal, bordered) =====
        float barPadX = 16f;
        float barHgt  = 22f;
        float barPadY = 12f;

        drawXpBarHorizontal(
                blueCard.x + barPadX,
                blueCard.y + barPadY,
                blueCard.width - barPadX * 2f,
                barHgt,
                world.getBlueXp(),
                world.getBlueXpToNext()
        );

        drawXpBarHorizontal(
                redCard.x + barPadX,
                redCard.y + barPadY,
                redCard.width - barPadX * 2f,
                barHgt,
                world.getRedXp(),
                world.getRedXpToNext()
        );
    }


    private void drawXpBarHorizontal(float x, float y, float w, float h, float xp, float xpToNext) {
        float pct = (xpToNext <= 0f) ? 0f : Math.min(1f, xp / xpToNext);

        // Container (black fill)
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 1f);
        sr.rect(x, y, w, h);

        // Fill (white)
        sr.setColor(1f, 1f, 1f, 1f);
        sr.rect(x, y, w * pct, h);
        sr.end();

        // White border (like your screenshot)
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(1f, 1f, 1f, 1f);
        sr.rect(x, y, w, h);
        // optional: thicker border (draw a second outline slightly inset)
        sr.rect(x + 1f, y + 1f, w - 2f, h - 2f);
        sr.end();
    }

    private void drawCard(Texture tex, Rectangle area, float scale, boolean used, int level) {
        float pad = 10f;

        float availW = area.width - pad * 2f;
        float availH = area.height - pad * 2f;

        float texW = tex.getWidth();
        float texH = tex.getHeight();
        float aspect = texW / texH;

        float h = availH;
        float w = h * aspect;

        if (w > availW) {
            w = availW;
            h = w / aspect;
        }

        w *= scale;
        h *= scale;

        float x = area.x + (area.width - w) * 0.5f;
        float y = area.y + (area.height - h) * 0.5f;

        batch.begin();
        batch.setColor(1f, 1f, 1f, used ? 0.30f : 1f);
        batch.draw(tex, x, y, w, h);
        batch.setColor(1f, 1f, 1f, 1f);

        font.draw(batch, "LV " + level, area.x + 14f, area.y + area.height - 14f);
        batch.end();
    }

    private void drawPauseOverlay() {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(0f, 0f, 0f, 0.55f);
        sr.rect(0, 0, worldW, worldH);
        sr.end();

        batch.begin();
        font.draw(batch, "PAUSED",
                worldW * 0.5f - 60,
                worldH * 0.5f + 160);
        font.draw(batch, "RESUME", resumeBtn.x + 85, resumeBtn.y + 55);
        font.draw(batch, "EXIT TO MENU", exitBtn.x + 55, exitBtn.y + 55);
        batch.end();
    }

    @Override
    public void dispose() {
        if (world != null) world.dispose();

        if (fireplaceBg != null) {
            fireplaceBg.dispose();
            fireplaceBg = null;
        }

        if (blueCardTex != null) blueCardTex.dispose();
        if (redCardTex != null) redCardTex.dispose();
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.dispose();
            bgMusic = null;
        }
    }
}
