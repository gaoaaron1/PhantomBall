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
    private boolean ghostProp;

    // NEW: which sprite this prop uses
    private Texture sprite;

    // NEW: shared textures (loaded once)
    private static Texture BLUE_1, BLUE_2, RED_1, RED_2;
    private static boolean loaded = false;

    private static void ensureLoaded() {
        if (loaded) return;

        BLUE_1 = new Texture(Gdx.files.internal("BlueSpirit1.png"));
        BLUE_2 = new Texture(Gdx.files.internal("BlueSpirit2.png"));
        RED_1  = new Texture(Gdx.files.internal("RedSpirit1.png"));
        RED_2  = new Texture(Gdx.files.internal("RedSpirit2.png"));

        BLUE_1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        BLUE_2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        RED_1.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        RED_2.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        loaded = true;
    }

    // Call from WorldController.dispose()
    public static void disposeAll() {
        if (!loaded) return;
        BLUE_1.dispose();
        BLUE_2.dispose();
        RED_1.dispose();
        RED_2.dispose();
        loaded = false;
    }

    // Factory method for creating a random prop
    public static Prop randomProp(float W, float H, boolean ghostProp) {
        ensureLoaded();

        Prop p = new Prop();
        p.ghostProp = ghostProp;

        p.r = MathUtils.random(Const.PROP_MIN_RADIUS, Const.PROP_MAX_RADIUS);
        p.pos.set(
                MathUtils.random(p.r, W - p.r),
                MathUtils.random(p.r, H - p.r)
        );
        p.vel.set(
                MathUtils.random(-160f, 160f),
                MathUtils.random(-160f, 160f)
        );

        // Prevent very slow props
        if (p.vel.len2() < 70f * 70f) {
            p.vel.set(140f, 90f);
        }

        p.pickRandomSprite();
        p.clampSpeed();
        return p;
    }

    private void pickRandomSprite() {
        // ghostProp true = RED, ghostProp false = BLUE (matching your old behavior mapping)
        if (ghostProp) {
            sprite = MathUtils.randomBoolean() ? RED_1 : RED_2;
        } else {
            sprite = MathUtils.randomBoolean() ? BLUE_1 : BLUE_2;
        }
    }

    public boolean isGhostProp() {
        return ghostProp;
    }

    // Small random change to direction (zero-G chaos)
    public void nudgeVelocity() {
        vel.rotateDeg(MathUtils.random(-25f, 25f));
        clampSpeed();
    }

    private void clampSpeed() {
        float speed = vel.len();
        speed = MathUtils.clamp(speed, Const.PROP_MIN_SPEED, Const.PROP_MAX_SPEED);

        if (speed < 1f) speed = Const.PROP_MIN_SPEED;
        vel.setLength(speed);
    }

    public void update(float dt, float W, float H) {
        pos.mulAdd(vel, dt);

        // Bounce off walls
        if (pos.x < r) { pos.x = r; vel.x *= -1; }
        if (pos.x > W - r) { pos.x = W - r; vel.x *= -1; }
        if (pos.y < r) { pos.y = r; vel.y *= -1; }
        if (pos.y > H - r) { pos.y = H - r; vel.y *= -1; }
    }

    public boolean collides(Ball ball) {
        float dx = ball.getPos().x - pos.x;
        float dy = ball.getPos().y - pos.y;
        float rr = ball.getR() + r;
        return (dx * dx + dy * dy) <= (rr * rr);
    }

    // Respawn after collision
    public void respawn(float W, float H) {
        pos.set(
                MathUtils.random(r, W - r),
                MathUtils.random(r, H - r)
        );
        vel.set(
                MathUtils.random(-160f, 160f),
                MathUtils.random(-160f, 160f)
        );

        if (vel.len2() < 70f * 70f) {
            vel.set(140f, 90f);
        }

        pickRandomSprite();
        clampSpeed();
    }

    // NEW: draw using SpriteBatch
    public void draw(SpriteBatch batch) {
        if (sprite == null) return;

        float size = r * 2f;
        float x = pos.x - r;
        float y = pos.y - r;

        // blue props should be translucent (like your old cyan alpha)
        if (!ghostProp) {
            batch.setColor(1f, 1f, 1f, 0.30f);
        } else {
            batch.setColor(1f, 1f, 1f, 1f);
        }

        batch.draw(sprite, x, y, size, size);

        // always reset so it doesn’t affect other draws
        batch.setColor(1f, 1f, 1f, 1f);
    }
}
