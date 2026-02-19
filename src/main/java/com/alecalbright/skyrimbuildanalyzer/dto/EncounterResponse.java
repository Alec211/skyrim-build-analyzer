package com.alecalbright.skyrimbuildanalyzer.dto;

import java.util.List;

public record EncounterResponse(
    String archetype,
    int totalRuns,
    double survivalRate,
    double avgEnemiesDefeated,
    double avgDamageDealt,
    double avgDamageReceived,
    List<EnemyFightSummary> perEnemyBreakdown
) {

    public record EnemyFightSummary(
        String enemyName,
        double winRate,
        double avgDamageDealt,
        double avgDamageReceived
    ) {}
}
