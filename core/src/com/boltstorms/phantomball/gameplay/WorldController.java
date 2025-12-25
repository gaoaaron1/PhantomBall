package com.boltstorms.phantomball.gameplay;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.boltstorms.phantomball.util.Const;
import com.boltstorms.phantomball.util.PlayerProfile;

import java.util.ArrayList;

public class WorldController {

    private float W, H;

    private Ball blueBall;
    private Ball redBall;

    private boolean blueUsed = false;
    private boolean redUsed = false;

    private final ArrayList<EvilSpirit> evilspirits = new ArrayList<>();

    private float driftTimer = 0f;
    private boolean paused = false;
    private int score = 0;

    private float blueSpawnX = 0f;
    private float redSpawnX  = 0f;
    private float spawnY     = 0f;

    // XP tracking (per run) — now float so we can gain XP continuously per damage dealt
    private float blueXp = 0f;
    private float redXp  = 0f;

    // ===== Spirit selection for label =====
    private EvilSpirit selectedSpirit = null;
    private float selectedTimer = 0f;
    private static final float SELECT_SHOW_TIME = 2.5f;

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

        x += MathUtils.random(-25f, 25f);
        y += MathUtils.random(0f, 20f);

        float margin = Const.BALL_START_RADIUS + 6f;
        x = MathUtils.clamp(x, margin, W - margin);
        y = MathUtils.clamp(y, margin, H - margin);

