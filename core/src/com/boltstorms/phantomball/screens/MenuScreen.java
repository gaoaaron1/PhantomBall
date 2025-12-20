package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.boltstorms.phantomball.util.Const;
import com.boltstorms.phantomball.util.PlayerProfile;

public class MenuScreen extends ScreenAdapter {

    private final PhantomBallGame game;

    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer sr;

    private OrthographicCamera cam;
    private Viewport viewport;

    private Texture logo;

    private String playerName = "Player";

    private Rectangle nameRect;
    private Rectangle startRect;

    private boolean consumedThisTouch = false;
    private int pressed = 0; // 0 none, 1 name, 2 start

    // animation time
    private float t = 0f;

    // simple starfield
    private static final int STAR_COUNT = 60;
    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final float[] starR = new float[STAR_COUNT];
    private final float[] starA = new float[STAR_COUNT];

    public MenuScreen(PhantomBallGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        sr = new ShapeRenderer();

        font = new BitmapFont();
        font.getData().setScale(1.55f);

        cam = new OrthographicCamera();
        viewport = new FitViewport(Const.VIRTUAL_W, Const.VIRTUAL_H, cam);
        viewport.apply(true);

        batch.setProjectionMatrix(cam.combined);
        sr.setProjectionMatrix(cam.combined);

        playerName = PlayerProfile.getPlayerName();
        logo = new Texture(Gdx.files.internal("PhantomBallLogo.png"));

        rebuildLayout();
        initStars();
    }

