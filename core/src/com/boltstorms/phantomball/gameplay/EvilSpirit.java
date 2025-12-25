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

public class EvilSpirit {

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();

    // Collision radius (HP-driven)
    private float r;

    private PhantomType type;
    private BallStats stats;
    private int level;

    // HP drives size between minRadius..maxRadius
    private float hp;

    // Sprite
    private Texture sprite;

    // Shared textures
    private static Texture BLUE_1, BLUE_2, RED_1, RED_2;
    private static boolean loaded = false;

    // ===================== LOADING =====================

    private static void ensureLoaded() {
        if (loaded) return;

        BLUE_1 = new Texture(Gdx.files.internal("BlueSpirit1.png"));
        BLUE_2 = new Texture(Gdx.files.internal("BlueSpirit2.png"));
        RED_1  = new Texture(Gdx.files.internal("RedSpirit1.png"));
        RED_2  = new Texture(Gdx.files.internal("RedSpirit2.png"));

        loaded = true;
    }

    public static void disposeAll() {
        if (!loaded) return;

        BLUE_1.dispose();
        BLUE_2.dispose();
        RED_1.dispose();
        RED_2.dispose();

        loaded = false;
    }

    // ===================== FACTORY =====================

    public static EvilSpirit randomProp(float W, float H, PhantomType type) {
        ensureLoaded();

        EvilSpirit p = new EvilSpirit();

        int lv = MathUtils.random(1, 5);
        p.init(type, lv);

        // Position based on radius
        p.pos.set(
                MathUtils.random(p.r, W - p.r),
                MathUtils.random(p.r, H - p.r)
        );

        p.randomizeVelocity();
        p.pickSprite();

        return p;
    }

    // ===================== INIT / LEVEL =====================

    private void init(PhantomType type, int level) {
        this.type = type;
        setLevel(level);

        // Start at 50% HP → mid size
        this.hp = stats.maxHp * 0.5f;
        syncRadiusToHp();
    }


    private void setLevel(int newLevel) {
        this.level = MathUtils.clamp(
                newLevel,
                Const.SPIRIT_MIN_LV,
                Const.SPIRIT_MAX_LV
        );
        this.stats = BallProgression.statsFor(type, this.level);
    }


    // ===================== SIZE / HP =====================

    private void syncRadiusToHp() {
        float hpPct = (stats.maxHp <= 0f)
                ? 1f
                : MathUtils.clamp(hp / stats.maxHp, 0f, 1f);

        float rawR = stats.minRadius
                + (stats.maxRadius - stats.minRadius) * hpPct;

        // Spirits slightly smaller than balls
        r = rawR * Const.SPIRIT_SIZE_SCALE;

        // Clamp to level bounds
        r = MathUtils.clamp(
                r,
                stats.minRadius * Const.SPIRIT_SIZE_SCALE,
                stats.maxRadius * Const.SPIRIT_SIZE_SCALE
        );
    }


    public float takeDamage(float amount) {
        float dmg = stats.applyResistance(amount);

        float oldHp = hp;
        hp = MathUtils.clamp(hp - dmg, 0f, stats.maxHp);

        float dealt = oldHp - hp;
        syncRadiusToHp();
        return dealt;
    }



    public void heal(float amount) {
        hp = MathUtils.clamp(hp + amount, 0f, stats.maxHp);
        syncRadiusToHp();
    }

    public boolean isDead() {
        return hp <= 0.001f;
    }

    // ===================== MOVEMENT =====================

    private void randomizeVelocity() {
        float angle = MathUtils.random(0f, 360f);
        vel.set(stats.speed, 0f).setAngleDeg(angle);

        // Safety: never stop moving
        if (vel.len2() < 60f * 60f) {
            vel.setLength(120f);
        }
    }

    public void nudgeVelocity() {
        vel.rotateDeg(MathUtils.random(-25f, 25f));
        vel.setLength(stats.speed);
    }

    public void update(float dt, float W, float H) {
        pos.mulAdd(vel, dt);

        if (pos.x < r) { pos.x = r; vel.x *= -1; }
        if (pos.x > W - r) { pos.x = W - r; vel.x *= -1; }
        if (pos.y < r) { pos.y = r; vel.y *= -1; }
        if (pos.y > H - r) { pos.y = H - r; vel.y *= -1; }
    }

    // ===================== COLLISION =====================

    public boolean collides(Ball ball) {
        float dx = ball.getPos().x - pos.x;
        float dy = ball.getPos().y - pos.y;
        float rr = ball.getR() + r;
        return dx * dx + dy * dy <= rr * rr;
    }

    // Tap hit-test (for level label)
    public boolean containsPoint(float x, float y) {
        float dx = x - pos.x;
        float dy = y - pos.y;
        return dx * dx + dy * dy <= r * r;
    }

    // ===================== RESPAWN =====================

    public void respawn(float W, float H) {
        int lv = MathUtils.random(1, 5);
        init(type, lv);

        pos.set(
                MathUtils.random(r, W - r),
                MathUtils.random(r, H - r)
        );

        randomizeVelocity();
        pickSprite();
    }

    // ===================== RENDER =====================

    private void pickSprite() {
        sprite = (type == PhantomType.BLUE)
                ? (MathUtils.randomBoolean() ? BLUE_1 : BLUE_2)
                : (MathUtils.randomBoolean() ? RED_1 : RED_2);
    }

    public void draw(SpriteBatch batch) {
        float size = r * 2f;
        batch.draw(sprite, pos.x - r, pos.y - r, size, size);
    }

    public void drawDebug(ShapeRenderer sr) {
        if (!Const.DEBUG_DRAW) return;

        if (type == PhantomType.BLUE) {
            sr.setColor(0f, 0.6f, 1f, 1f);
        } else {
            sr.setColor(1f, 0.2f, 0.2f, 1f);
        }

        sr.circle(pos.x, pos.y, r);
    }

    // ===================== GETTERS =====================

    public PhantomType getType() { return type; }
    public int getLevel() { return level; }
    public Vector2 getPos() { return pos; }
    public float getR() { return r; }
}
