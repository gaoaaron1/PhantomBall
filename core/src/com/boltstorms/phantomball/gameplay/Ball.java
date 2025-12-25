package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.boltstorms.phantomball.gameplay.stats.BallProgression;
import com.boltstorms.phantomball.gameplay.stats.BallStats;
import com.boltstorms.phantomball.util.Const;

public class Ball {

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();

    private float r = Const.BALL_START_RADIUS;

    private final PhantomType type;
    private BallStats stats;

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

    public Ball(PhantomType type, int level) {
        this.type = type;
        this.stats = BallProgression.statsFor(type, level);

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

    public void setLevel(int level) {
        stats = BallProgression.statsFor(type, level);
        r = MathUtils.clamp(r, Const.BALL_MIN_RADIUS, stats.maxRadius);
    }

    public int getLevel() {
        return stats.level;
    }

    public PhantomType getType() {
        return type;
    }

    public Vector2 getPos() { return pos; }
    public float getR() { return r; }

    public boolean canBeHit() { return hitCooldown <= 0f; }
    public void triggerHitCooldown(float seconds) { hitCooldown = seconds; }

    public float getDamageToProp() { return stats.damageToProp; }

    public void growOnCorrectHit() {
        r = MathUtils.clamp(r + stats.growAmount, Const.BALL_MIN_RADIUS, stats.maxRadius);
    }

    public void takeWrongHit() {
        float dmg = stats.applyResistance(stats.shrinkAmount);
        r = MathUtils.clamp(r - dmg, Const.BALL_MIN_RADIUS, stats.maxRadius);
    }

    public boolean isDead() {
        return r <= Const.BALL_MIN_RADIUS + 0.001f;
    }
    // Smooth growth per second (for drain-style collision)
    public void grow(float amount) {
        r = MathUtils.clamp(r + amount, Const.BALL_MIN_RADIUS, stats.maxRadius);
    }

    // Smooth damage per second (for drain-style collision)
    public void shrink(float amount) {
        r = MathUtils.clamp(r - amount, Const.BALL_MIN_RADIUS, stats.maxRadius);
    }

    public void resetWithAngle(float x, float y, float angleDeg) {
        pos.set(x, y);
        vel.set(stats.speed, 0f).setAngleDeg(angleDeg);

        animTime = 0f;
        rotation = 0f;
        frameTimer = 0f;
        frameB = MathUtils.randomBoolean();

        r = Const.BALL_START_RADIUS;
        r = MathUtils.clamp(r, Const.BALL_MIN_RADIUS, stats.maxRadius);
    }

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
