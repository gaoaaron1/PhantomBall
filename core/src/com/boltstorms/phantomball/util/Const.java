package com.boltstorms.phantomball.util;

public final class Const {

    private Const() {}

    // ===================== DEBUG =====================
    public static final boolean DEBUG_DRAW = true;

    // ===================== GLOBAL SIZE =====================
    // Master size control (scales EVERYTHING proportionally)
    public static final float BALL_SIZE_SCALE = 0.64f;

    // Visual size relative to collision
    public static final float BALL_SPRITE_SCALE = 2.4f;

    // Collision size relative to stats
    public static final float BALL_COLLISION_SCALE = 0.75f;

    // Evil Spirit visual scaling (kept proportional to balls)
    public static final float SPIRIT_SIZE_SCALE = 0.64f;

    // ===================== XP =====================
    // XP gained per 1 HP of damage dealt to correct-color spirit
    public static final float XP_PER_DAMAGE = 0.10f;

    // ===================== VIRTUAL RESOLUTION =====================
    public static final float VIRTUAL_W = 540f;
    public static final float VIRTUAL_H = 960f;

    // ===================== TIMING =====================
    public static final float DRIFT_NUDGE_TIME = 1.5f;

    // ===================== BALL / SPIRIT HEALTH & SIZE =====================
    public static final float BALL_START_RADIUS = 16f;
    public static final float BALL_MIN_RADIUS   = 8f;
    public static final float BALL_MAX_RADIUS   = 60f;

    public static final float PROP_DIE_RADIUS   = 10.0f;

    // ===================== CORE COMBAT (SYMMETRIC) =====================
    // These four values are intentionally paired

    // Correct-color drain (YOU win)
    public static final float PROP_DRAIN_RATE  = 55f; // spirit loses HP/sec
    public static final float BALL_GROW_RATE   = 25f; // you gain HP/sec

    // Wrong-color drain (ENEMY wins)
    public static final float BALL_DAMAGE_RATE = 55f; // you lose HP/sec
    public static final float SPIRIT_GROWTH_RATE = 25f; // spirit gains HP/sec

    // ===================== SPAWN / MISC =====================
    public static final float PROP_HIT_SHRINK = 4.0f;

    // Spirit spawn level range
    public static final int SPIRIT_MIN_LV = 1;
    public static final int SPIRIT_MAX_LV = 1;
}
