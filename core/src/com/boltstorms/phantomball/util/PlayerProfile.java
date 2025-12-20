package com.boltstorms.phantomball.util;

public final class PlayerProfile {

    private static String playerName = "Player";

    private PlayerProfile() {}

    public static void setPlayerName(String name) {
        if (name == null) return;
        name = name.trim();
        playerName = name.isEmpty() ? "Player" : name;
    }

    public static String getPlayerName() {
        return playerName;
    }
}
