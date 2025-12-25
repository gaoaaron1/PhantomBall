package com.boltstorms.phantomball.util;

public final class Const {

    private Const() {}

    public static final boolean DEBUG_DRAW = true; // ← toggle this
    // Master size control (scales EVERYTHING proportionally)
    public static final float BALL_SIZE_SCALE = 0.64f; // try 0.7–0.95

    // Visual size relative to collision
    public static final float BALL_SPRITE_SCALE = 2.4f;

    // Collision size relative to stats
    public static final float BALL_COLLISION_SCALE = 0.75f;

    // XP gained per 1 HP of damage dealt to a correct-color spirit
    public static final float XP_PER_DAMAGE = 0.10f; // 10 damage = 1 XP (tweak)


    // ===== Virtual resolution (phone-like) =====
    public static final float VIRTUAL_W = 540f;
    public static final float VIRTUAL_H = 960f;

    // Timing
    public static final float DRIFT_NUDGE_TIME = 1.5f;

    // Ball size / health-like behavior
    public static final float BALL_START_RADIUS = 16f;
    public static final float BALL_MIN_RADIUS = 8f;    // die if smaller than this
    public static final float BALL_MAX_RADIUS = 60f;    // cap so it doesn't fill screen

    // Absorb / damage amounts
    public static final float PROP_DRAIN_RATE = 55f;   // radius per second drained from spirit
    public static final float BALL_GROW_RATE  = 25f;   // radius per second gained on correct drain

    public static final float BALL_DAMAGE_RATE = 65f;  // radius per second lost on wrong drain

    public static final float PROP_HIT_SHRINK = 4.0f;
    public static final float PROP_DIE_RADIUS = 10.0f;

    // ===== Evil Spirit scaling (so they can share BallStats without being gigantic) =====
    public static final float SPIRIT_SIZE_SCALE = 0.64f;     // overall spirit size

    // ===== Evil Spirit growth (when draining wrong-colored player) =====
    public static final float SPIRIT_GROWTH_RATE = 28f; // HP per second (tweak)


    // Spirit spawn level range
    public static final int SPIRIT_MIN_LV = 1;
    public static final int SPIRIT_MAX_LV = 5;

}
