package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.List;

public record EncounterResult(
    String playerName,
    int enemiesDefeated,
    int totalEnemies,
    boolean playerSurvived,
    double remainingHP,
    double totalDamageDealt,
    double totalDamageReceived,
    List<FightResult> individualFights,
    String killedBy
) {
    public EncounterResult {
        individualFights = List.copyOf(individualFights);
    }

    public double survivalPercentage(){
        return totalEnemies > 0 ? (enemiesDefeated * 100.0) / totalEnemies : 0.0;
    }

    @Override
    public String toString(){
        return String.format("%s defeated %d/%d enemies, %s (HP: %.0f)",
            playerName, enemiesDefeated, totalEnemies,
            playerSurvived ? "SURVIVED" : "KILLED BY " + killedBy,
            remainingHP);
    }
}
