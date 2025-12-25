package com.boltstorms.phantomball.gameplay.stats;

public final class BallStats {
    public final int level;

    public final float speed;
    public final float damageToProp;
    public final float resistance;

    public final float maxRadius;
    public final float growAmount;
    public final float shrinkAmount;

    public BallStats(
            int level,
            float speed,
            float damageToProp,
            float resistance,
            float maxRadius,
            float growAmount,
            float shrinkAmount
    ) {
        this.level = level;
        this.speed = speed;
        this.damageToProp = damageToProp;
        this.resistance = resistance;
        this.maxRadius = maxRadius;
        this.growAmount = growAmount;
        this.shrinkAmount = shrinkAmount;
    }

    public float applyResistance(float incomingShrink) {
        return incomingShrink * (1f - resistance);
    }
}
