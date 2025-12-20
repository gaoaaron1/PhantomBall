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

    private float blueSpawnX = 0f;
    private float redSpawnX  = 0f;
    private float spawnY     = 0f; // in WORLD coords (play area coords)


    public void resize(int width, int height) {
        W = width;
        H = height;
        reset();
    }
    public void setSummonAnchors(float blueX, float redX, float y) {
        this.blueSpawnX = blueX;
        this.redSpawnX = redX;
        this.spawnY = y;
    }
    public void summonBlue() {
        if (blueUsed) return;
        blueUsed = true;

        blueBall = new Ball(PhantomType.BLUE);

        // Use anchor if set, otherwise fallback
        float x = (blueSpawnX > 0f) ? blueSpawnX : W * 0.35f;
        float y = (spawnY > 0f) ? spawnY : H * 0.15f;

        // Small jitter so it feels like it "spawns from the card"
        x += com.badlogic.gdx.math.MathUtils.random(-25f, 25f);
        y += com.badlogic.gdx.math.MathUtils.random(0f, 20f);

        // Clamp so it doesn't spawn inside walls
        float margin = Const.BALL_START_RADIUS + 6f;
        x = com.badlogic.gdx.math.MathUtils.clamp(x, margin, W - margin);
        y = com.badlogic.gdx.math.MathUtils.clamp(y, margin, H - margin);

        blueBall.reset(x, y);
    }

    public void summonRed() {
        if (redUsed) return;
        redUsed = true;

        redBall = new Ball(PhantomType.RED);

        float x = (redSpawnX > 0f) ? redSpawnX : W * 0.65f;
        float y = (spawnY > 0f) ? spawnY : H * 0.15f;

        x += com.badlogic.gdx.math.MathUtils.random(-25f, 25f);
        y += com.badlogic.gdx.math.MathUtils.random(0f, 20f);

        float margin = Const.BALL_START_RADIUS + 6f;
        x = com.badlogic.gdx.math.MathUtils.clamp(x, margin, W - margin);
        y = com.badlogic.gdx.math.MathUtils.clamp(y, margin, H - margin);

        redBall.reset(x, y);
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

            // ===================== BLUE BALL =====================
            if (blueBall != null && p.collides(blueBall)) {

                if (p.getType() == PhantomType.BLUE) {
                    // correct hit -> grow + damage spirit
                    score++;
                    blueBall.grow(Const.BALL_GROW_AMOUNT);

                    // Spirit shrinks until it "dies", then respawns
                    p.shrink(Const.PROP_HIT_SHRINK);
                    if (p.isDead()) {
                        p.respawn(W, H);
                    }

                } else {
                    // wrong hit -> ball takes damage, spirit unchanged
                    float dmg = scaledDamage(p);
                    blueBall.shrink(dmg);

                    if (blueBall.isDead()) {
                        blueBall.dispose();
                        blueBall = null;
                        blueUsed = false; // card reactivates
                    }
                }
            }

            // ===================== RED BALL =====================
            if (redBall != null && p.collides(redBall)) {

                if (p.getType() == PhantomType.RED) {
                    // correct hit -> grow + damage spirit
                    score++;
                    redBall.grow(Const.BALL_GROW_AMOUNT);

                    // Spirit shrinks until it "dies", then respawns
                    p.shrink(Const.PROP_HIT_SHRINK);
                    if (p.isDead()) {
                        p.respawn(W, H);
                    }

                } else {
                    // wrong hit -> ball takes damage, spirit unchanged
                    float dmg = scaledDamage(p);
                    redBall.shrink(dmg);

                    if (redBall.isDead()) {
                        redBall.dispose();
                        redBall = null;
                        redUsed = false; // card reactivates
                    }
                }
            }
        }
    }

        // You can delete this now if you no longer do wrong-color damage.
    private float scaledDamage(Prop p) {
        float t = (p.getRadius() - Const.PROP_MIN_RADIUS) / (Const.PROP_MAX_RADIUS - Const.PROP_MIN_RADIUS);
        t = Math.max(0f, Math.min(1f, t));
        float multiplier = 1.0f + t * 1.0f;
        return Const.BALL_SHRINK_AMOUNT * multiplier;
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

    public boolean isRedUsed() {
        return redUsed;
    }

    public void setPlayBounds(float width, float height) {
        W = width;
        H = height;
    }

    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isPaused() { return paused; }

    public void dispose() {
        if (blueBall != null) blueBall.dispose();
        if (redBall != null) redBall.dispose();
        Prop.disposeAll();
    }
}
