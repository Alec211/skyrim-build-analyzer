package com.alecalbright.skyrimbuildanalyzer.model;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;

public record ArchetypeRanking(
    CharacterArchetype archetype,
    int totalWins,
    int totalLosses,
    int totalDraws,
    double overallWinRate,
    String tier,
    String bestMatchup,
    String worstMatchup
) {
    public ArchetypeRanking {
        if (archetype == null) {
            throw new IllegalArgumentException("Archetype cannot be null");
        }
    }

    public int totalFights(){
        return totalWins + totalLosses + totalDraws;
    }

    @Override
    public String toString(){
        return String.format("[%s] %s — %.1f%% win rate (%d-%d-%d)",
            tier, archetype.getDisplayName(), overallWinRate,
            totalWins, totalLosses, totalDraws);
    }
}
