package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.boltstorms.phantomball.util.Const;

public class Prop {

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();

    private float r;
    private boolean ghostProp;

    // Factory method for creating a random prop
    public static Prop randomProp(float W, float H, boolean ghostProp) {
        Prop p = new Prop();
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

        p.ghostProp = ghostProp;
        p.clampSpeed();
        return p;
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

    // NEW: Respawn after successful (safe) collision
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

        clampSpeed();
    }

    public void draw(ShapeRenderer sr) {
        // Solid props are opaque, ghost props are translucent
        if (ghostProp) {
            sr.setColor(1f, 0.4f, 0.55f, 1f);    // solid pink/red
        } else {
            sr.setColor(0.55f, 0.85f, 1f, 0.30f); // translucent cyan
        }

        sr.circle(pos.x, pos.y, r);
    }
}
