package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.boltstorms.phantomball.util.Const;

import java.util.ArrayList;

public class WorldController {

    private float W = 0, H = 0;

    private final Ball ball = new Ball();
    private final ArrayList<Prop> props = new ArrayList<>();

    private boolean dead = false;
    private float driftTimer = 0f;

    private int score = 0;

    // Called once when screen size is known
    public void resize(int width, int height) {
        W = width;
        H = height;
        reset();
    }

    // Reset entire game state
    private void reset() {
        dead = false;
        driftTimer = 0f;
        score = 0;

        ball.reset(W * 0.5f, H * 0.5f);

        props.clear();
        for (int i = 0; i < 10; i++) {
            props.add(Prop.randomProp(W, H, i % 2 == 0));
        }
    }

    // Tap input
    public void onTap() {
        if (dead) {
            reset();
        } else {
            ball.toggleGhost(); // switch red <-> blue
        }
    }

    // Main update loop
    public void update(float dt) {
        if (W <= 0 || H <= 0) return;
        if (dead) return;

        ball.update(dt, W, H);

        driftTimer += dt;
        boolean doNudge = driftTimer >= Const.DRIFT_NUDGE_TIME;
        if (doNudge) driftTimer = 0f;

        for (Prop p : props) {
            if (doNudge) p.nudgeVelocity();
            p.update(dt, W, H);

            if (p.collides(ball)) {

                boolean sameColor =
                        (ball.isGhost() && p.isGhostProp()) ||
                                (!ball.isGhost() && !p.isGhostProp());

                if (sameColor) {
                    score++;
                    ball.grow(Const.ABSORB_GROWTH);
                    p.respawn(W, H);
                } else {
                    ball.shrink(Const.DAMAGE_SHRINK);
                    p.respawn(W, H);

                    if (ball.isDeadBySize()) {
                        dead = true;
                        break;
                    }
                }
            }
        }
    }

    // Draw everything
    public void draw(ShapeRenderer sr) {
        for (Prop p : props) {
            p.draw(sr);
        }

        ball.draw(sr);

        if (dead) {
            sr.setColor(0f, 0f, 0f, 0.35f);
            sr.rect(0, 0, W, H);
        }
    }

    // ===== HUD GETTERS =====

    public int getScore() {
        return score;
    }

    public boolean isGhostState() {
        return ball.isGhost();
    }

    public boolean isDead() {
        return dead;
    }

    public float getBallRadius() {
        return ball.getR();
    }

    // ✅ NEW: allow GameScreen to draw name above ball
    public Vector2 getBallPos() {
        return ball.getPos();
    }
}