        float angle = MathUtils.random(35f, 145f);
        blueBall.resetWithAngle(x, y, angle);
    }

    public void summonRed() {
        if (redUsed) return;
        redUsed = true;

        redBall = new Ball(PhantomType.RED, PlayerProfile.getRedLevel());

        float x = (redSpawnX > 0f) ? redSpawnX : W * 0.65f;
        float y = (spawnY > 0f) ? spawnY : H * 0.12f;

        x += MathUtils.random(-25f, 25f);
        y += MathUtils.random(0f, 20f);

        float margin = Const.BALL_START_RADIUS + 6f;
        x = MathUtils.clamp(x, margin, W - margin);
        y = MathUtils.clamp(y, margin, H - margin);

        float angle = MathUtils.random(35f, 145f);
        redBall.resetWithAngle(x, y, angle);
    }

    private void reset() {
        score = 0;
        driftTimer = 0f;

        blueXp = 0f;
        redXp  = 0f;

        if (blueBall != null) blueBall.dispose();
        if (redBall != null) redBall.dispose();

        blueBall = null;
        redBall = null;
        blueUsed = false;
        redUsed = false;

        evilspirits.clear();

        selectedSpirit = null;
        selectedTimer = 0f;

        for (int i = 0; i < 10; i++) {
            PhantomType t = (i % 2 == 0) ? PhantomType.BLUE : PhantomType.RED;
            evilspirits.add(EvilSpirit.randomProp(W, H, t));
        }
    }

    // ===== Tap selection (used by GameScreen) =====
    public boolean tapAt(float worldX, float worldY) {
        for (int i = evilspirits.size() - 1; i >= 0; i--) {
            EvilSpirit p = evilspirits.get(i);
            if (p.containsPoint(worldX, worldY)) {
                selectedSpirit = p;
                selectedTimer = SELECT_SHOW_TIME;
                return true;
            }
        }
        selectedSpirit = null;
        selectedTimer = 0f;
        return false;
    }

    public EvilSpirit getSelectedSpirit() { return selectedSpirit; }

    // ===== XP getters for HUD =====
    public float getBlueXp() { return blueXp; }
    public float getRedXp()  { return redXp;  }

    public int getBlueXpToNext() {
        if (blueBall != null) return blueBall.getXpToNext();
        // fallback to progression curve
        return 10 + (PlayerProfile.getBlueLevel() - 1) * 5;
    }

    public int getRedXpToNext() {
        if (redBall != null) return redBall.getXpToNext();
        return 10 + (PlayerProfile.getRedLevel() - 1) * 5;
    }

    // ===== XP + Leveling helpers (BALLS ONLY) =====
    private void addBlueXp(float amount) {
        if (blueBall == null) return;

        blueXp += amount;

        while (blueBall != null && blueXp >= blueBall.getXpToNext()) {
            blueXp -= blueBall.getXpToNext();
            int newLevel = PlayerProfile.levelUpBlue();
            blueBall.setLevel(newLevel);
        }
    }

    private void addRedXp(float amount) {
        if (redBall == null) return;

        redXp += amount;

        while (redBall != null && redXp >= redBall.getXpToNext()) {
            redXp -= redBall.getXpToNext();
            int newLevel = PlayerProfile.levelUpRed();
            redBall.setLevel(newLevel);
        }
    }

    public void update(float dt) {
        if (paused) return;

        // selection timer
        if (selectedTimer > 0f) {
            selectedTimer -= dt;
            if (selectedTimer <= 0f) {
                selectedTimer = 0f;
                selectedSpirit = null;
            }
        }

        if (blueBall != null) blueBall.update(dt, W, H);
        if (redBall != null)  redBall.update(dt, W, H);

        driftTimer += dt;
        boolean nudge = driftTimer >= Const.DRIFT_NUDGE_TIME;
        if (nudge) driftTimer = 0f;

        for (EvilSpirit p : evilspirits) {
            if (nudge) p.nudgeVelocity();
            p.update(dt, W, H);

            // ===================== BLUE BALL =====================
            if (blueBall != null && p.collides(blueBall)) {

                if (p.getType() == PhantomType.BLUE) {
                    // Player eats spirit: spirit loses HP continuously, player heals continuously
                    float drain = Const.PROP_DRAIN_RATE * dt;

                    // IMPORTANT: EvilSpirit.takeDamage should return actual HP removed
                    float dealt = p.takeDamage(drain);

                    // XP gained per damage dealt
                    addBlueXp(dealt * Const.XP_PER_DAMAGE);

                    blueBall.grow(Const.BALL_GROW_RATE * dt);

                    if (p.isDead()) {
                        score++;
                        p.respawn(W, H);
                    }

                } else {
                    // Spirit eats player: player loses HP, spirit heals/grows
                    blueBall.takeDamage(Const.BALL_DAMAGE_RATE * dt);
                    p.heal(Const.SPIRIT_GROWTH_RATE * dt);

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
                    float drain = Const.PROP_DRAIN_RATE * dt;
                    float dealt = p.takeDamage(drain);

                    addRedXp(dealt * Const.XP_PER_DAMAGE);

                    redBall.grow(Const.BALL_GROW_RATE * dt);

                    if (p.isDead()) {
                        score++;
                        p.respawn(W, H);
                    }

                } else {
                    redBall.takeDamage(Const.BALL_DAMAGE_RATE * dt);
                    p.heal(Const.SPIRIT_GROWTH_RATE * dt);

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
        for (EvilSpirit p : evilspirits) p.draw(batch);
        if (blueBall != null) blueBall.draw(batch);
        if (redBall != null) redBall.draw(batch);
        batch.end();

        if (!Const.DEBUG_DRAW) return;

        sr.begin(ShapeRenderer.ShapeType.Line);
        for (EvilSpirit p : evilspirits) p.drawDebug(sr);
        if (blueBall != null) blueBall.drawDebug(sr);
        if (redBall != null) redBall.drawDebug(sr);
        sr.end();
    }

    public void drawDebugBounds(ShapeRenderer sr) {
        if (!Const.DEBUG_DRAW) return;

        sr.setColor(0f, 1f, 0f, 1f);
        sr.rect(0, 0, W, H);
    }

    public int getScore() { return score; }
    public boolean isBlueUsed() { return blueUsed; }
    public boolean isRedUsed() { return redUsed; }

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
        EvilSpirit.disposeAll();
    }
}
