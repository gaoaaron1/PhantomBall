package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.boltstorms.phantomball.util.Const;

public class Prop {

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();
    private float r;

    private PhantomType type;
    private Texture sprite;

    private static Texture BLUE_1, BLUE_2, RED_1, RED_2;
    private static boolean loaded = false;

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

    public static Prop randomProp(float W, float H, PhantomType type) {
        ensureLoaded();

        Prop p = new Prop();
        p.type = type;

        p.r = MathUtils.random(Const.PROP_MIN_RADIUS, Const.PROP_MAX_RADIUS);
        p.pos.set(MathUtils.random(p.r, W - p.r), MathUtils.random(p.r, H - p.r));
        p.vel.set(MathUtils.random(-160f, 160f), MathUtils.random(-160f, 160f));

        if (p.vel.len2() < 70f * 70f) p.vel.set(140f, 90f);

        p.pickSprite();
        p.clampSpeed();
        return p;
    }

    private void pickSprite() {
        sprite = (type == PhantomType.BLUE)
                ? (MathUtils.randomBoolean() ? BLUE_1 : BLUE_2)
                : (MathUtils.randomBoolean() ? RED_1 : RED_2);
    }

    public PhantomType getType() {
        return type;
    }

    public void nudgeVelocity() {
        vel.rotateDeg(MathUtils.random(-25f, 25f));
        clampSpeed();
    }

    private void clampSpeed() {
        float speed = MathUtils.clamp(vel.len(), Const.PROP_MIN_SPEED, Const.PROP_MAX_SPEED);
        vel.setLength(speed);
    }

    public void update(float dt, float W, float H) {
        pos.mulAdd(vel, dt);

        if (pos.x < r) { pos.x = r; vel.x *= -1; }
        if (pos.x > W - r) { pos.x = W - r; vel.x *= -1; }
        if (pos.y < r) { pos.y = r; vel.y *= -1; }
        if (pos.y > H - r) { pos.y = H - r; vel.y *= -1; }
    }

    public boolean collides(Ball ball) {
        float dx = ball.getPos().x - pos.x;
        float dy = ball.getPos().y - pos.y;
        float rr = ball.getR() + r;
        return dx * dx + dy * dy <= rr * rr;
    }

    public void respawn(float W, float H) {
        pos.set(MathUtils.random(r, W - r), MathUtils.random(r, H - r));
        vel.set(MathUtils.random(-160f, 160f), MathUtils.random(-160f, 160f));
        if (vel.len2() < 70f * 70f) vel.set(140f, 90f);
        pickSprite();
        clampSpeed();
    }

    public void draw(SpriteBatch batch) {
        float size = r * 2f;
        batch.draw(sprite, pos.x - r, pos.y - r, size, size);
    }
}
