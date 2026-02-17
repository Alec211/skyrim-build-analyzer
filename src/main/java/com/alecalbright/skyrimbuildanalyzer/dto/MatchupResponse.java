package com.alecalbright.skyrimbuildanalyzer.dto;

public record MatchupResponse(
    int fightsSimulated,
    FighterProfile fighter1,
    FighterProfile fighter2,
    FightStats stats
) {

    public record FighterProfile(
        String archetype,
        String weapon,
        double maxHealth,
        double baseDamage,
        int wins,
        double winRate,
        double averageDPS
    ) {}

    public record FightStats(
        int draws,
        double averageFightLength,
        int shortestFight,
        int longestFight,
        double averageTimeToKill,
        double confidenceIntervalLow,
        double confidenceIntervalHigh,
        boolean statisticallySignificant
    ) {}
}
