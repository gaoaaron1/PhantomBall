package com.boltstorms.phantomball.gameplay.stats;

public final class BallStats {

    public final int level;
    public final float speed;

    public final float maxHp;
    public final float attack;
    public final float resistance;

    // NEW: size bounds per level
    public final float minRadius;
    public final float maxRadius;

    public final float growAmount;
    public final float shrinkAmount;

    public final int xpToNext;

    public BallStats(
            int level,
            float speed,
            float maxHp,
            float attack,
            float resistance,
            float minRadius,
            float maxRadius,
            float growAmount,
            float shrinkAmount,
            int xpToNext
    ) {
        this.level = level;
        this.speed = speed;
        this.maxHp = maxHp;
        this.attack = attack;
        this.resistance = resistance;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.growAmount = growAmount;
        this.shrinkAmount = shrinkAmount;
        this.xpToNext = xpToNext;
    }

    public float applyResistance(float incomingDamage) {
        return incomingDamage * (1f - resistance);
    }
}
