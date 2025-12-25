package com.boltstorms.phantomball.gameplay.stats;

import com.badlogic.gdx.math.MathUtils;
import com.boltstorms.phantomball.gameplay.PhantomType;

public final class BallProgression {

    private BallProgression() {}

    public static BallStats statsFor(PhantomType type, int level) {
        int lv = Math.max(1, level);

        // ===== HP (LOW START, GOOD READABILITY) =====
        float baseHp = 10f;        // Level 1
        float hpPerLevel = 10f;    // Growth per level
        float hp = baseHp + (lv - 1) * hpPerLevel;

        // ===== Combat =====
        float baseAtk = (type == PhantomType.BLUE) ? 10f : 12f;
        float atk = baseAtk + (lv - 1) * 2.2f;

        float res = MathUtils.clamp(
                0.05f + (lv - 1) * 0.03f,
                0f,
                0.45f
        );

        // ===== Speed =====
        float baseSpd = 210f;
        float spd = baseSpd + (lv - 1) * 6f;

        // ===== Size bounds (HP maps into this) =====
        float minR = 10f + (lv - 1) * 2f;
        float maxR = 30f + (lv - 1) * 3f;

        float growAmount   = 2.2f + (lv - 1) * 0.15f;
        float shrinkAmount = 3.2f + (lv - 1) * 0.20f;

        int xpToNext = 10 + (lv - 1) * 5;

        return new BallStats(
                lv,
                spd,
                hp,
                atk,
                res,
                minR,
                maxR,
                growAmount,
                shrinkAmount,
                xpToNext
        );
    }
}