    private void initStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(0f, Const.VIRTUAL_W);
            starY[i] = MathUtils.random(0f, Const.VIRTUAL_H);
            starR[i] = MathUtils.random(1.2f, 2.6f);
            starA[i] = MathUtils.random(0.10f, 0.55f);
        }
    }

    private void rebuildLayout() {
        float cardW = Const.VIRTUAL_W * 0.86f;
        float cardH = 128f;
        float x = (Const.VIRTUAL_W - cardW) * 0.5f;

        // pushed lower so logo area is clean
        float nameY = Const.VIRTUAL_H * 0.33f;
        float startY = Const.VIRTUAL_H * 0.18f;

        nameRect = new Rectangle(x, nameY, cardW, cardH);
        startRect = new Rectangle(x, startY, cardW, cardH);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(cam.combined);
        sr.setProjectionMatrix(cam.combined);
    }

    @Override
    public void render(float delta) {
        t += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.exit();
        }

        // Touch handling
        if (Gdx.input.justTouched() && !consumedThisTouch) {
            consumedThisTouch = true;

            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            pressed = 0;
            if (nameRect.contains(touch)) pressed = 1;
            else if (startRect.contains(touch)) pressed = 2;
        }

        // on release: activate
        if (consumedThisTouch && !Gdx.input.isTouched()) {
            consumedThisTouch = false;

            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touch);

            if (pressed == 1 && nameRect.contains(touch)) openNameKeyboard();
            if (pressed == 2 && startRect.contains(touch)) startGame();

            pressed = 0;
        }

        // Clear
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ===== Background (dark gradient + glow + stars) =====
        sr.begin(ShapeRenderer.ShapeType.Filled);

        // top gradient-ish slabs
        sr.setColor(0.06f, 0.07f, 0.10f, 1f);
        sr.rect(0, Const.VIRTUAL_H * 0.55f, Const.VIRTUAL_W, Const.VIRTUAL_H * 0.45f);

        sr.setColor(0.03f, 0.03f, 0.05f, 1f);
        sr.rect(0, 0, Const.VIRTUAL_W, Const.VIRTUAL_H * 0.55f);

        // neon glows (animated)
        float glow = 0.08f + 0.03f * MathUtils.sin(t * 1.3f);
        sr.setColor(0.35f, 0.75f, 1.0f, glow);
        sr.rect(0, Const.VIRTUAL_H * 0.62f, Const.VIRTUAL_W, Const.VIRTUAL_H * 0.38f);

        sr.setColor(1.0f, 0.45f, 0.75f, 0.05f + 0.02f * MathUtils.sin(t * 1.1f + 1.2f));
        sr.rect(0, 0, Const.VIRTUAL_W, Const.VIRTUAL_H * 0.30f);

        // stars
        for (int i = 0; i < STAR_COUNT; i++) {
            float tw = 0.25f + 0.25f * MathUtils.sin(t * 2.2f + i);
            sr.setColor(0.75f, 0.90f, 1.0f, starA[i] + tw * 0.15f);
            sr.circle(starX[i], starY[i], starR[i]);
        }

        // glass cards
        drawGlassCard(nameRect, pressed == 1);
        drawGlassCard(startRect, pressed == 2);

        sr.end();

        // ===== Logo + text =====
        batch.begin();

        drawLogo(batch);

        float pad = 30f;

        // NAME card text
        drawPillLabel(batch, "PLAYER", nameRect.x + pad, nameRect.y + nameRect.height - 36);

        // bigger name
        BitmapFont.BitmapFontData data = font.getData();
        float oldX = data.scaleX, oldY = data.scaleY;

        font.getData().setScale(2.0f);
        font.draw(batch, playerName, nameRect.x + pad, nameRect.y + 62);
        font.getData().setScale(oldX, oldY);

        font.draw(batch, "Tap to edit", nameRect.x + pad, nameRect.y + 28);

        // START card text
        font.getData().setScale(2.2f);
        font.draw(batch, "START", startRect.x + pad, startRect.y + 78);
        font.getData().setScale(oldX, oldY);

        font.draw(batch, "Tap to play", startRect.x + pad, startRect.y + 32);

        batch.end();
    }

    private void drawLogo(SpriteBatch batch) {
        if (logo == null) return;

        float maxW = Const.VIRTUAL_W * 0.78f;
        float scale = maxW / logo.getWidth();
        float w = logo.getWidth() * scale;
        float h = logo.getHeight() * scale;

        float x = (Const.VIRTUAL_W - w) * 0.5f;

        float bob = MathUtils.sin(t * 2.2f) * 6f;
        float y = Const.VIRTUAL_H * 0.56f + bob;

        batch.draw(logo, x, y, w, h);
    }

    private void drawGlassCard(Rectangle r, boolean isPressed) {
        // shadow
        sr.setColor(0f, 0f, 0f, 0.28f);
        sr.rect(r.x + 8, r.y - 8, r.width, r.height);

        // glass base
        float baseA = isPressed ? 0.16f : 0.12f;
        sr.setColor(1f, 1f, 1f, baseA);
        sr.rect(r.x, r.y, r.width, r.height);

        // top shine
        sr.setColor(1f, 1f, 1f, isPressed ? 0.10f : 0.08f);
        sr.rect(r.x, r.y + r.height - 22f, r.width, 22f);

        // border
        sr.setColor(0.70f, 0.90f, 1f, isPressed ? 0.22f : 0.18f);
        sr.rect(r.x, r.y, r.width, 3f);
        sr.rect(r.x, r.y + r.height - 3f, r.width, 3f);
        sr.rect(r.x, r.y, 3f, r.height);
        sr.rect(r.x + r.width - 3f, r.y, 3f, r.height);

        // left accent
        sr.setColor(0.55f, 0.85f, 1f, isPressed ? 0.35f : 0.25f);
        sr.rect(r.x, r.y, 7f, r.height);
    }

    private void drawPillLabel(SpriteBatch batch, String text, float x, float y) {
        // A small label vibe using text only (clean)
        // If you later add a skin/font, this becomes a real pill.
        font.draw(batch, text, x, y);
    }

    private void openNameKeyboard() {
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                if (text == null) return;
                text = text.trim();
                playerName = text.isEmpty() ? "Player" : text;
                PlayerProfile.setPlayerName(playerName);
            }

            @Override
            public void canceled() {
                // keep current name
            }
        }, "Enter Name", playerName, "Player");
    }

    private void startGame() {
        PlayerProfile.setPlayerName(playerName);
        game.setScreen(new GameScreen(game));
    }

    @Override
    public void dispose() {
        if (logo != null) logo.dispose();
        if (sr != null) sr.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
