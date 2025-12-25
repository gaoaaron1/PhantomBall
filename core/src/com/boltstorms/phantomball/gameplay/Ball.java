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

    // NEW: HP
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

    // HP -> radius mapping (tweak these)
    private static final float MIN_HP_SCALE = 0.55f; // near-death size
    private static final float MAX_HP_SCALE = 1.00f; // full-health size

    public Ball(PhantomType type, int level) {
        this.type = type;
        this.stats = BallProgression.statsFor(type, level);

        this.hp = stats.maxHp;
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
        float scale = MIN_HP_SCALE + (MAX_HP_SCALE - MIN_HP_SCALE) * hpPct;

        r = stats.maxRadius * scale;
        r = MathUtils.clamp(r, Const.BALL_MIN_RADIUS, stats.maxRadius);
    }

    public void setLevel(int level) {
        BallStats old = stats;
        stats = BallProgression.statsFor(type, level);

        float pct = (old.maxHp <= 0f) ? 1f : (hp / old.maxHp);
        hp = MathUtils.clamp(pct * stats.maxHp, 0f, stats.maxHp);

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

    public void printStats(String reason) {
        System.out.println(
                "[" + reason + "] " + type
                        + " LV " + stats.level
                        + " HP " + String.format("%.1f", hp) + "/" + String.format("%.1f", stats.maxHp)
                        + " ATK " + String.format("%.1f", stats.attack)
                        + " RES " + String.format("%.2f", stats.resistance)
                        + " SPD " + String.format("%.1f", stats.speed)
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

    // IMPORTANT: since radius is HP-driven, treat "grow" as healing
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

        hp = stats.maxHp;
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
