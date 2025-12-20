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

    private final PhantomType type;

    // animation
    private float animTime = 0f;
    private float rotation = 0f;
    private boolean frameB = false;
    private float frameTimer = 0f;

    private static final float FRAME_TIME = 0.18f;
    private static final float ROT_SPEED = 90f;
    private static final float PULSE_AMPLITUDE = 0.08f;
    private static final float PULSE_SPEED = 3.5f;

    private final Texture frame1;
    private final Texture frame2;

    private float hitCooldown = 0f;

    public Ball(PhantomType type) {
        this.type = type;

        if (type == PhantomType.BLUE) {
            frame1 = new Texture(Gdx.files.internal("PhantomPlayer.png"));
            frame2 = new Texture(Gdx.files.internal("PhantomPlayer.png"));
        } else {
            frame1 = new Texture(Gdx.files.internal("PhantomPlayerPink.png"));
            frame2 = new Texture(Gdx.files.internal("PhantomPlayerPink.png"));
        }

        frame1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        frame2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    public void reset(float x, float y) {
        pos.set(x, y);
        vel.set(Const.BALL_SPEED_X, Const.BALL_SPEED_Y)
                .rotateDeg(MathUtils.random(-25f, 25f));

        animTime = 0f;
        rotation = 0f;
        frameTimer = 0f;
        frameB = MathUtils.randomBoolean();
        r = Const.BALL_START_RADIUS;
    }
    public boolean canBeHit() { return hitCooldown <= 0f; }
    public void triggerHitCooldown(float seconds) { hitCooldown = seconds; }
    public void update(float dt, float W, float H) {
        if (hitCooldown > 0f) hitCooldown -= dt;
        animTime += dt;
        rotation = (rotation + ROT_SPEED * dt) % 360f;

        frameTimer += dt;
        if (frameTimer >= FRAME_TIME) {
            frameTimer -= FRAME_TIME;
            frameB = !frameB;
        }

        pos.mulAdd(vel, dt);

        if (pos.x < r) { pos.x = r; vel.x *= -1; }
        if (pos.x > W - r) { pos.x = W - r; vel.x *= -1; }
        if (pos.y < r) { pos.y = r; vel.y *= -1; }
        if (pos.y > H - r) { pos.y = H - r; vel.y *= -1; }
    }

    public PhantomType getType() {
        return type;
    }
    public void resetWithAngle(float x, float y, float angleDeg) {
        pos.set(x, y);

        // One consistent launch speed, but random direction
        float speed = (float) Math.sqrt(Const.BALL_SPEED_X * Const.BALL_SPEED_X + Const.BALL_SPEED_Y * Const.BALL_SPEED_Y);
        vel.set(speed, 0f).setAngleDeg(angleDeg);

        animTime = 0f;
        rotation = 0f;
        frameTimer = 0f;
        frameB = MathUtils.randomBoolean();
        r = Const.BALL_START_RADIUS;
    }

    public Vector2 getPos() {
        return pos;
    }

    public float getR() {
        return r;
    }

    public void grow(float amount) {
        r = MathUtils.clamp(r + amount, Const.BALL_MIN_RADIUS, Const.BALL_MAX_RADIUS);
    }

    public void shrink(float amount) {
        r = MathUtils.clamp(r - amount, Const.BALL_MIN_RADIUS, Const.BALL_MAX_RADIUS);
    }

    public boolean isDead() {
        return r <= Const.BALL_MIN_RADIUS + 0.001f;
    }


    private Texture getCurrentTexture() {
        return frameB ? frame2 : frame1;
    }

    public void draw(SpriteBatch batch) {
        Texture tex = getCurrentTexture();

        float pulse = 1f + MathUtils.sin(animTime * PULSE_SPEED) * PULSE_AMPLITUDE;
        float size = r * 2f * pulse;

        float x = pos.x - size * 0.5f;
        float y = pos.y - size * 0.5f;

        batch.draw(
                tex,
                x, y,
                size * 0.5f, size * 0.5f,
                size, size,
                1f, 1f,
                rotation,
                0, 0,
                tex.getWidth(), tex.getHeight(),
                false, false
        );
    }

    public void dispose() {
        frame1.dispose();
        frame2.dispose();
    }
}
