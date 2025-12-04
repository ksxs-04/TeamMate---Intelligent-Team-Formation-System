package com.teammate.models;

public enum GameRole {
    STRATEGIST("Strategist"),
    DEFENDER("Defender"),
    ATTACKER("Attacker"),
    SUPPORT("Support"),
    ALL_ROUNDER("All_Rounder");

    private final String displayName;

    GameRole(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
