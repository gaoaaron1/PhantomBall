package com.boltstorms.phantomball.gameplay.stats;

import com.boltstorms.phantomball.gameplay.PhantomType;
import com.boltstorms.phantomball.util.Const;

public final class BallProgression {
    private BallProgression() {}

    public static BallStats statsFor(PhantomType type, int level) {
        if (level < 1) level = 1;

        float baseSpeed = (float) Math.sqrt(
                Const.BALL_SPEED_X * Const.BALL_SPEED_X +
                        Const.BALL_SPEED_Y * Const.BALL_SPEED_Y
        );

        float speed        = baseSpeed + (level - 1) * 12f;
        float damageToProp = Const.PROP_HIT_SHRINK + (level - 1) * 0.6f;
        float resistance   = Math.min(0.50f, 0.05f * (level - 1)); // cap 50%
        float maxRadius    = Const.BALL_MAX_RADIUS + (level - 1) * 4f;
        float growAmount   = Const.BALL_GROW_AMOUNT + (level - 1) * 0.15f;
        float shrinkAmount = Const.BALL_SHRINK_AMOUNT + (level - 1) * 0.20f;

        return new BallStats(
                level,
                speed,
                damageToProp,
                resistance,
                maxRadius,
                growAmount,
                shrinkAmount
        );
    }
}
