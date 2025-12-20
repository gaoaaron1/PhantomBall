package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.boltstorms.phantomball.util.Const;

public class Ball {

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();

    private float r = Const.BALL_START_RADIUS;
    private float baseRadius = r;

    private boolean ghost = false;

    // Animation (pulse + rotation)
    private float animTime = 0f;
    private float rotation = 0f;

    private static final float PULSE_AMPLITUDE = 0.08f; // 8% size change
    private static final float PULSE_SPEED = 3.5f;      // higher = faster pulse
    private static final float ROT_SPEED = 90f;         // degrees per second

    // NEW: sprite flicker animation like spirits
    private float frameTimer = 0f;
    private static final float FRAME_TIME = 0.18f;      // swap speed
    private boolean frameB = false;                     // false = frame1, true = frame2

    // 2 frames for each mode
    private final Texture normal1;
    private final Texture normal2;
    private final Texture ghost1;
    private final Texture ghost2;

    public Ball() {
        // Normal
        normal1 = new Texture(Gdx.files.internal("PhantomPlayer.png"));
        normal2 = new Texture(Gdx.files.internal("PhantomPlayer.png"));

        // Ghost
        ghost1 = new Texture(Gdx.files.internal("PhantomPlayerPink.png"));
        ghost2 = new Texture(Gdx.files.internal("PhantomPlayerPink.png"));

        normal1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        normal2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ghost1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ghost2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void reset(float x, float y) {
        pos.set(x, y);
        vel.set(Const.BALL_SPEED_X, Const.BALL_SPEED_Y)
                .rotateDeg(MathUtils.random(-25f, 25f));

        ghost = false;
        r = Const.BALL_START_RADIUS;
        baseRadius = r;

        animTime = 0f;
        rotation = 0f;

        frameTimer = 0f;
        frameB = MathUtils.randomBoolean(); // start randomly on frame 1 or 2
    }

    public void toggleGhost() {
        ghost = !ghost;

        // Optional: small randomness when switching modes so it feels "alive"
        frameB = MathUtils.randomBoolean();
        frameTimer = 0f;
    }

    public boolean isGhost() {
        return ghost;
    }

    public Vector2 getPos() {
        return pos;
    }

    public float getR() {
        return r;
    }

    public void grow(float amount) {
        r = MathUtils.clamp(r + amount, Const.BALL_MIN_RADIUS, Const.BALL_MAX_RADIUS);
        baseRadius = r;
    }

    public void shrink(float amount) {
        r = MathUtils.clamp(r - amount, Const.BALL_MIN_RADIUS, Const.BALL_MAX_RADIUS);
        baseRadius = r;
    }

    public boolean isDeadBySize() {
        return r <= Const.BALL_MIN_RADIUS + 0.001f;
    }

    public void update(float dt, float W, float H) {
        // Pulse + rotate
        animTime += dt;
        rotation = (rotation + ROT_SPEED * dt) % 360f;

        // Frame swap (spirit effect)
        frameTimer += dt;
        if (frameTimer >= FRAME_TIME) {
            frameTimer -= FRAME_TIME;
            frameB = !frameB;
        }

        // Movement
        pos.mulAdd(vel, dt);

        if (pos.x < r) { pos.x = r; vel.x *= -1; }
        if (pos.x > W - r) { pos.x = W - r; vel.x *= -1; }
        if (pos.y < r) { pos.y = r; vel.y *= -1; }
        if (pos.y > H - r) { pos.y = H - r; vel.y *= -1; }
    }

    private Texture getCurrentTexture() {
        if (ghost) {
            return frameB ? ghost2 : ghost1;
        } else {
            return frameB ? normal2 : normal1;
        }
    }

    public void draw(SpriteBatch batch) {
        Texture tex = getCurrentTexture();

        // Pulsate scale
        float pulse = 1f + MathUtils.sin(animTime * PULSE_SPEED) * PULSE_AMPLITUDE;

        float size = baseRadius * 2f;
        float drawSize = size * pulse;

        float x = pos.x - drawSize * 0.5f;
        float y = pos.y - drawSize * 0.5f;

        batch.draw(
                tex,
                x, y,
                drawSize * 0.5f, drawSize * 0.5f, // origin (center)
                drawSize, drawSize,
                1f, 1f,
                rotation,
                0, 0,
                tex.getWidth(), tex.getHeight(),
                false, false
        );
    }

    public void dispose() {
        normal1.dispose();
        normal2.dispose();
        ghost1.dispose();
        ghost2.dispose();
    }
}
