package com.boltstorms.phantomball.gameplay.stats;

public final class BallStats {
    public final int level;

    public final float speed;

    // Combat-ish stats
    public final float maxHp;
    public final float attack;
    public final float resistance; // 0..1

    // Visual/size caps
    public final float maxRadius;

    // (Optional tuning knobs you might still want later)
    public final float growAmount;
    public final float shrinkAmount;

    // Leveling
    public final int xpToNext;

    public BallStats(
            int level,
            float speed,
            float maxHp,
            float attack,
            float resistance,
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
        this.maxRadius = maxRadius;
        this.growAmount = growAmount;
        this.shrinkAmount = shrinkAmount;
        this.xpToNext = xpToNext;
    }

    public float applyResistance(float incomingDamage) {
        return incomingDamage * (1f - resistance);
    }
}
