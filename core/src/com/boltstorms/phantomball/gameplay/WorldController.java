package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.boltstorms.phantomball.util.Const;
import com.boltstorms.phantomball.util.PlayerProfile;

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
    private float spawnY     = 0f;

    // NEW: track “correct hits” for leveling
    private int blueCorrectHits = 0;
    private int redCorrectHits  = 0;

    // NEW: how many correct hits needed per level
    private static final int HITS_PER_LEVEL = 10;

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

        blueBall = new Ball(PhantomType.BLUE, PlayerProfile.getBlueLevel());

        float x = (blueSpawnX > 0f) ? blueSpawnX : W * 0.35f;
        float y = (spawnY > 0f) ? spawnY : H * 0.12f;

        x += com.badlogic.gdx.math.MathUtils.random(-25f, 25f);
        y += com.badlogic.gdx.math.MathUtils.random(0f, 20f);

        float margin = Const.BALL_START_RADIUS + 6f;
        x = com.badlogic.gdx.math.MathUtils.clamp(x, margin, W - margin);
        y = com.badlogic.gdx.math.MathUtils.clamp(y, margin, H - margin);

        float angle = com.badlogic.gdx.math.MathUtils.random(35f, 145f);
        blueBall.resetWithAngle(x, y, angle);
    }

    public void summonRed() {
        if (redUsed) return;
        redUsed = true;

        redBall = new Ball(PhantomType.RED, PlayerProfile.getRedLevel());

        float x = (redSpawnX > 0f) ? redSpawnX : W * 0.65f;
        float y = (spawnY > 0f) ? spawnY : H * 0.12f;

        x += com.badlogic.gdx.math.MathUtils.random(-25f, 25f);
        y += com.badlogic.gdx.math.MathUtils.random(0f, 20f);

        float margin = Const.BALL_START_RADIUS + 6f;
        x = com.badlogic.gdx.math.MathUtils.clamp(x, margin, W - margin);
        y = com.badlogic.gdx.math.MathUtils.clamp(y, margin, H - margin);

        float angle = com.badlogic.gdx.math.MathUtils.random(35f, 145f);
        redBall.resetWithAngle(x, y, angle);
    }

    private void reset() {
        score = 0;
        driftTimer = 0f;

        // NOTE: We do NOT reset PlayerProfile levels here
        // (we do reset the “during-run” hit counters)
        blueCorrectHits = 0;
        redCorrectHits = 0;

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

    private void onBlueCorrectHit() {
        blueCorrectHits++;

        if (blueCorrectHits % HITS_PER_LEVEL == 0) {
            int newLevel = PlayerProfile.levelUpBlue();
            if (blueBall != null) blueBall.setLevel(newLevel);
        }
    }

    private void onRedCorrectHit() {
        redCorrectHits++;

        if (redCorrectHits % HITS_PER_LEVEL == 0) {
            int newLevel = PlayerProfile.levelUpRed();
            if (redBall != null) redBall.setLevel(newLevel);
        }
    }

    public void update(float dt) {
        if (paused) return;

        if (blueBall != null) blueBall.update(dt, W, H);
        if (redBall != null)  redBall.update(dt, W, H);

        driftTimer += dt;
        boolean nudge = driftTimer >= Const.DRIFT_NUDGE_TIME;
        if (nudge) driftTimer = 0f;

        for (Prop p : props) {
            if (nudge) p.nudgeVelocity();
            p.update(dt, W, H);

            // ===================== BLUE BALL =====================
            if (blueBall != null && p.collides(blueBall)) {

                if (p.getType() == PhantomType.BLUE) {
                    // smooth drain (correct)
                    float drain = Const.PROP_DRAIN_RATE * dt;
                    p.shrink(drain);

                    blueBall.grow(Const.BALL_GROW_RATE * dt);

                    if (p.isDead()) {
                        score++;
                        onBlueCorrectHit();
                        p.respawn(W, H);
                    }

                } else {
                    // smooth damage (wrong)
                    blueBall.shrink(Const.BALL_DAMAGE_RATE * dt);

                    if (blueBall.isDead()) {
                        blueBall.dispose();
                        blueBall = null;
                        blueUsed = false;
                    }
                }
            }

            // ===================== RED BALL =====================
            if (redBall != null && p.collides(redBall)) {

                if (p.getType() == PhantomType.RED) {
                    // smooth drain (correct)
                    float drain = Const.PROP_DRAIN_RATE * dt;
                    p.shrink(drain);

                    redBall.grow(Const.BALL_GROW_RATE * dt);

                    if (p.isDead()) {
                        score++;
                        onRedCorrectHit();
                        p.respawn(W, H);
                    }

                } else {
                    // smooth damage (wrong)
                    redBall.shrink(Const.BALL_DAMAGE_RATE * dt);

                    if (redBall.isDead()) {
                        redBall.dispose();
                        redBall = null;
                        redUsed = false;
                    }
                }
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

    public int getScore() { return score; }
    public boolean isBlueUsed() { return blueUsed; }
    public boolean isRedUsed() { return redUsed; }

    // NEW: let GameScreen show levels on cards
    public int getBlueLevel() { return PlayerProfile.getBlueLevel(); }
    public int getRedLevel()  { return PlayerProfile.getRedLevel(); }

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
