package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.boltstorms.phantomball.util.Const;

public class Ball {

    private final Vector2 pos = new Vector2();
    private final Vector2 vel = new Vector2();

    private float r = Const.BALL_START_RADIUS;
    private boolean ghost = false;

    public void reset(float x, float y) {
        pos.set(x, y);
        vel.set(Const.BALL_SPEED_X, Const.BALL_SPEED_Y).rotateDeg(MathUtils.random(-25f, 25f));
        ghost = false;
        r = Const.BALL_START_RADIUS;
    }

    public void toggleGhost() {
        ghost = !ghost;
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

    // NEW: grow/shrink
    public void grow(float amount) {
        r = MathUtils.clamp(r + amount, Const.BALL_MIN_RADIUS, Const.BALL_MAX_RADIUS);
    }

    public void shrink(float amount) {
        r = MathUtils.clamp(r - amount, Const.BALL_MIN_RADIUS, Const.BALL_MAX_RADIUS);
    }

    public boolean isDeadBySize() {
        return r <= Const.BALL_MIN_RADIUS + 0.001f;
    }

    public void update(float dt, float W, float H) {
        pos.mulAdd(vel, dt);

        // Bounce walls (use current radius!)
        if (pos.x < r) { pos.x = r; vel.x *= -1; }
        if (pos.x > W - r) { pos.x = W - r; vel.x *= -1; }
        if (pos.y < r) { pos.y = r; vel.y *= -1; }
        if (pos.y > H - r) { pos.y = H - r; vel.y *= -1; }
    }

    public void draw(ShapeRenderer sr) {
        // Your latest request: ball switches between RED and BLUE on tap
        if (ghost) sr.setColor(0.55f, 0.85f, 1f, 1f); // blue
        else sr.setColor(1f, 0.4f, 0.55f, 1f);        // red

        sr.circle(pos.x, pos.y, r);
    }
}
