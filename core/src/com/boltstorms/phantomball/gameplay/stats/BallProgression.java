package com.boltstorms.phantomball.gameplay.stats;

import com.badlogic.gdx.math.MathUtils;
import com.boltstorms.phantomball.gameplay.PhantomType;

public final class BallProgression {

    private BallProgression() {}

    public static BallStats statsFor(PhantomType type, int level) {
        int lv = Math.max(1, level);

        // You can tune blue/red to feel different
        float baseHp   = (type == PhantomType.BLUE) ? 120f : 110f;
        float baseAtk  = (type == PhantomType.BLUE) ? 10f  : 12f;
        float baseSpd  = (type == PhantomType.BLUE) ? 210f : 210f;

        float hp   = baseHp  + (lv - 1) * 18f;
        float atk  = baseAtk + (lv - 1) * 2.2f;

        // cap resistance so it doesn't become invincible
        float res  = MathUtils.clamp(0.08f + (lv - 1) * 0.03f, 0f, 0.45f);

        float spd  = baseSpd + (lv - 1) * 6f;

        // max radius cap grows slowly per level
        float maxR = 60f + (lv - 1) * 3f;

        // optional knobs (not required by the new HP system)
        float growAmount   = 2.2f + (lv - 1) * 0.15f;
        float shrinkAmount = 3.2f + (lv - 1) * 0.20f;

        int xpToNext = 10 + (lv - 1) * 5;

        return new BallStats(
                lv,
                spd,
                hp,
                atk,
                res,
                maxR,
                growAmount,
                shrinkAmount,
                xpToNext
        );
    }
}
