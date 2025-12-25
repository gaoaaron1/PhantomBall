package com.boltstorms.phantomball.util;

public final class Const {

    private Const() {}

    public static final float HIT_COOLDOWN = 0.18f; // tweak

    // ===== Virtual resolution (phone-like) =====
    public static final float VIRTUAL_W = 540f;
    public static final float VIRTUAL_H = 960f;

    // World
    public static final float BALL_RADIUS = 18f;

    // Speeds
    public static final float BALL_SPEED_X = 195f;
    public static final float BALL_SPEED_Y = 160f;

    public static final float PROP_MIN_RADIUS = 14f;
    public static final float PROP_MAX_RADIUS = 32f;

    public static final float PROP_MIN_SPEED = 80f;
    public static final float PROP_MAX_SPEED = 220f;

    // Timing
    public static final float DRIFT_NUDGE_TIME = 1.5f;

    // Ball size / health-like behavior
    public static final float BALL_START_RADIUS = 32f;
    public static final float BALL_MIN_RADIUS = 16f;    // die if smaller than this
    public static final float BALL_MAX_RADIUS = 60f;    // cap so it doesn't fill screen

    // Absorb / damage amounts

    public static final float BALL_GROW_AMOUNT  = 2.2f;
    public static final float BALL_SHRINK_AMOUNT = 3.2f;
    public static final float PROP_DRAIN_RATE = 55f;   // radius per second drained from spirit
    public static final float BALL_GROW_RATE  = 25f;   // radius per second gained on correct drain

    public static final float BALL_DAMAGE_RATE = 65f;  // radius per second lost on wrong drain

    public static final float PROP_HIT_SHRINK = 4.0f;
    public static final float PROP_DIE_RADIUS = 10.0f;


}
