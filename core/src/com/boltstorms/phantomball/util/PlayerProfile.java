package com.boltstorms.phantomball.util;

public final class PlayerProfile {

    private static String playerName = "Player";

    private static int blueLevel = 1;
    private static int redLevel  = 1;

    private PlayerProfile() {}

    public static void setPlayerName(String name) {
        if (name == null) return;
        name = name.trim();
        playerName = name.isEmpty() ? "Player" : name;
    }

    public static String getPlayerName() {
        return playerName;
    }

    public static int getBlueLevel() { return blueLevel; }
    public static int getRedLevel()  { return redLevel;  }

    public static void setBlueLevel(int level) {
        blueLevel = Math.max(1, level);
    }

    public static void setRedLevel(int level) {
        redLevel = Math.max(1, level);
    }

    // NEW: simple level-up helpers
    public static int levelUpBlue() {
        blueLevel++;
        return blueLevel;
    }

    public static int levelUpRed() {
        redLevel++;
        return redLevel;
    }
}
