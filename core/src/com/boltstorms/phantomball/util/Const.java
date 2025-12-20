package com.boltstorms.phantomball.util;

public final class Const {

    // Prevent instantiation
    private Const() {}

    // World
    public static final float BALL_RADIUS = 18f;

    // Speeds
    public static final float BALL_SPEED_X = 240f;
    public static final float BALL_SPEED_Y = 170f;

    public static final float PROP_MIN_RADIUS = 14f;
    public static final float PROP_MAX_RADIUS = 28f;

    public static final float PROP_MIN_SPEED = 80f;
    public static final float PROP_MAX_SPEED = 220f;

    // Timing
    public static final float DRIFT_NUDGE_TIME = 1.5f;

    // Ball size / health-like behavior
    public static final float BALL_START_RADIUS = 18f;
    public static final float BALL_MIN_RADIUS = 10f;    // die if smaller than this
    public static final float BALL_MAX_RADIUS = 60f;    // cap so it doesn't fill screen

    // Absorb / damage amounts
    public static final float ABSORB_GROWTH = 2.2f;     // how much radius increases on safe hit
    public static final float DAMAGE_SHRINK = 3.5f;     // how much radius decreases on wrong hit

}
