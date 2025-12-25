package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.boltstorms.phantomball.gameplay.stats.BallProgression;
import com.boltstorms.phantomball.gameplay.stats.BallStats;
import com.boltstorms.phantomball.util.Const;

public class Ball {

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();

    // Collision radius (HP-driven)
    private float r;

    private final PhantomType type;
    private BallStats stats;

    // HP drives size between stats.minRadius..stats.maxRadius for the level
    private float hp;

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

        // Start at half HP -> mid size between min/max radius
        this.hp = stats.maxHp * 0.5f;
        syncRadiusToHp();

        if (type == PhantomType.BLUE) {
            frame1 = new Texture(Gdx.files.internal("PhantomPlayer.png"));
            frame2 = new Texture(Gdx.files.internal("PhantomPlayer.png"));
        } else {
            frame1 = new Texture(Gdx.files.internal("PhantomPlayerPink.png"));
            frame2 = new Texture(Gdx.files.internal("PhantomPlayerPink.png"));
        }

        frame1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        frame2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        printStats("SPAWN");
    }

    private void syncRadiusToHp() {
        float hpPct = (stats.maxHp <= 0f) ? 1f : MathUtils.clamp(hp / stats.maxHp, 0f, 1f);

        // Map HP% -> [minRadius, maxRadius] for this level
        float rawR = stats.minRadius + (stats.maxRadius - stats.minRadius) * hpPct;

        // Apply collision scaling + master scaling
        r = rawR * Const.BALL_COLLISION_SCALE * Const.BALL_SIZE_SCALE;

        // Safety clamp to this level's bounds
        float minClamp = stats.minRadius * Const.BALL_COLLISION_SCALE * Const.BALL_SIZE_SCALE;
        float maxClamp = stats.maxRadius * Const.BALL_COLLISION_SCALE * Const.BALL_SIZE_SCALE;
        r = MathUtils.clamp(r, minClamp, maxClamp);
    }

    public void setLevel(int level) {
        BallStats old = stats;
        stats = BallProgression.statsFor(type, level);

        // keep HP percentage across level-up
        float pct = (old.maxHp <= 0f) ? 1f : MathUtils.clamp(hp / old.maxHp, 0f, 1f);
        hp = pct * stats.maxHp;

        syncRadiusToHp();
        printStats("LEVEL UP -> " + level);
    }

    public int getLevel() { return stats.level; }
    public PhantomType getType() { return type; }

    public Vector2 getPos() { return pos; }
    public float getR() { return r; }

    public boolean canBeHit() { return hitCooldown <= 0f; }
    public void triggerHitCooldown(float seconds) { hitCooldown = seconds; }

    public float getHp() { return hp; }
    public float getMaxHp() { return stats.maxHp; }

    public float getAttack() { return stats.attack; }
    public float getResistance() { return stats.resistance; }

    public int getXpToNext() { return stats.xpToNext; }

    public void heal(float amount) {
        hp = MathUtils.clamp(hp + amount, 0f, stats.maxHp);
        syncRadiusToHp();
        printHp("HEAL");
    }

    public void takeDamage(float amount) {
        float dmg = stats.applyResistance(amount);
        hp = MathUtils.clamp(hp - dmg, 0f, stats.maxHp);
        syncRadiusToHp();
        printHp("DMG");
    }

    public boolean isDead() {
        return hp <= 0.001f;
    }

    // "grow" is just healing in this HP-driven size system
    public void grow(float amount) {
        heal(amount);
    }

    public void resetWithAngle(float x, float y, float angleDeg) {
        pos.set(x, y);
        vel.set(stats.speed, 0f).setAngleDeg(angleDeg);

        animTime = 0f;
        rotation = 0f;
        frameTimer = 0f;
        frameB = MathUtils.randomBoolean();

        // Reset to half HP -> mid size
        hp = stats.maxHp * 0.5f;
        syncRadiusToHp();

        printStats("RESET");
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

        // Sprite size uses r but still respects your sprite scale
        float size = r * 2f * pulse * Const.BALL_SPRITE_SCALE;

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

    public void drawDebug(ShapeRenderer sr) {
        if (!Const.DEBUG_DRAW) return;
        sr.setColor(1f, 1f, 0f, 1f);
        sr.circle(pos.x, pos.y, r);
    }

    public void dispose() {
        frame1.dispose();
        frame2.dispose();
    }

    private void printStats(String reason) {
        System.out.println(
                "[" + reason + "] " + type
                        + " LV " + stats.level
                        + " HP " + String.format("%.1f", hp) + "/" + String.format("%.1f", stats.maxHp)
                        + " ATK " + String.format("%.1f", stats.attack)
                        + " RES " + String.format("%.2f", stats.resistance)
                        + " SPD " + String.format("%.1f", stats.speed)
                        + " minR " + String.format("%.1f", stats.minRadius)
                        + " maxR " + String.format("%.1f", stats.maxRadius)
                        + " xpNext " + stats.xpToNext
        );
    }

    private void printHp(String reason) {
        System.out.println(
                "[HP " + reason + "] " + type
                        + " LV " + stats.level
                        + " HP " + String.format("%.2f", hp)
                        + "/" + String.format("%.2f", stats.maxHp)
                        + " (R=" + String.format("%.1f", r) + ")"
        );
    }
}
