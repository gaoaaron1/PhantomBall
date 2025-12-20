package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.boltstorms.phantomball.util.Const;

import java.util.ArrayList;

public class WorldController {

    private float W, H;

    private Ball blueBall;
    private Ball redBall;

    private boolean blueUsed = false;
    private boolean redUsed = false;

    private final ArrayList<Prop> props = new ArrayList<>();

    private float driftTimer = 0f;
    private boolean paused = false;
    private int score = 0;

    public void resize(int width, int height) {
        W = width;
        H = height;
        reset();
    }

    private void reset() {
        score = 0;
        driftTimer = 0f;

        if (blueBall != null) blueBall.dispose();
        if (redBall != null) redBall.dispose();

        blueBall = null;
        redBall = null;
        blueUsed = false;
        redUsed = false;

        props.clear();
        for (int i = 0; i < 10; i++) {
            PhantomType t = (i % 2 == 0) ? PhantomType.BLUE : PhantomType.RED;
            props.add(Prop.randomProp(W, H, t));
        }
    }

    public void summonBlue() {
        if (blueUsed) return;
        blueUsed = true;
        blueBall = new Ball(PhantomType.BLUE);
        blueBall.reset(W * 0.35f, H * 0.5f);
    }

    public void summonRed() {
        if (redUsed) return;
        redUsed = true;
        redBall = new Ball(PhantomType.RED);
        redBall.reset(W * 0.65f, H * 0.5f);
    }

    public void update(float dt) {
        if (paused) return;

        if (blueBall != null) blueBall.update(dt, W, H);
        if (redBall != null) redBall.update(dt, W, H);

        driftTimer += dt;
        boolean nudge = driftTimer >= Const.DRIFT_NUDGE_TIME;
        if (nudge) driftTimer = 0f;

        for (Prop p : props) {
            if (nudge) p.nudgeVelocity();
            p.update(dt, W, H);

            if (blueBall != null && p.collides(blueBall) && p.getType() == PhantomType.BLUE) {
                score++;
                p.respawn(W, H);
            }

            if (redBall != null && p.collides(redBall) && p.getType() == PhantomType.RED) {
                score++;
                p.respawn(W, H);
            }
        }
    }

    public void draw(ShapeRenderer sr, SpriteBatch batch) {
        batch.begin();
        for (Prop p : props) p.draw(batch);
        if (blueBall != null) blueBall.draw(batch);
        if (redBall != null) redBall.draw(batch);
        batch.end();
    }

    public int getScore() {
        return score;
    }

    public boolean isBlueUsed() {
        return blueUsed;
    }
    public void setPlayBounds(float width, float height) {
        W = width;
        H = height;
    }

    public boolean isRedUsed() {
        return redUsed;
    }
    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isPaused() { return paused; }

    public void dispose() {
        if (blueBall != null) blueBall.dispose();
        if (redBall != null) redBall.dispose();
        Prop.disposeAll();
    }
}
